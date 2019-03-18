package de.uni_passau.fim.branchcoverage;

import com.google.common.collect.Lists;
import de.uni_passau.fim.branchcoverage.Branch;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.*;
import org.jf.dexlib2.builder.*;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.util.MethodUtil;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

public class BranchCoverage {

    public static final int OPCODE_API = 25;
    private static final String EXCLUSION_PATTERN_FILE = "exclude.txt";
    private static final String OUTPUT_BRANCHES_FILE = "branches.txt";
    private static boolean isOnDestroy = false;
    private static boolean isMainActivity = false;
    private static boolean modifiedOnDestroy = false;
    private static boolean foundMainActivity = false;
    private static boolean foundOnDestroy = false;
    private static String mainActivity;
    private static String packageName;
    private static int branchCounter;
    private static final int MAX_REGS = 16;
    private static final int MAX_LOCAL_USABLE_REG = 15; // v0-v15 only usable with certain instructions
    private static int localRegisterID = 0;
    private static MethodAnalyzer analyzer;
    private static int registerType;
    private static boolean doesElseBranchExist;
    private static List<Branch> coveredElseBranches = new ArrayList<>();
    private static Map<Integer, RegisterType> registerTypeMap = new HashMap<>();
    private static Map<Integer, Branch> branches = new HashMap<>();

    /**
     * Checks if {@code implementation} contains a branching instruction.
     *
     * @param implementation The implementation to check.
     * @return Return {@code true} if implementation contains branching instruction.
     */
    private static boolean methodNeedsModification(MethodImplementation implementation) {

        if (isOnDestroy)
            return true;

        List<Instruction> instructions = Lists.newArrayList(implementation.getInstructions());

        /*
         * Search for branch instructions. Those are identified by their opcode,
         * which ranges from 32-3D, where 32-37 refers to register comparison
         * and 38-3D compares against 0, e.g. IF_EQZ or IF_NEZ. Alternatively,
         * all branching instructions start with the prefix 'IF_'.
         */
        for (Instruction instruction : instructions) {
            if (instruction.getOpcode().name().startsWith("IF_"))
                return true;
        }

        return false;
    }

