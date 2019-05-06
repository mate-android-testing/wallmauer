package de.uni_passau.fim.branchcoverage;

import com.google.common.collect.Lists;
import de.uni_passau.fim.utility.Utility;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.analysis.RegisterType;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.BuilderOffsetInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;

import java.util.*;

public final class Instrumenter {

    public static final int MAX_USABLE_REGS = 16;

    /**
     * Checks if {@code implementation} contains a branching instruction.
     *
     * @param implementation The implementation to check.
     * @return Return {@code true} if implementation contains branching instruction.
     */
    public static boolean methodNeedsModification(MethodImplementation implementation) {

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
    public static MethodImplementation insertOnDestroy(String packageName) {

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

        final Map<Integer, RegisterType> registerType = registerTypeMap.getOrDefault(index, null);
        insertInstrumentationCode(mutableImplementation, registerType, index, packageName, "write", coveredInstructions);
    }

    private static Map.Entry<Integer, RegisterType> findSuitableRegister(Map<Integer, RegisterType> registerTypes) {

        Map.Entry<Integer, RegisterType> selectedRegister= new AbstractMap.SimpleEntry<Integer, RegisterType>(-1, RegisterType.LONG_HI_TYPE);

        // find a register type that is not long or double (would require 2 registers)
        for (Map.Entry<Integer, RegisterType> entry : registerTypes.entrySet()) {
            if (entry.getValue().category == RegisterType.LONG_HI || entry.getValue().category== RegisterType.LONG_LO
                    || entry.getValue().category == RegisterType.DOUBLE_HI
                    || entry.getValue().category == RegisterType.DOUBLE_LO) {
                continue;
            } else {
                // we found a suitable type
                // TODO: verify that we don't get undesired type
                selectedRegister = entry;
                break;
            }
        }
        System.out.println("Selected Register: v" + selectedRegister.getKey() + ", " + selectedRegister.getValue());
        return selectedRegister;
    }

    /**
     * Maps a given {@param registerType} to a value defined by the following encoding:
     * Reference         ->  0
     * Primitive         ->  1
     * Primitive-Wide    ->  2
     * Conflicted/UNINIT ->  3
     *
     * @param registerType The register type we like to map to its value.
     * @return Returns the value denoting the register type.
     */
    private static int mapRegisterType(final RegisterType registerType) {

        if (registerType.category == RegisterType.REFERENCE || registerType.category == RegisterType.NULL) {
            return 0;
        } else if (registerType.category == RegisterType.BOOLEAN || registerType.category == RegisterType.CHAR
                || registerType.category == RegisterType.INTEGER || registerType.category == RegisterType.BYTE
                || registerType.category == RegisterType.SHORT || registerType.category == RegisterType.FLOAT) {
            return 1;
        } else if (registerType.category == RegisterType.LONG_HI || registerType.category == RegisterType.LONG_LO
                || registerType.category == RegisterType.DOUBLE_HI || registerType.category == RegisterType.DOUBLE_LO) {
            return 2;
        } else {
            return 3;
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
     * @param registerTypes          Specifies the type of a register at a certain position (instruction). This information
     *                              is only required if we can't use the newly created register directly.
     * @param index                 The position where we insert our instrumented code.
     * @param id                    The trace which identifies the given branch, i.e. packageName->className->method->branchID.
     * @param method                The tracer method which should be called when the given branch is executed.
     * @param coveredInstructions   Represents the amount of instructions, which have already been inspected by
     *                              the method {@method reOrderRegister}.
     */
    private static void insertInstrumentationCode(MutableMethodImplementation mutableImplementation, final Map<Integer, RegisterType> registerTypes,
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

            if (registerTypes == null) {
                System.err.println("Can't instrument method, because registerType couldn't be derived!");
                return;
            }

            Opcode moveOpCode = null;
            Opcode moveBackOpCode = null;
            boolean insertDummyInstruction = false;

            Map.Entry<Integer, RegisterType> selectedRegister = findSuitableRegister(registerTypes);

            // depending on the registerType of v0, we need to use a different move instruction
            switch (mapRegisterType(selectedRegister.getValue())) {
                case 0:
                    // use move-object
                    moveOpCode = Opcode.MOVE_OBJECT_16;
                    moveBackOpCode = Opcode.MOVE_OBJECT_FROM16;
                    break;
                case 1:
                    // use move
                    moveOpCode = Opcode.MOVE_16;
                    moveBackOpCode = Opcode.MOVE_FROM16;
                    break;
                case 2:
                    // use move-wide
                    moveOpCode = Opcode.MOVE_WIDE_16;
                    moveBackOpCode = Opcode.MOVE_WIDE_FROM16;
                    break;
                case 3:
                    // conflicted/unit -> use arbitrarly
                    moveOpCode = Opcode.MOVE_OBJECT_16;
                    moveBackOpCode = Opcode.MOVE_OBJECT_FROM16;
                    insertDummyInstruction = true;
                    break;
                default:
                    System.err.println("Encoding currently not supported!");
                    return;
            }

            int selectedRegisterID = selectedRegister.getKey();
            System.out.println("The selected register ID is: " + selectedRegisterID);

            /*
             * If the register has NOT been initialized with any value, e.g. through const v0 0x1,
             * we can't use it as the source register of a move instruction. Thus, we perform
             * some dummy initialization already matching our desired type.
             */
            if(insertDummyInstruction) {
                BuilderInstruction21c constString = new BuilderInstruction21c(Opcode.CONST_STRING, selectedRegisterID,
                        new ImmutableStringReference("dummy init"));
                coveredInstructions.add(constString);
                mutableImplementation.addInstruction(++index, constString);
            }

            BuilderInstruction32x move = new BuilderInstruction32x(moveOpCode, localRegisterID, selectedRegisterID);

            BuilderInstruction21c constString = new BuilderInstruction21c(Opcode.CONST_STRING, selectedRegisterID,
                    new ImmutableStringReference(id));

            BuilderInstruction35c invokeStatic = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1
                    , selectedRegisterID, 0, 0, 0, 0,
                    new ImmutableMethodReference("Lde/uni_passau/fim/auermich/tracer/Tracer;", method,
                            Lists.newArrayList("Ljava/lang/String;"), "V"));

            BuilderInstruction22x moveBack = new BuilderInstruction22x(moveBackOpCode, selectedRegisterID, localRegisterID);

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
     * Performs the instrumentation, i.e. inserts instructions at each branch.
     *
     * @param implementation The implementation to instrument.
     * @param identifier     The unique identifier for the method, i.e. packageName->className->methodName
     * @param totalRegisters The total amount of registers, i.e. the .register directive at the method head
     * @return Return the instrumented {@code MethodImplementation}.
     * @throws NoSuchFieldException   Should never happen, a byproduct of using reflection.
     * @throws IllegalAccessException Should never happen, a byproduct of using reflection.
     */
    public static MethodImplementation modifyMethod(MethodImplementation implementation, String identifier,
                                                    int totalRegisters, Map<Integer,Branch> branches, Map<Integer,
                                                    Map<Integer,RegisterType>> registerTypeMap, RegisterInformation registerInformation) {

        int branchIndex = 0;
        int branchCounter = 0;

        // contains the covered instructions, those we inspected for modification of the register order (shift of register number)
        Set<BuilderInstruction> coveredInstructions = new HashSet<>();

        // contains the covered else branches
        Set<Branch> coveredElseBranches = new HashSet<>();
        Set<BuilderInstruction> coveredIfBranches = new HashSet<>();

        MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(implementation);
        List<BuilderInstruction> instructions = mutableImplementation.getInstructions();

        // increase the register count of the method, i.e. the .register directive at each method's head
        Utility.increaseMethodRegisterCount(mutableImplementation, totalRegisters);

        // check whether increasing worked
        if (mutableImplementation.getRegisterCount() != totalRegisters) {
            System.out.println("Couldn't increase register count, abort instrumentation!");
            return mutableImplementation;
        }

        for (int i = 0; i < instructions.size(); i++) {

            BuilderInstruction instruction = instructions.get(i);

            if (!coveredInstructions.contains(instruction)) {
                coveredInstructions.add(instruction);
                // re-order param registers (shift of register number)
                Utility.reOrderRegister(instruction, localRegisterID);
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

                Map<Integer, RegisterType> registerTypes = registerTypeMap.getOrDefault(branchIndex, null);

                insertInstrumentationCode(mutableImplementation, registerTypes, ifBranchIndex, id, "trace", coveredInstructions);

                branchCounter++;
                branchIndex++;

                Branch elseBranch = branches.get(branchIndex);

                if (!coveredElseBranches.contains(elseBranch)) {

                    coveredElseBranches.add(elseBranch);

                    id = identifier;
                    id += "->" + branchIndex;

                    int elseBranchIndex = ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex();
                    registerTypes = registerTypeMap.getOrDefault(branchIndex, null);
                    insertInstrumentationCode(mutableImplementation, registerTypes, elseBranchIndex, id, "trace", coveredInstructions);

                    if (localRegisterID <= MAX_LOCAL_USABLE_REG) {
                        mutableImplementation.swapInstructions(elseBranchIndex, elseBranchIndex + 1);
                        mutableImplementation.swapInstructions(elseBranchIndex + 1, elseBranchIndex + 2);
                    } else {
                        if (registerTypes != null) {
                            RegisterType selectedRegisterType = findSuitableRegister(registerTypes).getValue();
                            if (mapRegisterType(selectedRegisterType) == DUMMY_INSTRUCTION_TYPE) {
                                mutableImplementation.swapInstructions(elseBranchIndex, elseBranchIndex + 1);
                                mutableImplementation.swapInstructions(elseBranchIndex + 1, elseBranchIndex + 2);
                                mutableImplementation.swapInstructions(elseBranchIndex + 2, elseBranchIndex + 3);
                                mutableImplementation.swapInstructions(elseBranchIndex + 3, elseBranchIndex + 4);
                                mutableImplementation.swapInstructions(elseBranchIndex + 4, elseBranchIndex + 5);
                            } else {
                                mutableImplementation.swapInstructions(elseBranchIndex, elseBranchIndex + 1);
                                mutableImplementation.swapInstructions(elseBranchIndex + 1, elseBranchIndex + 2);
                                mutableImplementation.swapInstructions(elseBranchIndex + 2, elseBranchIndex + 3);
                                mutableImplementation.swapInstructions(elseBranchIndex + 3, elseBranchIndex + 4);
                            }
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



}
