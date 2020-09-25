package de.uni_passau.fim.auermich.branchcoverage;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.branchcoverage.analysis.Analyzer;
import de.uni_passau.fim.auermich.branchcoverage.dto.MethodInformation;
import de.uni_passau.fim.auermich.branchcoverage.instrumentation.Instrumentation;
import de.uni_passau.fim.auermich.branchcoverage.utility.Utility;
import de.uni_passau.fim.auermich.branchcoverage.xml.ManifestParser;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
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
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class BranchCoverage {

    // the logger instance
    private static final Logger LOGGER = LogManager.getLogger(BranchCoverage.class);

    // the package name declared in the AndroidManifest file
    public static String packageName;

    // the name of the mainActivity declared in the AndroidManifest file
    public static String mainActivity;

    // the path to the APK file
    public static String apkPath;

    // the output path of the decoded APK
    public static String decodedAPKPath;

    // dex op code specified in header of classes.dex file
    public static int OPCODE_API = 28;

    /*
     * Defines the number of additional registers. We require one additional register
     * for storing the unique branch id. We require a second register when shifting
     * wide types.
     */
    public static final int ADDITIONAL_REGISTERS = 2;

    // the dex conform mainActivity name, uses '/' instead of '.'
    public static String mainActivityDex;

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
     * Instruments a given APK file with branch coverage information.
     *
     * @param args The path to the APK file.
     * @throws IOException        Should never happen.
     * @throws URISyntaxException Should never happen.
     */
    public static void main(String[] args) throws IOException, URISyntaxException {

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

            // process directly apk file (support for multi-dex)
            MultiDexContainer<? extends DexBackedDexFile> apk
                    = DexFileFactory.loadDexContainer(apkFile, null);

            // decode the APK file
            decodedAPKPath = Utility.decodeAPK(apkFile);

            ManifestParser manifest = new ManifestParser(decodedAPKPath + File.separator + "AndroidManifest.xml");

            // retrieve package name and main activity
            if (!manifest.parseManifest()) {
                LOGGER.warn("Couldn't retrieve MainActivity and/or PackageName!");
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
                    LOGGER.warn("Failure loading dexFile");
                    LOGGER.warn(e.getMessage());
                }
            });

            // add broadcast receiver tag into AndroidManifest
            if (!manifest.addBroadcastReceiverTag(
                    "de.uni_passau.fim.auermich.branchcoverage.tracer.Tracer",
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

            // we insert into the last classes.dex file our tracer functionality

            // the path to the last dex file, e.g. classes3.dex
            String lastDexFile = decodedAPKPath + File.separator
                    + apk.getDexEntryNames().get(apk.getDexEntryNames().size() - 1);

            // the output directory for baksmali d
            File smaliFolder = new File(decodedAPKPath + File.separator + "out");

            // baksmali d classes.dex -o out
            Baksmali.disassembleDexFile(DexFileFactory.loadDexFile(lastDexFile,
                    Opcodes.forApi(OPCODE_API)), smaliFolder, 1, new BaksmaliOptions());

            // the location of the tracer directory
            File tracerFolder = Paths.get(smaliFolder.getAbsolutePath(), "de", "uni_passau",
                    "fim", "auermich", "branchcoverage", "tracer").toFile();
            // TODO: verify that mkdirs doesn't overwrite pre-existing sub-directories/files
            tracerFolder.mkdirs();

            // copy from resource folder Tracer.smali to smali folder
            InputStream inputStream = BranchCoverage.class.getClassLoader().getResourceAsStream("Tracer.smali");
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

        LOGGER.info("Dex version: " + dexFile.getOpcodes().api);

        // set the opcode api level
        OPCODE_API = dexFile.getOpcodes().api;

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

            // the set of methods included in the instrumented classes.dex
            List<Method> methods = Lists.newArrayList();

            // track whether we modified the method or not
            boolean modifiedMethod = false;

            // count the number of branches per class
            int numberOfBranches = 0;

            for (Method method : classDef.getMethods()) {

                // each method is identified by its class name and method name
                String id = method.toString();

                MethodInformation methodInformation = new MethodInformation(id, classDef, method, dexFile);
                MethodImplementation methImpl = methodInformation.getMethodImplementation();

                /* We can only instrument methods with a given register count because
                 * our instrumentation code uses instructions that only the usage of
                 * registers with a register ID < MAX_TOTAL_REGISTERS, i.e. the newly
                 * inserted registers aren't allowed to exceed this limit.
                 */
                if (methImpl != null && methImpl.getRegisterCount() < MAX_TOTAL_REGISTERS) {

                    LOGGER.info("Instrumenting method " + method + " of class " + classDef.toString());

                    // determine the new local registers and free register IDs
                    Analyzer.computeRegisterStates(methodInformation,ADDITIONAL_REGISTERS);

                    // determine the location of the branches
                    methodInformation.setInstrumentationPoints(Analyzer.trackInstrumentationPoints(methodInformation));

                    // determine the location of try blocks
                    methodInformation.setTryBlocks(Analyzer.getTryBlocks(methodInformation));

                    // determine the number of branches per class
                    numberOfBranches += Analyzer.trackNumberOfBranches(methodInformation);

                    // determine the register type of the param registers if the method has param registers
                    if (methodInformation.getParamRegisterCount() > 0) {
                        Analyzer.analyzeParamRegisterTypes(methodInformation, dexFile);
                    }

                    // instrument branches
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

                } else {
                    // no modification necessary
                    methods.add(method);
                }
            }

            if (!modifiedMethod) {
                classes.add(classDef);
            } else {
                // add modified class including its method to the list of classes
                Utility.addInstrumentedClass(classes, methods, classDef);
            }

            // write out the number of branches per class
            Utility.writeBranches(classDef.getType(), numberOfBranches);
        }

        // assemble modified dex files
        Utility.writeToDexFile(decodedAPKPath + File.separator + dexFileName, classes, OPCODE_API);
    }
}
