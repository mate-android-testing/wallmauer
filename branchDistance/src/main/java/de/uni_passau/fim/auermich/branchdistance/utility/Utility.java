package de.uni_passau.fim.auermich.branchdistance.utility;

import brut.androlib.Androlib;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkOptions;
import brut.common.BrutException;
import brut.directory.ExtFile;
import de.uni_passau.fim.auermich.branchdistance.dto.MethodInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.AnalyzedInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

public final class Utility {

    public static final String EXCLUSION_PATTERN_FILE = "exclude.txt";
    public static final String OUTPUT_BRANCHES_FILE = "branches.txt";

    private static final Logger LOGGER = LogManager.getLogger(Utility.class);


    private Utility() {
        throw new UnsupportedOperationException("Utility class!");
    }

    /**
     * Writes the number of branches for each class to the given file.
     * Classes without any branches are omitted.
     *
     * @param className The name of the class.
     * @param branchCounter The number of branches for a certain class.
     * @throws FileNotFoundException Should never be thrown.
     */
    public static void writeBranches(String className, int branchCounter) throws FileNotFoundException {

        File file = new File(OUTPUT_BRANCHES_FILE);
        OutputStream outputStream = new FileOutputStream(file, true);
        PrintStream printStream = new PrintStream(outputStream);

        if (branchCounter != 0) {
            // we have to save our branchCounter for the later evaluation
            printStream.println(className + ": " + branchCounter);
            printStream.flush();
        }
        printStream.close();
    }

    /**
     * Transforms a class name containing '/' into a class name with '.'
     * instead, and removes the leading 'L' as well as the ';' at the end.
     *
     * @param className The class name which should be transformed.
     * @return The transformed class name.
     */
    public static String dottedClassName(String className) {
        className = className.substring(className.indexOf('L') + 1, className.indexOf(';'));
        className = className.replace('/', '.');
        return className;
    }

    /**
     * Generates patterns of classes which should be excluded from the instrumentation.
     *
     * @return The pattern representing classes that should not be instrumented.
     * @throws IOException        If the file containing excluded classes is not available.
     * @throws URISyntaxException If the file is not present.
     */
    public static Pattern readExcludePatterns() throws IOException, URISyntaxException {

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(EXCLUSION_PATTERN_FILE);

        if (inputStream == null) {
            System.out.println("Couldn't find exlcusion file!");
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (first)
                first = false;
            else
                builder.append("|");
            builder.append(line);
        }
        reader.close();
        return Pattern.compile(builder.toString());
    }

    /**
     * Decodes a given APK using apktool.
     */
    public static String decodeAPK(File apkPath) {

        try {
            // ApkDecoder decoder = new ApkDecoder(new Androlib());
            ApkDecoder decoder = new ApkDecoder(apkPath);

            // path where we want to decode the APK
            String parentDir = apkPath.getParent();
            String outputDir = parentDir + File.separator + "decodedAPK";

            LOGGER.info("Decoding Output Dir: " + outputDir);
            decoder.setOutDir(new File(outputDir));

            // whether to decode classes.dex into smali files: -s
            decoder.setDecodeSources(ApkDecoder.DECODE_SOURCES_NONE);

            // whether to decode the AndroidManifest.xml
            // decoder.setForceDecodeManifest(ApkDecoder.FORCE_DECODE_MANIFEST_FULL);

            // whether to decode resources: -r
            // TODO: there seems to be some problem with the AndroidManifest if we don't fully decode resources
            // decoder.setDecodeResources(ApkDecoder.DECODE_RESOURCES_NONE);

            // overwrites existing dir: -f
            decoder.setForceDelete(true);

            decoder.decode();

            // the dir where the decoded content can be found
            return outputDir;
        } catch (BrutException | IOException e) {
            LOGGER.warn("Failed to decode APK file!");
            LOGGER.warn(e.getMessage());
            throw new IllegalStateException("Decoding APK failed");
        }
    }

