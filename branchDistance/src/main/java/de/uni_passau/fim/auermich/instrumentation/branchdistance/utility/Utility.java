package de.uni_passau.fim.auermich.instrumentation.branchdistance.utility;

import brut.androlib.Androlib;
import brut.androlib.ApkDecoder;
import brut.androlib.options.BuildOptions;
import brut.common.BrutException;
import brut.directory.ExtFile;
import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.BranchDistance;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.core.InstrumentationPoint;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.dto.MethodInformation;
import lanchon.multidexlib2.BasicDexFileNamer;
import lanchon.multidexlib2.DexIO;
import lanchon.multidexlib2.MultiDexIO;
import org.antlr.runtime.RecognitionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.AnalyzedInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.dexbacked.value.DexBackedTypeEncodedValue;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.smali.SmaliTestUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.regex.Pattern;

public final class Utility {

    public static final String EXCLUSION_PATTERN_FILE = "exclude.txt";
    public static final String OUTPUT_BRANCHES_FILE = "branches.txt";
    public static final String OUTPUT_INSTRUMENTATION_POINTS_FILE = "instrumentation-points.txt";

    private static final Logger LOGGER = LogManager.getLogger(Utility.class);

    /**
     * It seems that certain resource classes are API dependent, e.g.
     * "R$interpolator" is only available in API 21.
     */
    private static final Set<String> resourceClasses = new HashSet<String>() {{
        add("R$anim");
        add("R$attr");
        add("R$bool");
        add("R$color");
        add("R$dimen");
        add("R$drawable");
        add("R$id");
        add("R$integer");
        add("R$layout");
        add("R$mipmap");
        add("R$string");
        add("R$style");
        add("R$styleable");
        add("R$interpolator");
        add("R$menu");
        add("R$array");
    }};

    /**
     * The methods contained in the java.lang.Object class.
     */
    private static final Set<String> JAVA_OBJECT_METHODS = new HashSet<>() {{
        add("hashCode()I");
        add("equals(Ljava/lang/Object;)Z");
        add("getClass()Ljava/lang/Class;");
        add("clone()Ljava/lang/Object;");
        add("toString()Ljava/lang/String;");
        add("notify()V");
        add("notifyAll()V");
        add("wait(J)V");
        add("wait(JI)V");
        add("wait()V");
        add("finalize()V");
    }};

    /**
     * The recognized ART methods excluding component invocation methods, e.g. startActivity().
     */
    private static final Set<String> ART_METHODS = new HashSet<>() {{
        add("findViewById(I)Landroid/view/View;");
        add("setContentView(I)V");
        add("setContentView(Landroid/view/View;)V");
        add("setContentView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V");
        add("getSupportFragmentManager()Landroid/support/v4/app/FragmentManager;");
        add("addContentView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V");
        add("getMenuInflater()Landroid/view/MenuInflater;");
        add("invalidateOptionsMenu()V");
        add("writeToParcel(Landroid/os/Parcel;I)V");
    }};

    private Utility() {
        throw new UnsupportedOperationException("Utility class!");
    }

