package de.uni_passau.fim.auermich.branchdistance;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.branchdistance.analysis.Analyzer;
import de.uni_passau.fim.auermich.branchdistance.dto.MethodInformation;
import de.uni_passau.fim.auermich.branchdistance.instrumentation.Instrumentation;
import de.uni_passau.fim.auermich.branchdistance.utility.Utility;
import de.uni_passau.fim.auermich.branchdistance.xml.ManifestParser;
import org.apache.commons.io.FileUtils;
import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.*;
import org.jf.smali.Smali;
import org.jf.smali.SmaliOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class BranchDistance {

    /* PARAMS SECTION BEGIN */

    // the package name declared in the AndroidManifest file
    public static String packageName;

    // the name of the mainActivity declared in the AndroidManifest file
    public static String mainActivity;

    // the path to the APK file
    public static String apkPath;

    // the output path of the decoded APK
    public static String decodedAPKPath;

    /* PARAMS SECTION END */

    // dex op code specified in header of classes.dex file
    public static final int OPCODE_API = 28;

    // the number of additional required registers (two for wide-types required when moving)
    public static final int ADDITIONAL_REGISTERS = 2;

    // the dex conform mainActivity name, uses '/' instead of '.'
    public static String mainActivityDex;

    // the logger instance
    private static final Logger LOGGER = Logger.getLogger(BranchDistance.class
            .getName());

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
     * Invokes the instrumentation process on a given app.
     *
     * @param args A single commandline argument specifying the path to the APK file.
     * @throws IOException Should never happen.
     * @throws URISyntaxException Should never happen.
     */
    public static void main(String[] args) throws IOException, URISyntaxException {

        LOGGER.setLevel(Level.ALL);

        if (args.length != 1) {
            LOGGER.info("Expect exactly one argument: path to the APK file");
        } else {

            // process command line arguments
            handleArguments(args);

            // describes class names we want to exclude from instrumentation
            Pattern exclusionPattern = Utility.readExcludePatterns();

            // the APK file
            File apkFile = new File(apkPath);

            // process directly apk file (support for multi-dex)
            MultiDexContainer<? extends DexBackedDexFile> apk
                    = DexFileFactory.loadDexContainer(apkFile, Opcodes.forApi(OPCODE_API));

            // decode the APK file
            decodedAPKPath = Utility.decodeAPK(apkFile);

            ManifestParser manifest = new ManifestParser(decodedAPKPath + File.separator + "AndroidManifest.xml");

            // retrieve package name and main activity
            if (!manifest.parseManifest()) {
                LOGGER.warning("Couldn't retrieve MainActivity and/or PackageName!");
                return;
            }

            mainActivity = manifest.getMainActivity();
            packageName = manifest.getPackageName();

            // convert the MainActivity to dex format
            mainActivityDex = "L" + mainActivity.replaceAll("\\.", "/") + ";";

            // instrument all the dex files included in the APK file
            apk.getDexEntryNames().forEach(dexFile -> {
                try {
                    instrument(apk.getEntry(dexFile).getDexFile(), dexFile, exclusionPattern);
                } catch (IOException e) {
                    LOGGER.warning("Failure loading dexFile");
                    LOGGER.warning(e.getMessage());
                }
            });

            // add broadcast receiver tag into AndroidManifest
            if (!manifest.addBroadcastReceiverTag(
                    "de.uni_passau.fim.auermich.branchdistance.tracer.Tracer",
                    "STORE_TRACES")) {
                LOGGER.warning("Couldn't insert broadcast receiver tag!");
                return;
            }

            // mark app as debuggable
            if (!manifest.addDebuggableFlag()) {
                LOGGER.warning("Couldn't mark app as debuggable!");
                return;
            }

            // add external storage write permission
            if (!manifest.addPermissionTag("android.permission.WRITE_EXTERNAL_STORAGE")
                    || !manifest.addPermissionTag("android.permission.READ_EXTERNAL_STORAGE")) {
                LOGGER.warning("Couldn't add read/write permission for external storage!");
                return;
            }

            // we insert into the last classes.dex file our tracer functionality

            // the path to the last dex file, e.g. classes3.dex
            String lastDexFile = decodedAPKPath + File.separator
                    + apk.getDexEntryNames().get(apk.getDexEntryNames().size()-1);

            // the output directory for baksmali d
            File smaliFolder = new File(decodedAPKPath + File.separator + "out");

            // baksmali d classes.dex -o out
            Baksmali.disassembleDexFile(DexFileFactory.loadDexFile(lastDexFile,
                    Opcodes.forApi(OPCODE_API)),smaliFolder, 1, new BaksmaliOptions());

            // the location of the tracer directory
            File tracerFolder = Paths.get(smaliFolder.getAbsolutePath(), "de", "uni_passau",
                    "fim", "auermich", "branchdistance", "tracer").toFile();
            // TODO: verify that mkdirs doesn't overwrite pre-existing sub-directories/files
            tracerFolder.mkdirs();

            // copy from resource folder Tracer.smali to smali folder
            InputStream inputStream = BranchDistance.class.getClassLoader().getResourceAsStream("Tracer.smali");
            File tracerFile = new File(tracerFolder, "Tracer.smali");
            FileUtils.copyInputStreamToFile(inputStream, tracerFile);

            // smali a out -o classes.dex
            SmaliOptions smaliOptions = new SmaliOptions();
            smaliOptions.outputDexFile = lastDexFile;
            Smali.assemble(smaliOptions, smaliFolder.getAbsolutePath());

            // the output name of the APK
            File outputAPKFile = new File(apkPath.replace(".apk", "-instrumented.apk"));

            // build the APK to the
            Utility.buildAPK(decodedAPKPath, outputAPKFile);

            // remove the decoded APK files
            FileUtils.deleteDirectory(new File(decodedAPKPath));
        }
    }

    /**
     * Instruments the classes respectively methods within a dex file.
     *
     * @param dexFile The dexFile containing the classes and methods.
     * @param dexFileName The name of the dexFile.
     * @param exclusionPattern A pattern describing classes that should be excluded from instrumentation.
     * @throws IOException Should never happen.
     */
    private static void instrument(DexFile dexFile, String dexFileName, Pattern exclusionPattern) throws  IOException {

        LOGGER.info("Starting Instrumentation of App!");

        // the set of classes we write into the instrumented classes.dex file
        List<ClassDef> classes = Lists.newArrayList();

        for (ClassDef classDef : dexFile.getClasses()) {

            // the class name is part of the method id
            String className = Utility.dottedClassName(classDef.getType());

            // exclude certain packages/classes from instrumentation, e.g. android.widget.*
            if (exclusionPattern != null && exclusionPattern.matcher(className).matches()) {
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

            // count the number of branches per class
            int numberOfBranches = 0;

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

                    LOGGER.info("Instrumenting method " + method.getName() + " of class " + classDef.toString());

                    // determine the new local registers and free register IDs
                    Analyzer.computeRegisterStates(methodInformation,ADDITIONAL_REGISTERS);

                    // determine where we need to instrument
                    methodInformation.setInstrumentationPoints(Analyzer.trackInstrumentationPoints(methodInformation));

                    // determine the method entry points
                    methodInformation.setEntryInstructionIDs(Analyzer.trackEntryInstructions(methodInformation, dexFile));

                    // determine the number of branches per class
                    numberOfBranches += Analyzer.trackNumberOfBranches(methodInformation);

                    // determine the register type of the param registers if the method has param registers
                    if (methodInformation.getParamRegisterCount() > 0) {
                        Analyzer.analyzeParamRegisterTypes(methodInformation, dexFile);
                    }

                    // instrument branches + if stmts
                    Instrumentation.modifyMethod(methodInformation);
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

            // write out the number of branches per class
            Utility.writeBranches(className, numberOfBranches);
        }

        // assemble modified dex files
        Utility.writeToDexFile(decodedAPKPath + File.separator + dexFileName, classes, OPCODE_API);
    }

}
