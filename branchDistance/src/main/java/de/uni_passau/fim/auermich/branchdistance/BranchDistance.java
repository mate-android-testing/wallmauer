package de.uni_passau.fim.auermich.branchdistance;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.branchdistance.analysis.Analyzer;
import de.uni_passau.fim.auermich.branchdistance.dto.MethodInformation;
import de.uni_passau.fim.auermich.branchdistance.instrumentation.Instrumentation;
import de.uni_passau.fim.auermich.branchdistance.utility.Utility;
import de.uni_passau.fim.auermich.branchdistance.xml.ManifestParser;
import lanchon.multidexlib2.BasicDexFileNamer;
import lanchon.multidexlib2.MultiDexIO;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Defines the entry point, i.e. the command line interface, of the branch distance
 * instrumentation.
 */
public class BranchDistance {

    private static final Logger LOGGER = LogManager.getLogger(BranchDistance.class);

    // the path to the APK file
    public static String apkPath;

    // the output path of the decoded APK
    public static String decodedAPKPath;

    // the API opcode level defined in the dex header (can be derived automatically)
    public static int OPCODE_API = 28;

    /*
     * Defines the number of additional registers. We require one additional register
     * for storing the unique branch id. Then, we need two additional registers for holding
     * the arguments of if instructions. In addition, we may need two further registers
     * for the shifting of the param registers, since the register type must be consistent
     * within a try-catch block, otherwise the verification process fails.
     */
    public static final int ADDITIONAL_REGISTERS = 3;

    /*
     * We can't instrument methods with more than 256 registers in total,
     * since certain instructions (which we make use of) only allow parameters with
     * register IDs < 256 (some even < 16). As we need two additional register,
     * the register count before instrumentation must be < 255.
     */
    private static final int MAX_TOTAL_REGISTERS = 255;

    /**
     * Processes the command line arguments. The following
     * arguments are mandatory:
     * <p>
     * 1) the path to the APK file.
     *
     * @param args The command line arguments.
     */
    private static void handleArguments(String[] args) {
        assert args.length == 1;

        apkPath = Objects.requireNonNull(args[0]);
        LOGGER.info("The path to the APK file is: " + apkPath);
    }

    /**
     * Defines the command-line entry point. To invoke the branch distance instrumentation,
     * solely the APK is required as input.
     *
     * @param args A single commandline argument specifying the path to the APK file.
     * @throws IOException        Should never happen.
     */
    public static void main(String[] args) throws IOException {

        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);

