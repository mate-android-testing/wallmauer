package de.uni_passau.fim.auermich.branchdistance;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.branchdistance.analysis.Analyzer;
import de.uni_passau.fim.auermich.branchdistance.branch.Branch;
import de.uni_passau.fim.auermich.branchdistance.dto.MethodInformation;
import de.uni_passau.fim.auermich.branchdistance.instrumentation.Instrumenter;
import de.uni_passau.fim.auermich.branchdistance.utility.Utility;
import org.apache.commons.io.FileUtils;
import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.*;
import org.jf.smali.Smali;
import org.jf.smali.SmaliOptions;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
     * 1) the path to the classes.dex which should be instrumented
     * 2) the path to the instrumented classes.dex (output file)
     * 3) the package name of the application (see AndroidManifest file)
     * 4) the name of mainActivity (full path, separated by '.')
     *
     * @param args The command line arguments.
     */
    private static void handleArguments(String[] args) {

        assert args.length == 3;

        apkPath = Objects.requireNonNull(args[0]);
        packageName = Objects.requireNonNull(args[1]);
        mainActivity = Objects.requireNonNull(args[2]);

        LOGGER.info("The path to the APK file is: " + apkPath);
        LOGGER.info("The package name of the application is: " + packageName);
        LOGGER.info("The name of the MainActivity is: " + mainActivity);

        // we need to add a missing slash to the packageName
        packageName = packageName + "/";

        // convert the MainActivity to dex format
        mainActivityDex = "L" + mainActivity.replaceAll("\\.", "/") + ";";
    }

    public static void main(String[] args) throws IOException, URISyntaxException {

        LOGGER.setLevel(Level.ALL);

        if (args.length < 3) {
            LOGGER.severe("You have not specified enough arguments!");
            LOGGER.info("1. argument: path to the APK file");
            LOGGER.info("2. argument: package name of app declared in AndroidManifest file");
            LOGGER.info("3. argument: name of MainActivity declared in AndroidManifest file (FQN)");
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

            // instrument all the dex files included in the APK file
            apk.getDexEntryNames().forEach(dexFile -> {
                try {
                    instrument(apk.getEntry(dexFile).getDexFile(), dexFile, exclusionPattern);
                } catch (IOException e) {
                    LOGGER.warning("Failure loading dexFile");
                    LOGGER.warning(e.getMessage());
                }
            });

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

    private static void instrument(DexFile dexFile, String dexFileName, Pattern exclusionPattern) throws  IOException {

        LOGGER.info("Starting Instrumentation of App!");

        // the set of classes we write into the instrumented classes.dex file
        List<ClassDef> classes = Lists.newArrayList();

        // track if we found MainActivity and its onDestroy method
        boolean foundMainActivity = false;
        boolean foundOnDestroy = false;

        ClassDef mainActivity = null;

        // count total number of branches per each class
        List<Branch> branches = new LinkedList<>();

        for (ClassDef classDef : dexFile.getClasses()) {

            // the class name is part of the method id
            String className = Utility.dottedClassName(classDef.getType());

            // exclude certain packages/classes from instrumentation, e.g. android.widget.*
            if (exclusionPattern != null && exclusionPattern.matcher(className).matches()) {
                LOGGER.info("Excluding class: " + className + " from instrumentation!");
                classes.add(classDef);
                continue;
            }

            // reset flag
            boolean isMainActivity = false;

            // check whether given class is MainActivity
            if (Utility.isMainActivity(classDef, mainActivityDex)) {
                isMainActivity = true;
                foundMainActivity = true;
                mainActivity = classDef;
            }

            // the set of methods included in the instrumented classes.dex
            List<Method> methods = Lists.newArrayList();

            // track whether we modified the method or not
            boolean modifiedMethod = false;

            // reset number of branches per class
            branches.clear();

            for (Method method : classDef.getMethods()) {

                // each method is identified by its class name and method name
                String id = method.toString();

                MethodInformation methodInformation = new MethodInformation(id, classDef, method, isMainActivity);

                // check whether given method is onDestroy method of the MainActivity class
                if (isMainActivity && Utility.isOnDestroy(method)) {
                    methodInformation.setOnDestroyFlag();
                    foundOnDestroy = true;
                }

                MethodImplementation methImpl = methodInformation.getMethodImplementation();

                // check whether we can instrument given method
                if (methImpl != null && methImpl.getRegisterCount() < MAX_TOTAL_REGISTERS) {

                    LOGGER.info("Instrumenting method " + method.getName() + " of class " + classDef.toString());

                    // determine the new local registers and free register IDs
                    Instrumenter.computeRegisterStates(methodInformation,ADDITIONAL_REGISTERS);

                    // determine the number of branches per method
                    branches.addAll(Analyzer.trackBranches(methodInformation));

                    // determine the register type of the param registers if the method has param registers
                    if (methodInformation.getParamRegisterCount() > 0) {
                        Analyzer.analyzeParamRegisterTypes(methodInformation, dexFile);
                    }

                    // instrument branches + method entry and exit
                    Instrumenter.modifyMethod(methodInformation);
                    modifiedMethod = true;

                    // onDestroy need to call Tracer.write() to write branch traces to file
                    if (methodInformation.isMainActivity() && methodInformation.isOnDestroy()) {
                        Instrumenter.modifyOnDestroy(methodInformation, packageName);
                    }

                    /*
                    * We need to shift param registers by two positions to the left,
                    * e.g. move p1, p2, such that the last (two) param register(s) is/are
                    * free for use. We need two regs for wide types which span over 2 regs.
                     */
                    if (methodInformation.getParamRegisterCount() > 0) {
                        Instrumenter.shiftParamRegisters(methodInformation);
                    }

                    // add instrumented method implementation
                    Utility.addInstrumentedMethod(methods, methodInformation);

                } else {
                    // no modification necessary
                    methods.add(method);
                }
            }

            // check whether we need to insert own onDestroy method
            if (isMainActivity && !foundOnDestroy) {
                modifiedMethod = true;
                Instrumenter.insertOnDestroy(methods, classDef, packageName);
            }

            if (!modifiedMethod) {
                classes.add(classDef);
            } else {
                // add modified class including its method to the list of classes
                Utility.addInstrumentedClass(classes, methods, classDef);
            }

            // write out the number of branches per class
            Utility.writeBranches(className, branches.size());
        }

        /*
         * Calling onDestroy() requires to call super() unless it is not
         * the activity super class. Thus, we need to insert
         * a custom onDestroy() in the activity hierarchy if the super
         * class of the current activity doesn't define
         * any onDestroy() method already.
         */
        if (foundMainActivity && !foundOnDestroy) {
            classes = Instrumenter.insertOnDestroyForSuperClasses(classes, mainActivity);
        }

        LOGGER.info("Found 'MainActivity': " + foundMainActivity);
        LOGGER.info("Does 'MainActivity contains onDestroy method per default: " + foundOnDestroy);

        Utility.writeToDexFile(decodedAPKPath + File.separator + dexFileName, classes, OPCODE_API);
    }

}
