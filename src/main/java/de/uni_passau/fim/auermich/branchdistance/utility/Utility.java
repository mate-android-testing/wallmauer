package de.uni_passau.fim.auermich.branchdistance.utility;

import brut.androlib.Androlib;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkOptions;
import brut.common.BrutException;
import brut.directory.ExtFile;
import de.uni_passau.fim.auermich.branchdistance.BranchDistance;
import de.uni_passau.fim.auermich.branchdistance.dto.MethodInformation;
import org.apache.commons.io.FileUtils;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;


import javax.annotation.Nonnull;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public final class Utility {

    public static final String EXCLUSION_PATTERN_FILE = "exclude.txt";
    public static final String OUTPUT_BRANCHES_FILE = "branches.txt";

    // the logger instance
    private static final Logger LOGGER = Logger.getLogger(Utility.class
            .getName());

    private Utility() {
        throw new UnsupportedOperationException("Utility class!");
    }

    /**
     * Checks whether we can find the 'MainActivity' class in the set of given classes.
     *
     * @param classes The set of classes contained in the classes.dex file.
     * @param mainActivity The name of the 'MainActivity'.
     * @return Returns @{code true} if the 'MainActivity is included, otherwise {@code false}.
     */
    public static boolean containsMainActivity(Set<? extends ClassDef> classes, String mainActivity) {

        for (ClassDef classDef: classes) {
            if (classDef.toString().equals(mainActivity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the 'MainActivity' class declares an 'onDestroy' method.
     *
     * @param mainClass The 'MainActivity' class.
     * @return Returns @{code true} if the onDestroy method is declared, otherwise {@code false}.
     */
    public static boolean containsOnDestroy(ClassDef mainClass) {

        for (Method method : mainClass.getMethods()) {
            if (method.getName().equals("onDestroy")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMainActivity(ClassDef classDef, String mainActivity) {
        return classDef.toString().equals(mainActivity);
    }

    public static boolean isOnDestroy(Method method) {
        return method.getName().equals("onDestroy");
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
            LOGGER.warning("Failed to decode APK file!");
            LOGGER.warning(e.getMessage());
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
        apkOptions.useAapt2 = true;
        apkOptions.verbose = true;

        try {
            new Androlib(apkOptions).build(new ExtFile(new File(decodedAPKPath)), outputFile);
        } catch (BrutException e) {
            LOGGER.warning("Failed to build APK file!");
            LOGGER.warning(e.getMessage());
        }
    }

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
     * Increasing the amount of registers for a method requires shifting
     * certain registers back to their original position. In particular, all
     * registers, which register id is bigger or equal than the new specified
     * register count {@param registerNumber}, need to be shifted (increased) by the amount of the newly
     * created registers. Especially, originally param registers are affected
     * by increasing the register count, and would be treated now as a local register
     * without re-ordering (shifting).
     *
     * @param instruction    The instruction that is currently inspected.
     * @param registerNumber Specifies a lower limit for registers, which need to be considered.
     * @throws NoSuchFieldException   Should never happen, constitutes a byproduct of using reflection.
     * @throws IllegalAccessException Should never happen, constitutes a byproduct of using reflection.
     */
    public static void reOrderRegister(BuilderInstruction instruction, int registerNumber, int shift)
            throws NoSuchFieldException, IllegalAccessException {

        // those invoke range instructions require a special treatment, since they don't have fields containing the registers
        if (instruction instanceof BuilderInstruction3rc) {

            // those instructions store the number of registers (var registerCount) and the first register of these range (var startRegister)
            int registerStart = ((BuilderInstruction3rc) instruction).getStartRegister();
            java.lang.reflect.Field f = instruction.getClass().getDeclaredField("startRegister");
            if (registerStart >= registerNumber) {
                f.setAccessible(true);
                f.set(instruction, registerStart + shift);
            }
            return;
        }

        java.lang.reflect.Field[] fields = instruction.getClass().getDeclaredFields();

        for (java.lang.reflect.Field field : fields) {
            // all fields are labeled registerA - registerG
            if (field.getName().startsWith("register") && !field.getName().equals("registerCount")) {
                field.setAccessible(true);
                // System.out.println(field.getName());
                try {
                    int value = field.getInt(instruction);

                    if (value >= registerNumber)
                        field.set(instruction, value + shift);

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