    /**
     * Builds a given APK using apktool.
     *
     * @param decodedAPKPath The root directory of the decoded APK.
     * @param outputFile The file path of the resulting APK. If {@code null}
     *                   is specified, the default location ('dist' directory)
     *                   and the original APK name is used.
     */
    public static void buildAPK(String decodedAPKPath, File outputFile) {

        ApkOptions apkOptions = new ApkOptions();
        // apkOptions.useAapt2 = true;
        apkOptions.verbose = true;

        try {
            new Androlib(apkOptions).build(new ExtFile(new File(decodedAPKPath)), outputFile);
        } catch (BrutException e) {
            LOGGER.warn("Failed to build APK file!");
            LOGGER.warn(e.getMessage());
        }
    }

    /**
     * Writes the assembled classes to a dex file.
     *
     * @param filePath The path of the newly created dex file.
     * @param classes The classes that should be contained within the dex file.
     * @param opCode The API opcode level, e.g. API 28 (Android).
     * @throws IOException Should never happen.
     */
    public static void writeToDexFile(String filePath, List<ClassDef> classes, int opCode) throws IOException {

        DexFileFactory.writeDexFile(filePath, new DexFile() {
            @Nonnull
            @Override
            public Set<? extends ClassDef> getClasses() {
                return new AbstractSet<ClassDef>() {
                    @Nonnull
                    @Override
                    public Iterator<ClassDef> iterator() {
                        return classes.iterator();
                    }

                    @Override
                    public int size() {
                        return classes.size();
                    }
                };
            }

            @Nonnull
            @Override
            public Opcodes getOpcodes() {
                return Opcodes.forApi(opCode);
            }
        });
    }

    /**
     * Increases the register directive of the method, i.e. the .register statement at the method head
     * according to the number specified by {@param newRegisterCount}.
     *
     * @param methodInformation Stores all relevant information about a method.
     * @param newRegisterCount      The new amount of registers the method should have.
     * @throws NoSuchFieldException   Should never happen - a byproduct of reflection.
     * @throws IllegalAccessException Should never happen - a byproduct of reflection.
     * @return Returns the modified implementation.
     *
     */
    public static MethodImplementation increaseMethodRegisterCount(MethodInformation methodInformation, int newRegisterCount) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(methodImplementation);

        try {
            java.lang.reflect.Field f = mutableImplementation.getClass().getDeclaredField("registerCount");
            f.setAccessible(true);
            f.set(mutableImplementation, newRegisterCount);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        // update implementation
        methodInformation.setMethodImplementation(mutableImplementation);
        return mutableImplementation;
    }

    /**
     * Checks whether the given instruction refers to an if instruction.
     *
     * @param analyzedInstruction The instruction to be analyzed.
     * @return Returns {@code true} if the instruction is a branching instruction,
     * otherwise {@code false} is returned.
     */
    public static boolean isBranchingInstruction(AnalyzedInstruction analyzedInstruction) {
        Instruction instruction = analyzedInstruction.getInstruction();
        EnumSet<Format> branchingInstructions = EnumSet.of(Format.Format21t, Format.Format22t);
        return branchingInstructions.contains(instruction.getOpcode().format);
    }

    /**
     * Adds the modified method implementation to the list of methods that are written to
     * the instrumented dex file.
     *
     * @param methods The list of methods included in the final dex file.
     * @param methodInformation Stores all relevant information about a method.
     */
    public static void addInstrumentedMethod(List<Method> methods, MethodInformation methodInformation) {

        Method method = methodInformation.getMethod();
        MethodImplementation modifiedImplementation = methodInformation.getMethodImplementation();

        methods.add(new ImmutableMethod(
                method.getDefiningClass(),
                method.getName(),
                method.getParameters(),
                method.getReturnType(),
                method.getAccessFlags(),
                method.getAnnotations(),
                modifiedImplementation));
    }

