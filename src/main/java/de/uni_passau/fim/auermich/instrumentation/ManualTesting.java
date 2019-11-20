package de.uni_passau.fim.auermich.instrumentation;

import com.google.common.collect.Lists;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.*;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.util.MethodUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class ManualTesting {

    public static final int OPCODE_API = 28;
    private static final int MAX_LOCAL_USABLE_REG = 16; // 15 or 16 (v0-v15)
    private static String packageName;
    private static String mainActivity;

    public static void main(String[] args) throws IOException {

        packageName = args[2];
        mainActivity = args[3];

        // add missing slash to packageName
        packageName = packageName + "/";

        // System.out.println("The extracted packageName is: " + packageName);
        // System.out.println("The extracted MainActivityName is: " + mainActivity);

        String mainActivityDex = "L" + mainActivity.replaceAll("\\.", "/") + ";";
        // System.out.println("The dex conform MainActivityName is: " + mainActivityDex);

        DexFile dexFile = DexFileFactory.loadDexFile(args[0], Opcodes.forApi(OPCODE_API));

        final List<ClassDef> classes = Lists.newArrayList();

        for (ClassDef classDef : dexFile.getClasses()) {

            List<Method> methods = Lists.newArrayList();

            for (Method method : classDef.getMethods()) {

                MethodImplementation methImpl = method.getImplementation();

                if (methImpl != null) {
                    // System.out.println("Class " + classDef.toString() + " contains method: " + method.getName());

                    int totalRegisters = methImpl.getRegisterCount();
                    int paramRegisters = MethodUtil.getParameterRegisterCount(method);
                    int localRegisters = totalRegisters - paramRegisters;

                    // System.out.println("Number of local registers: " + localRegisters);
                    if (localRegisters > MAX_LOCAL_USABLE_REG) {
                        System.out.println("Number of local registers: " + localRegisters);
                        System.out.println("We found an interesting method -->: " + classDef.toString() + "->" + method.getName());
                        if (classDef.toString().contains("AllInOneActivity") && method.getName().contains("onResume")) {
                            analyzeMethod(dexFile, method, localRegisters);
                        }
                        System.out.println(System.lineSeparator());
                    }
                }
            }
        }
        // writeToDexFile(args[1], classes);
    }

    private static void analyzeMethod(DexFile dexFile, Method method, int localRegisters) {

        try {
            MethodAnalyzer analyzer = new MethodAnalyzer(new ClassPath(Lists.newArrayList(new DexClassProvider(dexFile)),
                    true, ClassPath.NOT_ART), method, null, false);

            List<AnalyzedInstruction> analyzedInstructions = analyzer.getAnalyzedInstructions();

            for (AnalyzedInstruction analyzedInstruction : analyzedInstructions) {
                    System.out.println("Instruction: " + analyzedInstruction.getInstruction().getOpcode());
                    System.out.println("Index of instruction: " +analyzedInstruction.getInstructionIndex());
                    System.out.println("Register type of v0 before instruction:"
                        + analyzedInstruction.getPreInstructionRegisterType(0));
                    System.out.println("Register type of v0 after instruction: "
                        + analyzedInstruction.getPostInstructionRegisterType(0));
                    System.out.println(System.lineSeparator());
            }
        } catch (UnresolvedClassException e) {
            e.printStackTrace();
        }
    }

    private static void writeToDexFile(String filePath, List<ClassDef> classes) throws IOException {

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
                return Opcodes.forApi(OPCODE_API);
            }
        });
    }
}