        if (args.length != 1) {
            LOGGER.info("Expect exactly one argument: path to the APK file");
        } else {

            // process command line arguments
            handleArguments(args);

            // describes class names we want to exclude from instrumentation
            Pattern exclusionPattern = Utility.readExcludePatterns();

            // the APK file
            File apkFile = new File(apkPath);

            // decode the APK file
            decodedAPKPath = Utility.decodeAPK(apkFile);

            /*
             * TODO: Directly read from APK file if possible (exception so far). This
             *  should be fixed with the next release, check the github page of (multi)dexlib2.
             *
             * Multidexlib2 provides a merged dex file. So, you don't have to care about
             * multiple dex files at all. When writing this merged dex file to a directory,
             * the dex file is split into multiple dex files such that the method reference
             * constraint is not violated.
             */
            DexFile mergedDex = MultiDexIO.readDexFile(true, new File(decodedAPKPath),
                    new BasicDexFileNamer(), null, null);

            // instrument + write merged dex file to directory
            instrument(mergedDex, exclusionPattern);

            ManifestParser manifest = new ManifestParser(decodedAPKPath + File.separator + "AndroidManifest.xml");

            // add broadcast receiver tag into AndroidManifest
            if (!manifest.addBroadcastReceiverTag(
                    "de.uni_passau.fim.auermich.tracer.Tracer",
                    "STORE_TRACES")) {
                LOGGER.warn("Couldn't insert broadcast receiver tag!");
                return;
            }

            // mark app as debuggable
            if (!manifest.addDebuggableFlag()) {
                LOGGER.warn("Couldn't mark app as debuggable!");
                return;
            }

            // add external storage write permission
            if (!manifest.addPermissionTag("android.permission.WRITE_EXTERNAL_STORAGE")
                    || !manifest.addPermissionTag("android.permission.READ_EXTERNAL_STORAGE")) {
                LOGGER.warn("Couldn't add read/write permission for external storage!");
                return;
            }

            // the output name of the APK
            File outputAPKFile = new File(apkPath.replace(".apk", "-instrumented.apk"));

            // build the APK to the
            Utility.buildAPK(decodedAPKPath, outputAPKFile);

            // remove the decoded APK files
            FileUtils.deleteDirectory(new File(decodedAPKPath));
        }
    }

    /**
     * Instruments the classes respectively methods within a (merged) dex file.
     *
     * @param dexFile          The dexFile containing the classes and methods.
     * @param exclusionPattern A pattern describing classes that should be excluded from instrumentation.
     * @throws IOException Should never happen.
     */
    private static void instrument(DexFile dexFile, Pattern exclusionPattern) throws IOException {

        LOGGER.info("Starting Instrumentation of App!");

        LOGGER.info("Dex version: " + dexFile.getOpcodes().api);

        // set the opcode api level
        OPCODE_API = dexFile.getOpcodes().api;

        // the set of classes we write into the instrumented classes.dex file
        List<ClassDef> classes = Lists.newArrayList();

        for (ClassDef classDef : dexFile.getClasses()) {

            // the class name is part of the method id
            String className = Utility.dottedClassName(classDef.getType());

            // exclude certain packages/classes from instrumentation, e.g. android.widget.*
            if ((exclusionPattern != null && exclusionPattern.matcher(className).matches())
                    || Utility.isResourceClass(classDef)
                    || Utility.isBuildConfigClass(classDef)) {
                LOGGER.info("Excluding class: " + className + " from instrumentation!");
                classes.add(classDef);
                continue;
            }

            boolean isActivity = false;
            boolean isFragment = false;

            // check whether the current class is an activity/fragment class
            if (Utility.isActivity(classes, classDef)) {
                isActivity = true;
            } else if (Utility.isFragment(classes, classDef)) {
                isFragment = true;
            }

            // track which activity/fragment lifecycle methods are missing
            Set<String> activityLifeCycleMethods = new HashSet<>(Utility.getActivityLifeCycleMethods());
            Set<String> fragmentLifeCycleMethods = new HashSet<>(Utility.getFragmentLifeCycleMethods());

            // the set of methods included in the instrumented classes.dex
            List<Method> methods = Lists.newArrayList();

            // track whether we modified the method or not
            boolean modifiedMethod = false;

            for (Method method : classDef.getMethods()) {

                // each method is identified by its class name and method name
                String id = method.toString();

                // track which lifecycle methods are missing, i.e. not overwritten lifecycle methods
                if (isActivity) {
                    String methodName = id.split("->")[1];
                    if (activityLifeCycleMethods.contains(methodName)) {
                        activityLifeCycleMethods.remove(methodName);
                    }
                } else if (isFragment) {
                    String methodName = id.split("->")[1];
                    if (fragmentLifeCycleMethods.contains(methodName)) {
                        fragmentLifeCycleMethods.remove(methodName);
                    }
                }

                MethodInformation methodInformation = new MethodInformation(id, classDef, method, dexFile);
                MethodImplementation methImpl = methodInformation.getMethodImplementation();

                /* We can only instrument methods with a given register count because
                 * our instrumentation code uses instructions that only the usage of
                 * registers with a register ID < MAX_TOTAL_REGISTERS, i.e. the newly
                 * inserted registers aren't allowed to exceed this limit.
                 */
                if (methImpl != null && methImpl.getRegisterCount() < MAX_TOTAL_REGISTERS) {

                    LOGGER.info("Instrumenting method " + method.toString());

                    // determine the new local registers and free register IDs
                    Analyzer.computeRegisterStates(methodInformation, ADDITIONAL_REGISTERS);

                    // determine where we need to instrument
                    methodInformation.setInstrumentationPoints(Analyzer.trackInstrumentationPoints(methodInformation));

                    // determine the method entry points
                    methodInformation.setMethodEntries(Analyzer.trackMethodEntries(methodInformation, dexFile));

                    // determine the method exit points
                    methodInformation.setMethodExits(Analyzer.trackMethodExits(methodInformation));

                    // determine the location of try blocks
                    methodInformation.setTryBlocks(Analyzer.getTryBlocks(methodInformation));

                    // determine the register type of the param registers if the method has param registers
                    if (methodInformation.getParamRegisterCount() > 0) {
                        Analyzer.analyzeParamRegisterTypes(methodInformation, dexFile);
                    }

                    // instrument branches, if statements as well as method entry and exit
                    Instrumentation.modifyMethod(methodInformation, dexFile);
                    modifiedMethod = true;

                    /*
                     * We need to shift param registers by two positions to the left,
                     * e.g. move p1, p2, such that the last (two) param register(s) is/are
                     * free for use. We need two regs for wide types which span over 2 regs.
                     */
                    if (methodInformation.getParamRegisterCount() > 0) {
                        Instrumentation.shiftParamRegisters(methodInformation);
                    }

                    // add instrumented method implementation
                    Utility.addInstrumentedMethod(methods, methodInformation);

                    // write out the branches per method
                    Utility.writeBranches(methodInformation);
                } else {
                    // no modification necessary
                    methods.add(method);
                }
            }

            // add dummy implementation for missing activity/fragment lifecycle methods
            if (isActivity) {
                LOGGER.info("Missing activity lifecycle methods: " + activityLifeCycleMethods);
                activityLifeCycleMethods.forEach(method -> Instrumentation.addLifeCycleMethod(method, methods, classDef));
                modifiedMethod = true;
            } else if (isFragment) {
                LOGGER.info("Missing fragment lifecycle methods: " + fragmentLifeCycleMethods);
                fragmentLifeCycleMethods.forEach(method -> Instrumentation.addLifeCycleMethod(method, methods, classDef));
                modifiedMethod = true;
            }

            if (!modifiedMethod) {
                classes.add(classDef);
            } else {
                // add modified class including its method to the list of classes
                Utility.addInstrumentedClass(classes, methods, classDef);
            }
        }

        // insert tracer class
        ClassDef tracerClass = Utility.loadTracer(OPCODE_API);
        classes.add(tracerClass);

        // write modified (merged) dex file to directory
        Utility.writeMultiDexFile(decodedAPKPath, classes, OPCODE_API);
    }

}
