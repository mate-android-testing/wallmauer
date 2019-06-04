package de.uni_passau.fim.branchcoverage2;

import com.google.common.collect.Lists;
import de.uni_passau.fim.branchcoverage.*;
import de.uni_passau.fim.utility.Utility;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.*;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.util.MethodUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class BranchCoverage {

    /* PARAMS SECTION BEGIN */

    // the package name declared in the AndroidManifest file
    public static String packageName;

    // the name of the mainActivity declared in the AndroidManifest file
    public static String mainActivity;

    // the path to the classes.dex file
    public static String dexInputPath;

    // the path where the instrumented dex file should reside
    public static String dexOutputPath;

    /* PARAMS SECTION END */

    // dex op code specified in header of classes.dex file
    public static final int OPCODE_API = 28;

    // the number of additional required registers (two for wide-types required when moving)
    public static final int ADDITIONAL_REGISTERS = 2;

    // the dex conform mainActivity name, uses '/' instead of '.'
    public static String mainActivityDex;

    // the logger instance
    private static final Logger LOGGER = Logger.getLogger(BranchCoverage.class
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
    public static void handleArguments(String[] args) {

        assert args.length == 5;

        dexInputPath = Objects.requireNonNull(args[0]);
        dexOutputPath = Objects.requireNonNull(args[1]);
        packageName = Objects.requireNonNull(args[2]);
        mainActivity = Objects.requireNonNull(args[3]);

        LOGGER.info("The input path of the classes.dex file is: " + dexInputPath);
        LOGGER.info("The output path of the instrumented classes.dex file is: " + dexOutputPath);
        LOGGER.info("The package name of the application is: " + packageName);
        LOGGER.info("The name of the MainActivity is: " + mainActivity);

        // we need to add a missing slash to the packageName
        packageName = packageName + "/";

        // convert the MainActivity to dex format
        mainActivityDex = "L" + mainActivity.replaceAll("\\.", "/") + ";";
    }

    public static void main(String[] args) throws IOException, URISyntaxException {

        LOGGER.setLevel(Level.ALL);

        if (args.length < 5) {
            LOGGER.severe("You have specified not enough arguments!");
            LOGGER.info("1. argument: path to classes.dex file");
            LOGGER.info("2. argument: path to instrumented classes.dex file");
            LOGGER.info("3. argument: package name of app declared in AndroidManifest file");
            LOGGER.info("4. argument: name of MainActivity declared in AndroidManifest file (full path)");
        } else {

            // describes class names we want to exclude from instrumentation
            Pattern exclusionPattern = Utility.readExcludePatterns();

            // loads the classes.dex file
            DexFile dexFile = DexFileFactory.loadDexFile(dexInputPath, Opcodes.forApi(OPCODE_API));

            instrument(dexFile, exclusionPattern);
        }
    }

    public static void instrument(DexFile dexFile, Pattern exclusionPattern) throws  IOException {

        LOGGER.info("Starting Instrumentation of App!");

        // the set of classes we write into the instrumented classes.dex file
        final List<ClassDef> classes = Lists.newArrayList();

        // track if we found MainActivity and its onDestroy method
        boolean foundMainActivity = false;
        boolean foundOnDestroy = false;

        // collect locations of branches
        Map<Integer, Branch> branches = new HashMap<>();

        // activity super class, e.g. AppCompatActivity, Activity or ...
        String superClass = null;

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
            }

            // the set of methods included in the instrumented classes.dex
            List<Method> methods = Lists.newArrayList();

            // track whether we modified the method or not
            boolean modifiedMethod = false;

            for (Method method : classDef.getMethods()) {

                // each method is identified by its class name and method name
                String id = className + "->" + method.getName();

                MethodInformation methodInformation = new MethodInformation(id, classDef, method, isMainActivity);

                // check whether given method is onDestroy method of the MainActivity class
                if (isMainActivity && Utility.isOnDestroy(method)) {
                    methodInformation.setOnDestroyFlag();
                    foundOnDestroy = true;
                }

                Optional<MethodImplementation> methImpl = methodInformation.getImplementation();

                // check whether we can and need to instrument given method
                if (methImpl.isPresent() && Instrumenter.methodNeedsModification(methImpl.get())
                        || methodInformation.isOnDestroy() && methImpl.get().getRegisterCount() < MAX_TOTAL_REGISTERS) {

                    LOGGER.info("Instrumenting method " + method.getName() + " of class " + classDef.toString());

                    // determine the new local registers and free register IDS
                    Instrumenter.computeRegisterStates(methodInformation,ADDITIONAL_REGISTERS);

                    // determine the location of the branch instructions (if,else)
                    Analyzer.trackBranchLocations(methodInformation);

                    // determine the register type of the param registers if the method has param registers
                    if (methodInformation.getParamRegisterCount() > 0) {
                        Analyzer.analyzeParamRegisterTypes(methodInformation, dexFile);
                    }

                    // we need to track the instructions we inserted, these are irrelevant for the later register substitution
                    Set<BuilderInstruction> insertedInstructions = new HashSet<>();

                    // instrument branches
                    MethodImplementation modifiedImplementation =
                            Instrumenter.modifyMethod(methodInformation);

                    modifiedMethod = true;

                    // onDestroy need to call Tracer.write() to write branch traces to file
                    if (methodInformation.isMainActivity() && methodInformation.isOnDestroy()) {
                        modifiedImplementation = Instrumenter.modifyOnDestroy(modifiedImplementation, packageName, insertedInstructions);
                    }

                    /*
                    * We need to shift param registers by two positions to the left,
                    * e.g. move p1, p2, such that the last param register is free
                    * for use.
                     */
                    if (methodInformation.getParamRegisterCount() > 0) {
                        modifiedImplementation = Instrumenter.shiftParamRegisters(methodInformation);
                    }

                    methods.add(new ImmutableMethod(
                            method.getDefiningClass(),
                            method.getName(),
                            method.getParameters(),
                            method.getReturnType(),
                            method.getAccessFlags(),
                            method.getAnnotations(),
                            modifiedImplementation));
                } else {
                    // no modification necessary
                    methods.add(method);
                }
            }

            // check whether we need to insert own onDestroy method
            if (isMainActivity && !foundOnDestroy) {
                modifiedMethod = true;

                superClass = classDef.getSuperclass();
                LOGGER.info("Super class of MainActivity: " + superClass);

                // we need to determine the activity super class
                while (superClass != null && !superClass.equals("Landroid/app/Activity;")
                        && !superClass.equals("Landroid/support/v7/app/AppCompatActivity;")
                        && !superClass.equals("Landroid/support/v7/app/ActionBarActivity;")
                        && !superClass.equals("Landroid.support.v4.app.FragmentActivity;")) {
                    // iterate over classDef and follow link of superClass until we reach a suitable one
                    for (ClassDef classesDef : classes) {
                        if (classesDef.toString().equals(superClass)) {
                            superClass = classesDef.getSuperclass();
                            break;
                        }
                    }
                }

                methods.add(new ImmutableMethod(
                        classDef.toString(),
                        "onDestroy",
                        null,
                        "V",
                        4,
                        null,
                        InstrumenterFinal.insertOnDestroy(packageName, superClass)));
            }

            if (!modifiedMethod) {
                classes.add(classDef);
            } else {
                classes.add(new ImmutableClassDef(
                        classDef.getType(),
                        classDef.getAccessFlags(),
                        classDef.getSuperclass(),
                        classDef.getInterfaces(),
                        classDef.getSourceFile(),
                        classDef.getAnnotations(),
                        classDef.getFields(),
                        methods));
            }

            // write out the number of branches per class
            Utility.writeBranches(className, branches.size());
        }

        LOGGER.info("Found 'MainActivity': " + foundMainActivity);
        LOGGER.info("Does 'MainActivity contains onDestroy method per default: " + foundOnDestroy);

        // we want to create a new DexFile containing the modified code
        Utility.writeToDexFile(dexOutputPath, classes, OPCODE_API);
    }

}