    /**
     * Inserts a simple onDestroy method in the 'MainActivity' if it is not
     * present yet. The only purpose of this method is calling the 'write' method
     * of our tracer functionality, which in turn writes the collected traces
     * into the app-internal storage. The onDestroy method should be always called
     * upon the (normal) termination of the app.
     *
     * @return Returns the implementation of the onDestroy method.
     */
    private static MethodImplementation insertOnDestroy() {

        modifiedOnDestroy = true;
        System.out.println("Inserting onDestroy method into 'MainActivity'");

        MutableMethodImplementation implementation = new MutableMethodImplementation(2);

        // TODO: verify that addInstruction inserts the instruction at the end!
        // const-string instruction has format '21c' and opcode '1a', registerA defines local register starting from v0,v1,...
        implementation.addInstruction(new BuilderInstruction21c(Opcode.CONST_STRING, 0,
                new ImmutableStringReference(packageName)));

        //     invoke-static {v0}, Lde/uni_passau/fim/auermich/tracer/Tracer;->write(Ljava/lang/String;)V

        // invoke-static instruction has format '35c' and opcode '71'
        implementation.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1
                , 0, 0, 0, 0, 0,
                new ImmutableMethodReference("Lde/uni_passau/fim/auermich/tracer/Tracer;", "write",
                        Lists.newArrayList("Ljava/lang/String;"), "V")));

        // we have to add return-statement as well, though void!
        implementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));

        return implementation;
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
    private static void reOrderRegister(BuilderInstruction instruction, int registerNumber)
            throws NoSuchFieldException, IllegalAccessException {

        // those invoke range instructions require a special treatment, since they don't have fields containing the registers
        if (instruction instanceof BuilderInstruction3rc) {

            // those instructions store the number of registers (var registerCount) and the first register of these range (var startRegister)
            int registerStart = ((BuilderInstruction3rc) instruction).getStartRegister();
            java.lang.reflect.Field f = instruction.getClass().getDeclaredField("startRegister");
            if (registerStart >= registerNumber) {
                f.setAccessible(true);
                f.set(instruction, registerStart + 1);
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
                        field.set(instruction, value + 1);

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Inserts at each branch the tracer functionality. The if-branch is identified by an instruction
     * having a certain opcode, while the else-branch is indirectly addressed by the first instruction residing
     * at the label specified at the if-branch. We need to distinguish whether we can use the newly created local
     * register directly, or whether we need to use a save and restore approach in combination with an already
     * used register.
     *
     * @param mutableImplementation The implementation we need to instrument.
     * @param registerType          Specifies the type of a register at a certain position (instruction). This information
     *                              is only required if we can't use the newly created register directly.
     * @param index                 The position where we insert our instrumented code.
     * @param id                    The trace which identifies the given branch, i.e. packageName->className->method->branchID.
     * @param method                The tracer method which should be called when the given branch is executed.
     * @param coveredInstructions   Represents the amount of instructions, which have already been inspected by
     *                              the method {@method reOrderRegister}.
     */
    private static void insertInstrumentationCode(MutableMethodImplementation mutableImplementation, final RegisterType registerType,
                                                  int index, final String id, final String method, Set<BuilderInstruction> coveredInstructions) {

        if (localRegisterID <= MAX_LOCAL_USABLE_REG) {

            BuilderInstruction21c constString = new BuilderInstruction21c(Opcode.CONST_STRING, localRegisterID,
                    new ImmutableStringReference(id));

            BuilderInstruction35c invokeStatic = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1
                    , localRegisterID, 0, 0, 0, 0,
                    new ImmutableMethodReference("Lde/uni_passau/fim/auermich/tracer/Tracer;", method,
                            Lists.newArrayList("Ljava/lang/String;"), "V"));

            coveredInstructions.add(constString);
            coveredInstructions.add(invokeStatic);

            mutableImplementation.addInstruction(++index, constString);
            mutableImplementation.addInstruction(++index, invokeStatic);

        } else {

            if (registerType == null) {
                System.err.println("Can't instrument method, because registerType couldn't be derived!");
                return;
            }

            Opcode moveOpCode = null;

            // depending on the registerType of v0, we need to use a different move instruction
            switch (mapRegisterType(registerType)) {
                case 0:
                    // use move-object
                    moveOpCode = Opcode.MOVE_OBJECT_16;
                    break;
                case 1:
                    // use move-wide
                    moveOpCode = Opcode.MOVE_WIDE_16;
                    break;
                case 2:
                    // use move
                    moveOpCode = Opcode.MOVE_16;
                    break;
                default:
                    System.err.println("Encoding currently not supported!");
            }

            BuilderInstruction32x move = new BuilderInstruction32x(moveOpCode, localRegisterID, 0);

            BuilderInstruction21c constString = new BuilderInstruction21c(Opcode.CONST_STRING, 0,
                    new ImmutableStringReference(id));

            BuilderInstruction35c invokeStatic = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1
                    , 0, 0, 0, 0, 0,
                    new ImmutableMethodReference("Lde/uni_passau/fim/auermich/tracer/Tracer;", method,
                            Lists.newArrayList("Ljava/lang/String;"), "V"));

            BuilderInstruction32x moveBack = new BuilderInstruction32x(moveOpCode, 0, localRegisterID);

            coveredInstructions.add(move);
            coveredInstructions.add(constString);
            coveredInstructions.add(invokeStatic);
            coveredInstructions.add(moveBack);

            mutableImplementation.addInstruction(++index, move);
            mutableImplementation.addInstruction(++index, constString);
            mutableImplementation.addInstruction(++index, invokeStatic);
            mutableImplementation.addInstruction(++index, moveBack);

        }
    }

    /**
     * Increases the register directive of the method, i.e. the .register statement at the method head
     * according to the number specified by {@param newRegisterCount}.
     *
     * @param mutableImplementation The implementation representing the method.
     * @param newRegisterCount      The new amount of registers the method should have.
     * @throws NoSuchFieldException   Should never happen - a byproduct of reflection.
     * @throws IllegalAccessException Should never happen - a byproduct of reflection.
     */
    private static void increaseMethodRegisterCount(MutableMethodImplementation mutableImplementation, int newRegisterCount)
            throws NoSuchFieldException, IllegalAccessException {

        java.lang.reflect.Field f = mutableImplementation.getClass().getDeclaredField("registerCount");
        f.setAccessible(true);
        f.set(mutableImplementation, newRegisterCount);
    }

    /**
     * Performs the instrumentation, i.e. inserts instructions at each branch.
     *
     * @param implementation The implementation to instrument.
     * @param identifier     The unique identifier for the method, i.e. packageName->className->methodName
     * @param totalRegisters The total amount of registers, i.e. the .register directive at the method head
     * @return Return the instrumented {@code MethodImplementation}.
     * @throws NoSuchFieldException   Should never happen, a byproduct of using reflection.
     * @throws IllegalAccessException Should never happen, a byproduct of using reflection.
     */
    private static MethodImplementation modifyMethod(MethodImplementation implementation, String identifier, int totalRegisters)
            throws NoSuchFieldException, IllegalAccessException {

        int branchIndex = 0;
        branchCounter = 0;

        // contains the covered instructions, those we inspected for modification of the register order (shift of register number)
        Set<BuilderInstruction> coveredInstructions = new HashSet<>();

        // contains the covered else branches
        Set<Branch> coveredElseBranches = new HashSet<>();
        Set<BuilderInstruction> coveredIfBranches = new HashSet<>();

        MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(implementation);
        List<BuilderInstruction> instructions = mutableImplementation.getInstructions();

        // increase the register count of the method, i.e. the .register directive at each method's head
        increaseMethodRegisterCount(mutableImplementation, totalRegisters);

        for (int i = 0; i < instructions.size(); i++) {

            BuilderInstruction instruction = instructions.get(i);

            if (!coveredInstructions.contains(instruction)) {
                coveredInstructions.add(instruction);
                // re-order param registers (shift of register number)
                reOrderRegister(instruction, localRegisterID);
            }

            /**
             * Branching instructions are either identified by their opcode,
             * the prefix they share (i.e. IF_) or the format, which
             * is either '21t' (e.g. IF_EQZ) or '22t' (e.g. IF_EQ).
             */
            if ((instruction instanceof BuilderInstruction21t
                    || instruction instanceof BuilderInstruction22t)
                    && !coveredIfBranches.contains(instruction)) {

                // avoid iterating over the same instruction several times
                coveredIfBranches.add(instruction);

                String id = identifier;
                id += "->" + branchIndex;

                Branch ifBranch = branches.get(branchIndex);
                int ifBranchIndex = instruction.getLocation().getIndex();

                RegisterType registerType = registerTypeMap.getOrDefault(branchIndex, null);
                insertInstrumentationCode(mutableImplementation, registerType, ifBranchIndex, id, "trace", coveredInstructions);

                branchCounter++;
                branchIndex++;

                Branch elseBranch = branches.get(branchIndex);

                if (!coveredElseBranches.contains(elseBranch)) {

                    coveredElseBranches.add(elseBranch);

                    id = identifier;
                    id += "->" + branchIndex;

                    int elseBranchIndex = ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex();
                    registerType = registerTypeMap.getOrDefault(branchIndex, null);
                    insertInstrumentationCode(mutableImplementation, registerType, elseBranchIndex, id, "trace", coveredInstructions);

                    if (localRegisterID <= MAX_LOCAL_USABLE_REG) {
                        mutableImplementation.swapInstructions(elseBranchIndex, elseBranchIndex + 1);
                        mutableImplementation.swapInstructions(elseBranchIndex + 1, elseBranchIndex + 2);
                    } else {
                        if (registerType != null) {
                            mutableImplementation.swapInstructions(elseBranchIndex, elseBranchIndex + 1);
                            mutableImplementation.swapInstructions(elseBranchIndex + 1, elseBranchIndex + 2);
                            mutableImplementation.swapInstructions(elseBranchIndex + 2, elseBranchIndex + 3);
                            mutableImplementation.swapInstructions(elseBranchIndex + 3, elseBranchIndex + 4);
                        }
                    }
                    branchCounter++;
                }
                // we have to update the branchIndex in any way
                branchIndex++;
            }
        }

        // whether we already modified onDestroy or not (if branches of it), we need to further modify it to call Tracer.write()
        if (isMainActivity && isOnDestroy && !modifiedOnDestroy) {
            modifyOnDestroy(mutableImplementation, coveredInstructions);
        }
        return mutableImplementation;
    }

    /**
     * If the 'MainActivity' already contains an onDestroy method, we have to integrate our tracer functionality
     * into it instead of creating an own onDestroy method. In general, we need to place before each 'return' statement
     * instructions that call our tracer.
     *
     * @param mutableImplementation The implementation of the onDestroy method.
     * @param coveredInstructions   A set of instructions that should be not re-ordered, i.e. the method
     *                              {@method reOrderRegister} should not be invoked on those instructions.
     */
    private static void modifyOnDestroy(MutableMethodImplementation mutableImplementation, Set<BuilderInstruction> coveredInstructions) {

        System.out.println("Modifying onDestroy method of 'MainActivity'");
        modifiedOnDestroy = true;

        /*
         * The onDestroy method of the 'MainActivity' requires
         * an additional instruction, which in turn calls Tracer.write(packageName).
         * This instruction should be placed at the last position, since the onDestroy
         * method may also contain if branches, which should be reported. However,
         * we have to assume that last statement of onDestroy gets triggered,
         * otherwise we end up with not calling Tracer.write().
         * TODO: check for return statement(s) of onDestroy, i.e. return-void
         * TODO: and place before each the Tracer.write() instruction.
         */

        // FIXME: we didn't analyze the type of v0 before each return statement, we should call MethodAnalyzer again here

        // assuming last statement is return statement and we don't accidentally overwrite the succeeding instructions
        int index = mutableImplementation.getInstructions().size() - 2;

        final RegisterType registerType = registerTypeMap.getOrDefault(index, null);
        insertInstrumentationCode(mutableImplementation, registerType, index, packageName, "write", coveredInstructions);
    }

    private static boolean doesInstructionUseLocalReg(BuilderInstruction instruction, int localRegisterNumber) {

        for (AnalyzedInstruction analyzedInstruction : analyzer.getAnalyzedInstructions()) {

            if (analyzedInstruction.getInstruction().getOpcode().equals(instruction.getOpcode())
                    && ((analyzedInstruction.getInstructionIndex() == instruction.getLocation().getIndex())
                    || (analyzedInstruction.getInstruction().getCodeUnits() == instruction.getCodeUnits())
                    || (Math.abs(analyzedInstruction.getInstructionIndex() - instruction.getLocation().getIndex()) <= 4))) {

                // assuming we have found the instruction within the analyzed one

                int registers = analyzedInstruction.getRegisterCount();

                if (registers > localRegisterNumber && !analyzedInstruction.getPreInstructionRegisterType(localRegisterNumber)
                        .equals(analyzedInstruction.getPostInstructionRegisterType(localRegisterNumber))) {

                    System.out.println("The type of register v" + localRegisterNumber + " has changed.");
                    registerType = mapRegisterType(analyzedInstruction.getPostInstructionRegisterType(localRegisterNumber));
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Maps a given {@param registerType} to a value defined by the following encoding:
     * Reference       ->  0
     * Primitive       ->  1
     * Primitive-Wide ->   2
     *
     * @param registerType The register type we like to map to its value.
     * @return Returns the value denoting the register type.
     */
    private static int mapRegisterType(final RegisterType registerType) {

        if (registerType.category == RegisterType.REFERENCE || registerType.category == RegisterType.CONFLICTED
                || registerType.category == RegisterType.UNINIT_REF || registerType.category == RegisterType.UNINIT_THIS
                || registerType.category == RegisterType.UNKNOWN || registerType.category == RegisterType.NULL) {
            return 0;

        } /*else if (registerType == RegisterType.DOUBLE_HI_TYPE || registerType == RegisterType.DOUBLE_LO_TYPE
                || registerType == RegisterType.LONG_HI_TYPE || registerType == RegisterType.LONG_LO_TYPE) {
            return 1;
        }*/ else {
            return 2;
        }
    }

    /**
     * Generates patterns of classes which should be excluded from the instrumentation.
     *
     * @return The pattern representing classes that should not be instrumented.
     * @throws IOException        If the file containing excluded classes is not available.
     * @throws URISyntaxException If the file is not present.
     */
    private static Pattern readExcludePatterns() throws IOException, URISyntaxException {

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
     * Transforms a class name containing '/' into a class name with '.'
     * instead, and removes the leading 'L' as well as the ';' at the end.
     *
     * @param className The class name which should be transformed.
     * @return The transformed class name.
     */
    private static String dottedClassName(String className) {
        className = className.substring(className.indexOf('L') + 1, className.indexOf(';'));
        className = className.replace('/', '.');
        return className;
    }

    public static void main(String[] args) throws IOException, URISyntaxException, IllegalAccessException, NoSuchFieldException {

        File file = new File(OUTPUT_BRANCHES_FILE);
        OutputStream outputStream = new FileOutputStream(file, true);
        PrintStream printStream = new PrintStream(outputStream);

        packageName = args[2];
        mainActivity = args[3];

        // add missing slash to packageName
        packageName = packageName + "/";

        System.out.println("The extracted packageName is: " + packageName);
        System.out.println("The extracted MainActivityName is: " + mainActivity);

        String mainActivityDex = "L" + mainActivity.replaceAll("\\.", "/") + ";";
        System.out.println("The dex conform MainActivityName is: " + mainActivityDex);

        Pattern exclusionPattern = readExcludePatterns();

        DexFile dexFile = DexFileFactory.loadDexFile(args[0], Opcodes.forApi(OPCODE_API));

        final List<ClassDef> classes = Lists.newArrayList();

        for (ClassDef classDef : dexFile.getClasses()) {

            isMainActivity = false;
            isOnDestroy = false;

            String className = dottedClassName(classDef.getType());

            // exclude certain packages/classes from instrumentation, e.g. android.widget.*
            if (exclusionPattern != null && exclusionPattern.matcher(className).matches()
                    || className.contains("$") || className.contains("com.a")
                    || className.contains("com.b") || className.contains("a.")
                    || className.contains("b.")) {
                // System.out.println("Excluding class: " + className + " from instrumentation!");
                classes.add(classDef);
                continue;
            }

            // we need to reset our branchCounter for each new method/class
            branchCounter = 0;

            if (classDef.toString().equals(mainActivityDex)) {
                isMainActivity = true;
                foundMainActivity = true;
            }

            List<Method> methods = Lists.newArrayList();
            boolean modifiedMethod = false;

            for (Method method : classDef.getMethods()) {

                if (isMainActivity && method.getName().equals("onDestroy")) {
                    isOnDestroy = true;
                    foundOnDestroy = true;
                }

                MethodImplementation methImpl = method.getImplementation();

                // we need to insert a field representing our counter

                if (methImpl != null && methodNeedsModification(methImpl)) {

                    System.out.println("Class " + classDef.toString() + " contains method: " + method.getName());

                    // each method is identified by its class name and method name
                    String id = className + "->" + method.getName();

                    // we need to check whether we have enough local params or not
                    int totalRegisters = methImpl.getRegisterCount();
                    int paramRegisters = MethodUtil.getParameterRegisterCount(method);
                    int localRegisters = totalRegisters - paramRegisters;

                    // we require one additional local register containing our trace
                    totalRegisters++;

                    // we need to determine the ID of the newly inserted local register
                    localRegisterID = localRegisters; // starts from Index 0 (v0)

                    System.out.println("The method requires " + totalRegisters + " registers.");

                    /*
                     * If we the given method uses 15 local registers before the
                     * instrumentation, we require a special treatment for the
                     * parameter registers. The increase of the local register count
                     * by one causes that the original p0 register might be used
                     * within an instruction it is not allowed to due to the
                     * constraint regarding the register number.
                     * TODO: Derive the type of p0 and save it in some other register.
                     */
                    if (localRegisters == MAX_LOCAL_USABLE_REG) {
                        System.out.println("Method discarded from instrumentation!");
                        methods.add(method);
                        continue;
                    }

                    /*
                     * In the case that the given method exceeds the magic number of 16 local
                     * registers, we require a special treatment. In particular,
                     * we need to analyze the instructions in order to determine the type
                     * of v0 at each branch. This is necessary, since the distinction between
                     * non-object (primitive or wide primitive) and object requires the use
                     * of a different move instruction.
                     */
                    registerTypeMap.clear();
                    branches.clear();

                    try {
                        analyzer = new MethodAnalyzer(new ClassPath(Lists.newArrayList(new DexClassProvider(dexFile)),
                                true, ClassPath.NOT_ART), method, null, false);
                        // we want to analyze the register type of v0 at each branch
                        analyzeRegisterTypes(method, 0);
                    } catch (UnresolvedClassException e) {
                        e.printStackTrace();
                        analyzer = null;
                    }

                    modifiedMethod = true;
                    methods.add(new ImmutableMethod(
                            method.getDefiningClass(),
                            method.getName(),
                            method.getParameters(),
                            method.getReturnType(),
                            method.getAccessFlags(),
                            method.getAnnotations(),
                            modifyMethod(methImpl, id, totalRegisters)));

                } else {
                    methods.add(method);
                }
            }

            // check whether we need to insert own onDestroy method
            if (isMainActivity && !isOnDestroy && !modifiedOnDestroy) {
                modifiedMethod = true;

                methods.add(new ImmutableMethod(
                        classDef.toString(),
                        "onDestroy",
                        null,
                        "V",
                        4,
                        null,
                        insertOnDestroy()));
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

            if (branchCounter != 0) {
                // we have to save our branchCounter for the later evaluation
                printStream.println(className + ": " + branchCounter);
                printStream.flush();
            }
        }

        printStream.close();
        System.out.println("Found 'MainActivity': " + foundMainActivity);
        System.out.println("Does 'MainActivity contains onDestroy method per default: " + foundOnDestroy);

        // we want to create a new DexFile containing the modified code
        writeToDexFile(args[1], classes);
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

    /**
     * We like to derive the register type of the local register specified by {@param registerNumber}
     * in the method {@param method} at certain instructions. In particular, we are interested in the
     * register type, i.e. reference, primitive or primitive-wide, at the beginning of each branch.
     * That is the register type after the actual branching instruction, e.g. if-eqz, and the register
     * type at the beginning of the corresponding 'else' branch identified by a label. Since the
     * first instruction of the 'else' branch may modify the register and its type, we have to consider
     * the type before the instruction gets executed.
     *
     * @param method         The method we like to analyze.
     * @param registerNumber The register number of the local register from which we like
     *                       to derive the type.
     */
    private static void analyzeRegisterTypes(Method method, int registerNumber) {

        assert analyzer != null;

        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(method.getImplementation());

        List<BuilderInstruction> instructions = mutableMethodImplementation.getInstructions();
        List<AnalyzedInstruction> analyzedInstructions = analyzer.getAnalyzedInstructions();

        int branchID = 0;

        for (int i = 0; i < instructions.size(); i++) {

            BuilderInstruction instruction = instructions.get(i);

            if (instruction instanceof BuilderInstruction21t
                    || instruction instanceof BuilderInstruction22t) {

                AnalyzedInstruction analyzedInstruction = analyzedInstructions.get(i);

                assert analyzedInstruction.getInstruction().getOpcode().equals(instruction.getOpcode())
                        && analyzedInstruction.getInstructionIndex() == instruction.getLocation().getIndex();

                assert analyzedInstruction.getRegisterCount() > registerNumber;

                Branch ifBranch = new IfBranch(instruction.getLocation().getIndex(), instruction.getLocation().getCodeAddress());
                branches.put(branchID, ifBranch);

                // obtain the current type of the local register with number 'registerNumber' at the if branch
                registerTypeMap.put(branchID, analyzedInstruction.getPostInstructionRegisterType(registerNumber));

                branchID++;

                // additionally we need to consider the first instruction at the else branch (label)
                analyzedInstruction = analyzedInstructions.get(((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex());

                assert analyzedInstruction.getRegisterCount() > registerNumber;

                Branch elseBranch = new ElseBranch(((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex(),
                        ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getCodeAddress(),
                        ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getLabels());

                branches.put(branchID, elseBranch);

                // we need to consider the type before the instruction gets executed, since it may change the register type
                registerTypeMap.put(branchID, analyzedInstruction.getPreInstructionRegisterType(registerNumber));

                branchID++;
            }
        }
    }

}
