package de.uni_passau.fim.auermich.instrumentation.branchcoverage;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.instrumentation.branchcoverage.analysis.Analyzer;
import de.uni_passau.fim.auermich.instrumentation.branchcoverage.core.Instrumentation;
import de.uni_passau.fim.auermich.instrumentation.branchcoverage.dto.MethodInformation;
import de.uni_passau.fim.auermich.instrumentation.branchcoverage.utility.Utility;
import de.uni_passau.fim.auermich.instrumentation.branchcoverage.xml.ManifestParser;
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
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BranchCoverage {

    // the logger instance
    private static final Logger LOGGER = LogManager.getLogger(BranchCoverage.class);

    // the path to the APK file
    public static String apkPath;

    // the output path of the decoded APK
    public static File decodedAPKPath;

    // whether only classes belonging to the app package should be instrumented
    private static boolean onlyInstrumentAUTClasses = false;

    /*
     * Defines the number of additional registers. We require one additional register
     * for storing the unique branch id. We require a second register when shifting
     * wide types.
     */
    public static final int ADDITIONAL_REGISTERS = 2;

    /*
     * We can't instrument methods with more than 256 registers in total,
     * since certain instructions (which we make use of) only allow parameters with
     * register IDs < 256 (some even < 16). As we need two additional register,
     * the register count before instrumentation must be < 255.
     */
    private static final int MAX_TOTAL_REGISTERS = 255;

    /**
     * Processes the command line arguments. The following arguments are supported:
     * <p>
     * 1) the path to the APK file.
     * 2) The flag --only-aut to instrument only classes belonging to the app package (optional).
     *
     * @param args The command line arguments.
     */
    private static void handleArguments(String[] args) {
        assert args.length >= 1 && args.length <= 2;

        apkPath = Objects.requireNonNull(args[0]);
        LOGGER.info("The path to the APK file is: " + apkPath);

        if (args.length == 2) {
            if (args[1].equals("--only-aut")) {
                LOGGER.info("Only instrumenting classes belonging to the app package!");
                onlyInstrumentAUTClasses = true;
            } else {
                LOGGER.info("Argument " + args[1] + " not recognized!");
            }
        }
    }

    /**
     * Instruments a given APK file with branch coverage information.
     *
     * @param args The path to the APK file.
     * @throws IOException Should never happen.
     */
    public static void main(String[] args) throws IOException {

        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);

        if (args.length < 1 || args.length > 2) {
            LOGGER.info("Wrong number of arguments!");
            LOGGER.info("Usage: java -jar branchCoverage.jar <path to the APK file> --only-aut (optional)");
        } else {

            // process command line arguments
            handleArguments(args);

            // the APK file
            File apkFile = new File(apkPath);

            // decode the APK file
            decodedAPKPath = Utility.decodeAPK(apkFile);

            ManifestParser manifest = new ManifestParser(decodedAPKPath + File.separator + "AndroidManifest.xml");

            // retrieve package name and main activity
            if (!manifest.parseManifest()) {
                LOGGER.warn("Couldn't retrieve MainActivity and/or PackageName!");
                return;
            }

            /*
             * TODO: Directly read from APK file if possible (exception so far). This
             *  should be fixed with the next release, check the github page of (multi)dexlib2.
             *
             * Multidexlib2 provides a merged dex file. So, you don't have to care about
             * multiple dex files at all. When writing this merged dex file to a directory,
             * the dex file is split into multiple dex files such that the method reference
             * constraint is not violated.
             */
            DexFile mergedDex = MultiDexIO.readDexFile(true, decodedAPKPath,
                    new BasicDexFileNamer(), null, null);

            instrument(mergedDex, manifest.getPackageName());

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
            FileUtils.deleteDirectory(decodedAPKPath);
        }
    }

    /**
     * Instruments the classes respectively methods within a (merged) dex file.
     *
     * @param dexFile The dexFile containing the classes and methods.
     * @param packageName The package name of the app.
     * @throws IOException Should never happen.
     */
    private static void instrument(final DexFile dexFile, final String packageName) throws IOException {

        LOGGER.info("Starting Instrumentation of App!");
        LOGGER.info("Dex version: " + dexFile.getOpcodes().api);
        LOGGER.info("Package Name: " + packageName);

        // describes class names we want to exclude from instrumentation
        final Pattern exclusionPattern = Utility.readExcludePatterns();

        List<ClassDef> instrumentedClasses = dexFile.getClasses().parallelStream()
                .map(classDef -> instrumentClass(dexFile, classDef, packageName, exclusionPattern))
                .collect(Collectors.toList());

        // insert tracer class
        ClassDef tracerClass = Utility.loadTracer(dexFile.getOpcodes().api);
        instrumentedClasses.add(tracerClass);

        // write modified (merged) dex file to directory
        Utility.writeMultiDexFile(decodedAPKPath, instrumentedClasses, dexFile.getOpcodes().api);
    }

    /**
     * Instruments the given class.
     *
     * @param dexFile The dex file containing the class.
     * @param classDef The class to be instrumented.
     * @param packageName The package name of the app.
     * @param exclusionPattern A pattern of classes that should be excluded from the instrumentation process.
     * @return Returns the instrumented class.
     */
    private static ClassDef instrumentClass(DexFile dexFile, ClassDef classDef, String packageName, Pattern exclusionPattern) {

        // the class name is part of the method id
        String className = Utility.dottedClassName(classDef.getType());

        // if only classes belonging to the app package should be instrumented
        if (onlyInstrumentAUTClasses && !className.startsWith(packageName)) {
            LOGGER.info("Excluding class: " + className + " from instrumentation!");
            return classDef;
        }

        // exclude certain packages/classes from instrumentation, e.g. android.widget.*
        if ((exclusionPattern != null && exclusionPattern.matcher(className).matches())
                || Utility.isResourceClass(classDef)
                || Utility.isBuildConfigClass(classDef)) {
            LOGGER.info("Excluding class: " + className + " from instrumentation!");
            return classDef;
        }

        List<Method> instrumentedMethods = Lists.newArrayList(classDef.getMethods()).parallelStream()
                .map(method -> instrumentMethod(dexFile, classDef, method))
                .collect(Collectors.toList());

        return new ImmutableClassDef(
                classDef.getType(),
                classDef.getAccessFlags(),
                classDef.getSuperclass(),
                classDef.getInterfaces(),
                classDef.getSourceFile(),
                classDef.getAnnotations(),
                classDef.getFields(),
                instrumentedMethods);
    }

    /**
     * Instruments the given method.
     *
     * @param dexFile The dex file containing the method.
     * @param classDef The class containing the method.
     * @param method The method to be instrumented.
     * @return Returns the instrumented method.
     */
    private static Method instrumentMethod(DexFile dexFile, ClassDef classDef, Method method) {

        // each method is identified by its class name and method name
        String id = method.toString();

        MethodInformation methodInformation = new MethodInformation(id, classDef, method, dexFile);
        MethodImplementation methImpl = methodInformation.getMethodImplementation();

        /* We can only instrument methods with a given register count because
         * our instrumentation code uses instructions that only allow the usage of
         * registers with a register ID < MAX_TOTAL_REGISTERS, i.e. the newly
         * inserted registers aren't allowed to exceed this limit.
         */
        if (methImpl != null && methImpl.getRegisterCount() < MAX_TOTAL_REGISTERS) {

            LOGGER.info("Instrumenting method " + method + " of class " + classDef);

            // determine the new local registers and free register IDs
            Analyzer.computeRegisterStates(methodInformation, ADDITIONAL_REGISTERS);

            // determine the location of the branches
            methodInformation.setInstrumentationPoints(Analyzer.trackInstrumentationPoints(methodInformation));

            // determine the location of try blocks
            methodInformation.setTryBlocks(Analyzer.getTryBlocks(methodInformation));

            // determine the register type of the param registers if the method has param registers
            if (methodInformation.getParamRegisterCount() > 0) {
                Analyzer.analyzeParamRegisterTypes(methodInformation, dexFile);
            }

            // instrument branches
            Instrumentation.modifyMethod(methodInformation);

            /*
             * We need to shift param registers by two positions to the left,
             * e.g. move p1, p2, such that the last (two) param register(s) is/are
             * free for use. We need two regs for wide types which span over 2 regs.
             */
            if (methodInformation.getParamRegisterCount() > 0) {
                Instrumentation.shiftParamRegisters(methodInformation);
            }

            // write out the branches per method
            Utility.writeBranches(methodInformation);

            return new ImmutableMethod(
                    method.getDefiningClass(),
                    method.getName(),
                    method.getParameters(),
                    method.getReturnType(),
                    method.getAccessFlags(),
                    method.getAnnotations(),
                    null, // necessary since dexlib2 2.4.0
                    methodInformation.getMethodImplementation());
        } else {
            // not possible to instrument
            return method;
        }
    }
}
