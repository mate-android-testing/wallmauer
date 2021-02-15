package de.uni_passau.fim.auermich.branchcoverage.utility;

import brut.androlib.Androlib;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkOptions;
import brut.common.BrutException;
import brut.directory.ExtFile;
import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import de.uni_passau.fim.auermich.branchcoverage.BranchCoverage;
import de.uni_passau.fim.auermich.branchcoverage.dto.MethodInformation;
import lanchon.multidexlib2.BasicDexFileNamer;
import lanchon.multidexlib2.DexIO;
import lanchon.multidexlib2.MultiDexIO;
import org.antlr.runtime.RecognitionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.jf.smali.SmaliTestUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URISyntaxException;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class Utility {

    public static final String EXCLUSION_PATTERN_FILE = "exclude.txt";
    public static final String OUTPUT_BRANCHES_FILE = "branches.txt";

    private static final Logger LOGGER = LogManager.getLogger(Utility.class);

    private Utility() {
        throw new UnsupportedOperationException("Utility class!");
    }

    /**
     * Loads the tracer functionality directly from a smali file.
     *
     * @param apiLevel The api opcode level.
     * @return Returns a class def representing the tracer smali file.
     */
    public static ClassDef loadTracer(int apiLevel) {

        InputStream inputStream = BranchCoverage.class.getClassLoader().getResourceAsStream("Tracer.smali");

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
     * Builds a given APK using apktool.
     *
     * @param decodedAPKPath The root directory of the decoded APK.
     * @param outputFile The file path of the resulting APK. If {@code null}
     *                   is specified, the default location ('dist' directory)
     *                   and the original APK name is used.
     */
    public static void buildAPK(File decodedAPKPath, File outputFile) {

        ApkOptions apkOptions = new ApkOptions();
        // apkOptions.useAapt2 = true;
        apkOptions.verbose = true;

        try {
            new Androlib(apkOptions).build(new ExtFile(decodedAPKPath), outputFile);
        } catch (BrutException e) {
            LOGGER.warn("Failed to build APK file!");
            LOGGER.warn(e.getMessage());
        }
    }

    /**
     * Decodes a given APK using apktool.
     */
    public static File decodeAPK(File apkPath) {

        try {
            // ApkDecoder decoder = new ApkDecoder(new Androlib());
            ApkDecoder decoder = new ApkDecoder(apkPath);

            // path where we want to decode the APK (the same directory as the APK)
            File parentDir = apkPath.getParentFile();
            File outputDir = new File(parentDir, "decodedAPK");

            LOGGER.info("Decoding Output Dir: " + outputDir);
            decoder.setOutDir(outputDir);

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
     */
    public static Pattern readExcludePatterns() throws IOException {

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
                return Opcodes.forApi(opCode);
            }
        };

        MultiDexIO.writeDexFile(true, filePath, new BasicDexFileNamer(),
                dexFile, DexIO.DEFAULT_MAX_DEX_POOL_SIZE, null);
    }

    /**
     * Produces a .dex file containing the given list of classes.
     *
     * @param filePath The path of the .dex file.
     * @param classes The list of classes that should be contained in the .dex file.
     * @param opCode The API opcode level.
     * @throws IOException Should never happen.
     */
    @SuppressWarnings("unused")
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
                null, // necessary for dexlib2 2.4.0
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