    /**
     * Checks whether the given class represents the dynamically generated R class or any
     * inner class of it.
     *
     * @param classDef The class to be checked.
     * @return Returns {@code true} if the given class represents the R class or any
     * inner class of it, otherwise {@code false} is returned.
     */
    public static boolean isResourceClass(ClassDef classDef) {

        String className = Utility.dottedClassName(classDef.toString());

        String[] tokens = className.split("\\.");

        // check whether it is the R class itself
        if (tokens[tokens.length - 1].equals("R")) {
            return true;
        }

        // check for inner R classes
        for (String resourceClass : resourceClasses) {
            if (className.contains(resourceClass)) {
                return true;
            }
        }

        // TODO: can be removed, just for illustration how to process annotations
        Set<? extends Annotation> annotations = classDef.getAnnotations();

        for (Annotation annotation : annotations) {

            // check if the enclosing class is the R class
            if (annotation.getType().equals("Ldalvik/annotation/EnclosingClass;")) {
                for (AnnotationElement annotationElement : annotation.getElements()) {
                    if (annotationElement.getValue() instanceof DexBackedTypeEncodedValue) {
                        DexBackedTypeEncodedValue value = (DexBackedTypeEncodedValue) annotationElement.getValue();
                        if (value.getValue().equals("Landroidx/appcompat/R;")) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks whether the given class represents the dynamically generated BuildConfig class.
     *
     * @param classDef The class to be checked.
     * @return Returns {@code true} if the given class represents the dynamically generated
     * BuildConfig class, otherwise {@code false} is returned.
     */
    public static boolean isBuildConfigClass(ClassDef classDef) {
        String className = Utility.dottedClassName(classDef.toString());
        // TODO: check solely the last token (the actual class name)
        return className.endsWith("BuildConfig");
    }

    /**
     * Loads the tracer functionality directly from smali files.
     *
     * @param apiLevel The api opcode level.
     * @return Returns the classes representing the tracer.
     */
    public static List<ClassDef> loadTracer(int apiLevel) {
        List<ClassDef> tracerClasses = new ArrayList<>();
        tracerClasses.add(loadClass(apiLevel, "Tracer.smali"));
        tracerClasses.add(loadClass(apiLevel, "Tracer$1.smali"));
        return tracerClasses;
    }

    /**
     * Loads a smali class from the resources folder.
     *
     * @param apiLevel The api level of the smali class.
     * @param className The smali class name.
     * @return Returns the loaded class.
     */
    public static ClassDef loadClass(int apiLevel, String className) {

        InputStream inputStream = BranchDistance.class.getClassLoader().getResourceAsStream(className);

        ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return inputStream;
            }
        };

        try {
            String smaliCode = byteSource.asCharSource(Charsets.UTF_8).read();
            return SmaliTestUtils.compileSmali(smaliCode, apiLevel);
        } catch (IOException | RecognitionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the super classes of the given class.
     *
     * @param dexFile The dex file containing all classes.
     * @param classDef The class for which we look up its super classes.
     * @return Returns the super classes of the given class if present in the dex file.
     */
    public static List<ClassDef> getSuperClasses(DexFile dexFile, ClassDef classDef) {

        List<ClassDef> superClasses = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(classDef.getSuperclass());

        while (!queue.isEmpty()) {
            String superClass = queue.poll();

            if (superClass == null || superClass.equals("Ljava/lang/Object;")) {
                break;
            }

            // try to find super class in dex file
            for (ClassDef clazz : dexFile.getClasses()) {
                if (clazz.toString().equals(superClass)) {
                    // found super class, look up its super class
                    superClasses.add(clazz);
                    queue.offer(clazz.getSuperclass());
                    break;
                }
            }
        }

        return superClasses;
    }

    /**
     * Writes out the branches and instrumentation points of the given method that could be instrumented.
     *
     * @param methodInformation Encapsulates a method.
     */
    public static synchronized void writeBranchesAndInstrumentationPoints(MethodInformation methodInformation) {

        File branchesFile = new File(OUTPUT_BRANCHES_FILE);
        File instrumentationPoints = new File(OUTPUT_INSTRUMENTATION_POINTS_FILE);

        try (PrintStream branches = new PrintStream(new FileOutputStream(branchesFile, true));
             PrintStream ips = new PrintStream(new FileOutputStream(instrumentationPoints, true));
        ) {
            for (InstrumentationPoint instrumentationPoint : methodInformation.getInstrumentationPoints()) {

                final String trace = methodInformation.getMethodID() + "->" + instrumentationPoint.getPosition();
                ips.println(trace);
                if (instrumentationPoint.getType() == InstrumentationPoint.Type.IF_BRANCH
                        || instrumentationPoint.getType() == InstrumentationPoint.Type.ELSE_BRANCH) {
                    branches.println(trace);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Couldn't write branches or instrumentation points to file!");
            throw new IllegalStateException("Couldn't write branches or instrumentation points to file!", e);
        }
    }

    /**
     * Writes out the branches of the given method that could be instrumented.
     *
     * @param methodInformation Encapsulates a method.
     */
    @SuppressWarnings("unused")
    public static synchronized void writeBranches(MethodInformation methodInformation) {

        File file = new File(OUTPUT_BRANCHES_FILE);

        try (OutputStream outputStream = new FileOutputStream(file, true);
             PrintStream printStream = new PrintStream(outputStream)) {

            for (InstrumentationPoint instrumentationPoint : methodInformation.getInstrumentationPoints()) {

                if (instrumentationPoint.getType() == InstrumentationPoint.Type.IF_BRANCH
                        || instrumentationPoint.getType() == InstrumentationPoint.Type.ELSE_BRANCH) {
                    String trace = methodInformation.getMethodID() + "->" + instrumentationPoint.getPosition();
                    printStream.println(trace);
                }
            }

            printStream.flush();
        } catch (IOException e) {
            LOGGER.error("Couldn't write branches to branches.txt");
            throw new IllegalStateException("Couldn't write branches to branches.txt");
        }
    }

    /**
     * Writes the number of branches for each class to the given file. Classes without any branches are omitted.
     *
     * @param className The name of the class.
     * @param branchCounter The number of branches for a certain class.
     * @throws FileNotFoundException Should never be thrown.
     */
    @SuppressWarnings("unusued")
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
     */
    public static Pattern readExcludePatterns() throws IOException {

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(EXCLUSION_PATTERN_FILE);

        if (inputStream == null) {
            LOGGER.info("Couldn't find exclusion file!");
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
    public static File decodeAPK(File apkPath) {

        // set 3rd party library (apktool) logging to 'SEVERE'
        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        rootLogger.setLevel(Level.SEVERE);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(Level.SEVERE);
        }

        ApkDecoder decoder = new ApkDecoder(apkPath);

        // path where we want to decode the APK (the same directory as the APK)
        File parentDir = apkPath.getParentFile();
        File outputDir = new File(parentDir, "decodedAPK");

        LOGGER.info("Decoding Output Dir: " + outputDir);
        decoder.setOutDir(outputDir);

        // overwrites existing dir: -f
        decoder.setForceDelete(true);

        try {

            // whether to decode classes.dex into smali files: -s
            decoder.setDecodeSources(ApkDecoder.DECODE_SOURCES_NONE);

            /*
             * Although the decoding of only the AndroidManifest works and even
             * the building of the APK works, the installing of the APK fails.
             * See https://github.com/iBotPeaches/Apktool/issues/2374 for more details.
             * Thus, we need to fully decode the resources right now. Hopefully
             * this bug can be fixed in the future, decoding/encoding resources is a
             * time consuming task.
             */

            // whether to decode the AndroidManifest.xml
            // decoder.setForceDecodeManifest(ApkDecoder.FORCE_DECODE_MANIFEST_FULL);

            // whether to decode resources: -r
            // TODO: there seems to be some problem with the AndroidManifest if we don't fully decode resources
            // decoder.setDecodeResources(ApkDecoder.DECODE_RESOURCES_NONE);

            decoder.decode();
            decoder.close();

            // the dir where the decoded content can be found
            return outputDir;
        } catch (BrutException | IOException e) {
            LOGGER.warn("Failed to decode APK file!");
            LOGGER.warn(e.getMessage());
            throw new IllegalStateException(e);
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
    public static void buildAPK(File decodedAPKPath, File outputFile) {

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.useAapt2 = true;
        buildOptions.verbose = true;
        // apkOptions.forceBuildAll = true;

        try {
            new Androlib(buildOptions).build(new ExtFile(decodedAPKPath), outputFile);
        } catch (BrutException e) {
            LOGGER.warn("Failed to build APK file!");
            LOGGER.warn(e.getMessage());
        }
    }

    /**
     * Writes a merged dex file to a directory. Under the scene, the dex file is split
     * into multiple dex files if the method reference limit would be violated.
     *
     * @param filePath The directory where the dex files should be written to.
     * @param classes The classes that should be contained within the dex file.
     * @param opCode The API opcode level, e.g. API 28 (Android).
     * @throws IOException Should never happen.
     */
    public static void writeMultiDexFile(File filePath, List<ClassDef> classes, int opCode) throws IOException {

        // TODO: directly update merged dex file instance instead of creating new dex file instance here
        DexFile dexFile = new DexFile() {
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
                // https://android.googlesource.com/platform/dalvik/+/master/dx/src/com/android/dex/DexFormat.java
                return Opcodes.forApi(opCode);
            }
        };

        MultiDexIO.writeDexFile(true, filePath, new BasicDexFileNamer(),
                dexFile, DexIO.DEFAULT_MAX_DEX_POOL_SIZE, null);
    }

    /**
     * Writes the assembled classes to a single dex file.
     *
     * @param filePath The path of the newly created dex file.
     * @param classes The classes that should be contained within the dex file.
     * @param opCode The API opcode level, e.g. API 28 (Android).
     * @throws IOException Should never happen.
     */
    @SuppressWarnings("unused")
    public static void writeToDexFile2(String filePath, List<ClassDef> classes, int opCode) throws IOException {

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
    @SuppressWarnings("unused")
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
                null,
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
    public static boolean isActivity(Set<? extends ClassDef> classes, ClassDef currentClass) {

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
    public static boolean isFragment(Set<? extends ClassDef> classes, ClassDef currentClass) {

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

    /**
     * Retrieves solely the method name from the full-qualified method signature.
     *
     * @param methodSignature The given method signature.
     * @return Returns the method name from the given method signature.
     */
    public static String getMethodName(String methodSignature) {
        return methodSignature.split("->")[1];
    }

    /**
     * Checks whether the given method is an inherited method from the java.lang.Object class.
     *
     * @param fullyQualifiedMethodName The method signature.
     * @return Returns {@code true} if the given method is a java.lang.Object method,
     * otherwise {@code false}.
     */
    public static boolean isJavaObjectMethod(final String fullyQualifiedMethodName) {
        String method = getMethodName(fullyQualifiedMethodName);
        return JAVA_OBJECT_METHODS.contains(method);
    }

    /**
     * Checks whether the given method is an (inherited) ART method, e.g. setContentView().
     *
     * @param fullyQualifiedMethodName The method signature.
     * @return Returns {@code true} if the given method is an ART method,
     * otherwise {@code false}.
     */
    public static boolean isARTMethod(final String fullyQualifiedMethodName) {
        String method = getMethodName(fullyQualifiedMethodName);
        return ART_METHODS.contains(method);
    }
}