    /**
     * Adds the given class (#param classDef} including its method to the list of classes
     * that are part of the final dex file.
     *
     * @param classes The list of classes part of the final dex file.
     * @param methods The list of methods belonging to the given class.
     * @param classDef The class we want to add.
     */
    public static void addInstrumentedClass(List<ClassDef> classes, List<Method> methods, ClassDef classDef) {

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

    /**
     * Checks whether the given class represents an activity by checking against the super class.
     *
     * @param classes The set of classes.
     * @param currentClass The class to be inspected.
     * @return Returns {@code true} if the current class is an activity,
     *          otherwise {@code false}.
     */
    public static boolean isActivity(List<ClassDef> classes, ClassDef currentClass) {

        // TODO: this approach might be quite time-consuming, may find a better solution

        String superClass = currentClass.getSuperclass();
        boolean abort = false;

        while (!abort && superClass != null && !superClass.equals("Ljava/lang/Object;")) {

            abort = true;

            if (superClass.equals("Landroid/app/Activity;")
                    || superClass.equals("Landroidx/appcompat/app/AppCompatActivity;")
                    || superClass.equals("Landroid/support/v7/app/AppCompatActivity;")
                    || superClass.equals("Landroid/support/v7/app/ActionBarActivity;")
                    || superClass.equals("Landroid/support/v4/app/FragmentActivity;")) {
                return true;
            } else {
                // step up in the class hierarchy
                for (ClassDef classDef : classes) {
                    if (classDef.toString().equals(superClass)) {
                        superClass = classDef.getSuperclass();
                        abort = false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the given class represents a fragment by checking against the super class.
     *
     * @param classes The set of classes.
     * @param currentClass The class to be inspected.
     * @return Returns {@code true} if the current class is a fragment,
     *          otherwise {@code false}.
     */
    public static boolean isFragment(List<ClassDef> classes, ClassDef currentClass) {

        // TODO: this approach might be quite time-consuming, may find a better solution

        String superClass = currentClass.getSuperclass();
        boolean abort = false;

        while (!abort && superClass != null && !superClass.equals("Ljava/lang/Object;")) {

            abort = true;

            // https://developer.android.com/reference/android/app/Fragment
            if (superClass.equals("Landroid/app/Fragment;")
                    || superClass.equals("Landroidx/fragment/app/Fragment;")
                    || superClass.equals("Landroid/support/v4/app/Fragment;")
                    || superClass.equals("Landroid/app/DialogFragment;")
                    || superClass.equals("Landroid/app/ListFragment;")
                    || superClass.equals("Landroid/preference/PreferenceFragment;")
                    || superClass.equals("Landroid/webkit/WebViewFragment;")) {
                return true;
            } else {
                // step up in the class hierarchy
                for (ClassDef classDef : classes) {
                    if (classDef.toString().equals(superClass)) {
                        superClass = classDef.getSuperclass();
                        abort = false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the set of activity lifecycle methods.
     *
     * @return Returns the method names of the activity lifecycle methods.
     */
    public static Set<String> getActivityLifeCycleMethods() {
        Set<String> activityLifeCycleMethods = new HashSet<>();
        activityLifeCycleMethods.add("onCreate(Landroid/os/Bundle;)V");
        activityLifeCycleMethods.add("onStart()V");
        activityLifeCycleMethods.add("onResume()V");
        activityLifeCycleMethods.add("onPause()V");
        activityLifeCycleMethods.add("onStop()V");
        activityLifeCycleMethods.add("onDestroy()V");
        activityLifeCycleMethods.add("onRestart()V");
        return activityLifeCycleMethods;
    }

    /**
     * Returns the set of fragment lifecycle methods.
     *
     * @return Returns the method names of the fragment lifecycle methods.
     */
    public static Set<String> getFragmentLifeCycleMethods() {

        // TODO: add the deprecated onAttach method

        Set<String> fragmentLifeCycleMethods = new HashSet<>();
        fragmentLifeCycleMethods.add("onAttach(Landroid/content/Context;)V");
        fragmentLifeCycleMethods.add("onCreate(Landroid/os/Bundle;)V");
        fragmentLifeCycleMethods.add("onCreateView(Landroid/view/LayoutInflater;"
                + "Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;");
        fragmentLifeCycleMethods.add("onActivityCreated(Landroid/os/Bundle;)V");
        fragmentLifeCycleMethods.add("onViewStateRestored(Landroid/os/Bundle;)V");
        fragmentLifeCycleMethods.add("onDestroyView()V");
        fragmentLifeCycleMethods.add("onDestroy()V");
        fragmentLifeCycleMethods.add("onDetach()V");
        return fragmentLifeCycleMethods;
    }

}
