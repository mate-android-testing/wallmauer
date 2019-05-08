package de.uni_passau.fim.branchcoverage;

import com.google.common.collect.Lists;
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

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    public static final int OPCODE_API = 28;

    public static void main(String[] args) throws IOException, URISyntaxException, IllegalAccessException, NoSuchFieldException {

        String packageName = args[2];
        String mainActivity = args[3];

        // add missing slash to packageName
        packageName = packageName + "/";

        System.out.println("The extracted packageName is: " + packageName);
        System.out.println("The extracted MainActivityName is: " + mainActivity);

        String mainActivityDex = "L" + mainActivity.replaceAll("\\.", "/") + ";";
        System.out.println("The dex conform MainActivityName is: " + mainActivityDex);

        Pattern exclusionPattern = Utility.readExcludePatterns();

        DexFile dexFile = DexFileFactory.loadDexFile(args[0], Opcodes.forApi(OPCODE_API));

        final List<ClassDef> classes = Lists.newArrayList();

        boolean foundMainActivity = false;
        boolean foundOnDestroy = false;
        boolean isMainActivity = false;
        boolean isOnDestroy = false;
        boolean modifiedOnDestroy = false;

        Map<Integer, Map<Integer,RegisterType>> registerTypeMap = new HashMap<>();
        Map<Integer, Branch> branches = new HashMap<>();

        for (ClassDef classDef : dexFile.getClasses()) {

            String className = Utility.dottedClassName(classDef.getType());

            // exclude certain packages/classes from instrumentation, e.g. android.widget.*
            if (exclusionPattern != null && exclusionPattern.matcher(className).matches()) {
                System.out.println("Excluding class: " + className + " from instrumentation!");
                classes.add(classDef);
                continue;
            }

            if (Utility.isMainActivity(classDef, mainActivityDex)) {
                isMainActivity = true;
                foundMainActivity = true;
            }

            List<Method> methods = Lists.newArrayList();
            boolean modifiedMethod = false;

            for (Method method : classDef.getMethods()) {

                if (isMainActivity && Utility.isOnDestroy(method)) {
                    isOnDestroy = true;
                    foundOnDestroy = true;
                }

                MethodImplementation methImpl = method.getImplementation();

                if (methImpl != null && Instrumenter.methodNeedsModification(methImpl)
                        || isOnDestroy) {

                    System.out.println("Class " + classDef.toString() + " contains method: " + method.getName());

                    // clear register map + reset branches
                    registerTypeMap.clear();
                    branches.clear();

                    // save register types of register with ID 14,15 (those are shifted out potentially)
                    List<RegisterType> registerTypes = new ArrayList<>();

                    // each method is identified by its class name and method name
                    String id = className + "->" + method.getName();

                    // we need to compute the number of additional local registers
                    int totalRegisters = methImpl.getRegisterCount();
                    int paramRegisters = MethodUtil.getParameterRegisterCount(method);
                    int localRegisters = totalRegisters - paramRegisters;

                    /*
                    * Due to the fact that invoke-range instructions can define
                    * a range of registers as parameters, even over the
                    * param register border, e.g. from v12 to p4, newly inserted
                    * local registers would corrupt those instructions. That's why
                    * we need to create for every param register a new local register
                    * or at least two additional registers if less than two param registers
                    * are present, and shift individually the content of the param
                    * register to the corresponding newly created local register.
                    * This ensures, we don't corrupt any invoke-range instruction as our
                    * registers for the instrumentation reside at the location of the param registers.
                     */
                    int additionalRegisters = Math.max(paramRegisters,2);

                    // compute both usable and new local register IDs
                    List<Integer> newLocalRegisters = new ArrayList<>();
                    List<Integer> usableRegisters = new ArrayList<>();

                    // list the new local register IDs which start at #localRegisters
                    // list the usable registers IDs which got shifted to the right due to new local registers
                    for (int reg=0; reg < additionalRegisters; reg++) {
                        newLocalRegisters.add(localRegisters+reg);
                        usableRegisters.add(totalRegisters-paramRegisters+reg);
                    }

                    totalRegisters = totalRegisters + additionalRegisters;

                    RegisterInformation registerInformation = new RegisterInformation(id,
                            newLocalRegisters, usableRegisters);

                    System.out.println("The method now has " + totalRegisters + " registers in total.");

                    // we need to analyze the register types in case we exceed 16 registers in total
                    try {
                        MethodAnalyzer analyzer = new MethodAnalyzer(new ClassPath(Lists.newArrayList(new DexClassProvider(dexFile)),
                                true, ClassPath.NOT_ART), method, null, false);

                        // analyze all register types at each branch
                        Analyzer.analyzeRegisterTypes(analyzer, method, branches, registerTypeMap);

                        // we need to analyze the register type of all param registers (at their original location) at the method entry
                        if (!usableRegisters.equals(newLocalRegisters)) {
                            // this is only the case when #p-regs > 0 -> usableRegs != newLocalRegs
                            registerTypes = Analyzer.analyzeShiftedRegisterTypes(analyzer, newLocalRegisters);
                        }
                    } catch (UnresolvedClassException e) {
                        e.printStackTrace();
                    }

                    modifiedMethod = true;

                    // we need to track the instructions we inserted, these are irrelevant for the later register substitution
                    Set<BuilderInstruction> insertedInstructions = new HashSet<>();

                    // instrument branches
                    MethodImplementation modifiedImplementation =
                            Instrumenter.modifyMethod(methImpl, id, totalRegisters, branches, registerTypeMap, registerInformation, insertedInstructions);

                    // whether we already modified onDestroy or not (if branches of it), we need to further modify it to call Tracer.write()
                    if (isMainActivity && isOnDestroy && !modifiedOnDestroy) {
                        modifiedImplementation = Instrumenter.modifyOnDestroy(modifiedImplementation, packageName, insertedInstructions);
                        modifiedOnDestroy = true;
                    }

                    // we need to shift the content of the param registers into the new local registers
                    // then we can use param registers for the branch coverage instructions
                    // this means we need to replace the register IDs in every instruction
                    if (totalRegisters > Instrumenter.MAX_USABLE_REGS
                            && !registerInformation.getNewLocalRegisters().equals(registerInformation.getUsableRegisters())) {

                        System.out.println("Tracked Instructions: " + insertedInstructions.size());

                        for (BuilderInstruction instruction : insertedInstructions) {
                            System.out.println(instruction.getLocation().getIndex());
                            System.out.println(instruction.getLocation().getCodeAddress());
                            System.out.println(instruction.getCodeUnits());
                            System.out.println(instruction.getFormat());
                            System.out.println(instruction.getOpcode());
                            System.out.println(System.lineSeparator());
                        }

                        // replace usable with local regs
                        // TODO: this should be already done since reOrderRegisters ist not called
                        // modifiedImplementation = Utility.replaceRegisterIDs(modifiedImplementation, registerInformation, insertedInstructions);

                        // insert move instruction at method head for making usable registers free
                        modifiedImplementation = Instrumenter.insertMoveInstructionsAtMethodEntry(modifiedImplementation, registerInformation, registerTypes);
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
                    methods.add(method);
                }
            }

            // check whether we need to insert own onDestroy method
            if (isMainActivity && !isOnDestroy && !modifiedOnDestroy) {
                modifiedMethod = true;
                modifiedOnDestroy = true;

                methods.add(new ImmutableMethod(
                        classDef.toString(),
                        "onDestroy",
                        null,
                        "V",
                        4,
                        null,
                        Instrumenter.insertOnDestroy(packageName)));
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

            Utility.writeBranches(className, branches.size());
        }

        System.out.println("Found 'MainActivity': " + foundMainActivity);
        System.out.println("Does 'MainActivity contains onDestroy method per default: " + foundOnDestroy);

        // we want to create a new DexFile containing the modified code
        Utility.writeToDexFile(args[1], classes, OPCODE_API);
    }
}
