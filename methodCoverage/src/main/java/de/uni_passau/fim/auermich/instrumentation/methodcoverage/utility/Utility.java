package de.uni_passau.fim.auermich.instrumentation.methodcoverage.utility;

import brut.androlib.ApkBuilder;
import brut.androlib.ApkDecoder;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.common.BrutException;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;
import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation;
import com.android.tools.smali.dexlib2.dexbacked.value.DexBackedTypeEncodedValue;
import com.android.tools.smali.dexlib2.iface.*;
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef;
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod;
import com.android.tools.smali.smali.SmaliTestUtils;
import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import de.uni_passau.fim.auermich.instrumentation.methodcoverage.MethodCoverage;
import de.uni_passau.fim.auermich.instrumentation.methodcoverage.dto.MethodInformation;
import lanchon.multidexlib2.BasicDexFileNamer;
import lanchon.multidexlib2.DexIO;
import lanchon.multidexlib2.MultiDexIO;
import org.antlr.runtime.RecognitionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.regex.Pattern;

public final class Utility {

    public static final String EXCLUSION_PATTERN_FILE = "exclude.txt";
    public static final String OUTPUT_METHODS_FILE = "methods.txt";

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

        InputStream inputStream = MethodCoverage.class.getClassLoader().getResourceAsStream(className);

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
     * @return Returns {@code true} if building the APK succeeded, otherwise {@code false} is returned.
     */
    public static boolean buildAPK(File decodedAPKPath, File outputFile) {

        final Config config = Config.getDefaultConfig();
        config.useAapt2 = true;
        config.verbose = true;

        try {
            new ApkBuilder(config, new ExtFile(decodedAPKPath)).build(outputFile);
            return true;
        } catch (BrutException e) {
            e.printStackTrace();
            return false;
        }
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

        final Config config = Config.getDefaultConfig();
        config.forceDelete = true; // overwrites existing dir: -f

        try {
            // do not decode dex classes to smali: -s
            config.setDecodeSources(Config.DECODE_SOURCES_NONE);

            /*
             * TODO: Right now we need to decode the resources completely although we only need to alter the manifest.
             *  While decoding only the manifest works and even re-packaging succeeds, the APK cannot be properly signed
             *  anymore: https://github.com/iBotPeaches/Apktool/issues/3389
             */

            // do not decode resources: -r
            // config.setDecodeResources(Config.DECODE_RESOURCES_NONE);

            // decode the manifest: --force-manifest
            // config.setForceDecodeManifest(Config.FORCE_DECODE_MANIFEST_FULL);

            // path where we want to decode the APK (the same directory as the APK)
            File parentDir = apkPath.getParentFile();
            File outputDir = new File(parentDir, "decodedAPK");

            LOGGER.debug("Decoding Output Dir: " + outputDir);

            final ApkDecoder decoder = new ApkDecoder(config, apkPath);
            decoder.decode(outputDir);
            return outputDir;
        } catch (AndrolibException | IOException | DirectoryException e) {
            e.printStackTrace();
            LOGGER.error("Failed to decode APK file!");
            return null;
        }
    }

    /**
     * Appends the instrumented method to the methods.txt file.
     *
     * @param instrumentedMethods The list of the instrumented instrumentedMethods.
     */
    public static synchronized void writeMethods(List<MethodInformation> instrumentedMethods) {

        File file = new File(OUTPUT_METHODS_FILE);

        try (OutputStream outputStream = new FileOutputStream(file, true);
             PrintStream printStream = new PrintStream(outputStream)) {

            for (MethodInformation instrumentedMethod : instrumentedMethods) {
                printStream.println(instrumentedMethod.getMethod());
            }

            printStream.flush();
        } catch (IOException e) {
            LOGGER.error("Couldn't write methods to methods.txt");
            throw new IllegalStateException("Couldn't write methods to methods.txt");
        }
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
            LOGGER.debug("Couldn't find exclusion file!");
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
                // https://android.googlesource.com/platform/dalvik/+/master/dx/src/com/android/dex/DexFormat.java
                return Opcodes.forApi(opCode);
            }
        };

        MultiDexIO.writeDexFile(true, 0, filePath, new BasicDexFileNamer(),
                dexFile, DexIO.DEFAULT_MAX_DEX_POOL_SIZE, null);
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
     * Determines whether the given class represents a core application class.
     *
     * @param packageName The package name of the app.
     * @param className The class to be checked.
     * @return Returns {@code true} if the given class represents an application class, otherwise {@code false}.
     */
    public static boolean isApplicationClass(final String packageName, final String className) {
        if (className.startsWith(packageName)) {
            return true;
        } else {
            // Heuristic: At the least the first two packages must match.
            final String[] packages = packageName.split("\\.");
            if (packages.length < 2) {
                return false;
            } else {
                return className.startsWith(packages[0] + "." + packages[1]);
            }
        }
    }
}
