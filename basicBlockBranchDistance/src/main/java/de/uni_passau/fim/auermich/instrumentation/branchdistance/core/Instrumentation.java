package de.uni_passau.fim.auermich.instrumentation.branchdistance.core;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.BasicBlockBranchDistance;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.dto.MethodInformation;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.utility.Range;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.utility.Utility;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.analysis.AnalyzedInstruction;
import org.jf.dexlib2.analysis.RegisterType;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.BuilderSwitchPayload;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides the functionality to instrument a given method. This includes
 * the insertion of certain lifecycle methods, e.g. onCreate(), shifting of
 * param registers and the instrumentation of branches.
 */
public final class Instrumentation {

    private static final Logger LOGGER = LogManager.getLogger(Instrumentation.class);

    private static final String TRACER = "Lde/uni_passau/fim/auermich/tracer/Tracer;";

    /**
     * Instruments the given basic block with the tracer functionality.
     *
     * @param methodInformation    Stores all relevant information about the given method.
     * @param instrumentationPoint Describes the position of the basic block.
     * @param trace                The trace which which should be logged every time the basic block is executed.
     * @return Returns the instrumented method implementation.
     */
    private static MutableMethodImplementation insertInstrumentationCode(MethodInformation methodInformation,
                                                                         InstrumentationPoint instrumentationPoint,
                                                                         final String trace) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // the location of try blocks
        Set<Range> tryBlocks = methodInformation.getTryBlocks();

        // the up-to-date position of the basic block / if statement
        int index = instrumentationPoint.getInstruction().getLocation().getIndex();

        // we require one parameter containing the unique branch id
        int freeRegisterID = methodInformation.getFreeRegisters().get(0);

        /*
         * We can't directly insert an instruction before another instruction that is attached to a label.
         * Consider the following example:
         *
         * :label (e.g. an else branch)
         * instruction
         *
         * If we would try to insert our code before the given instruction, the code would be
         * placed actually before the label, which is not what we want. Instead we need insert our code
         * after the instruction and swap the instructions afterwards.
         *
         * However, there is one special case that needs to be addressed here: The bytecode verifier ensures that
         * a move-exception instruction must be the first instruction within a catch block, but not all catch blocks
         * necessarily contain such move-exception instruction. This means whenever an instrumentation point coincides
         * with the location of a move-exception instruction, we can only insert our code after that instruction.
         */
        boolean swapInstructions = instrumentationPoint.isAttachedToLabel();
        if (instrumentationPoint.getInstruction().getOpcode() == Opcode.MOVE_EXCEPTION) {
            index++;
            swapInstructions = false;
        }

        // const-string pN, "basic block identifier" (pN refers to the free register at the end)
        BuilderInstruction21c constString = new BuilderInstruction21c(Opcode.CONST_STRING, freeRegisterID,
                new ImmutableStringReference(trace));

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                freeRegisterID, 1,
                new ImmutableMethodReference(TRACER, "trace",
                        Lists.newArrayList("Ljava/lang/String;"), "V"));

        // check whether the branch is located within a try block
        if (tryBlocks.stream().anyMatch(range -> range.contains(instrumentationPoint.getPosition()))) {

            /*
             * The bytecode verifier doesn't allow us to insert our tracer functionality directly within
             * try blocks. Actually, only (implicit) try blocks around a synchronized block are affected,
             * but we consider here any try block. The problem arises from the fact that an invoke instruction
             * within a try block introduces an additional edge to corresponding catch blocks, although it may
             * never throw an exception. As a result, the register type of the monitor enter/exit instruction, e.g. v1,
             * might be two-fold (conflicted), which is rejected by the verifier, see
             * https://android.googlesource.com/platform/art/+/master/runtime/verifier/register_line.cc#367.
             * Also consider the answer at:
             * https://stackoverflow.com/questions/64034015/dalvik-bytecode-verification-dex2oat/64034465.
             * The concrete problem is that the monitor object (register) gets in a conflicted state because the
             * insertion of instructions that can throw an exception introduce a new control flow (execution path)
             * in which the monitor object has not been initialized, consider the following example:
             *
             * if-eqz v0, :cond_0
             *
             * iget-object v1, v0, La/d/cn;->b:Ljava/lang/Object (initialises v1)
             * monitor-enter v1
             * :try_start_0
             * [some logic]
             * monitor-exit v1
             *
             * :cond_0
             * [inserted code here causing additional edge to catch block]
             * return p1
             *
             * :catchall_0
             * move-exception v0
             * monitor-exit v1 (can be reached while v1 is still unset)
             * :try_end_0
             *
             * In the original code, v1 is guaranteed to be set when the monitor-exit instruction is reached. However,
             * due to the additional edge introduced by our code, the monitor-exit instruction can be reached while
             * v1 is still unset, which was not possible in the original code.
             *
             * Actually we can bypass the verifier by introducing a jump forward and backward mechanism. Instead of
             * inserting the tracer functionality directly, we insert a goto instruction, which jumps to the end of the
             * method and calls the tracer functionality and afterwards jumps back to the original position. Since
             * a goto instruction can't throw any exception, the verifier doesn't complain. However, we have to ensure
             * that we don't introduce a control flow to the pseudo instructions packed-switch-data, sparse-switch-data
             * or fill-array-data, see the constraint B22 at https://source.android.com/devices/tech/dalvik/constraints.
             *
             * The idea of this kind of hack was taken from the paper 'Fine-grained Code Coverage Measurement in
             * Automated Black-box Android Testing', see section 4.3.
             */

            LOGGER.debug("Instrumentation point within try block!");
            LOGGER.debug("Instrumentation point: " + instrumentationPoint.getInstruction().getOpcode() +
                    "(" + instrumentationPoint.getPosition() + ")");

            // the label + tracer functionality comes after the last instruction
            int afterLastInstruction = mutableMethodImplementation.getInstructions().size();

            // insert goto to jump to method end
            Label tracerLabel = mutableMethodImplementation.newLabelForIndex(afterLastInstruction);
            BuilderInstruction jumpForward = new BuilderInstruction30t(Opcode.GOTO_32, tracerLabel);

            if (swapInstructions) {
                mutableMethodImplementation.addInstruction(index + 1, jumpForward);
                mutableMethodImplementation.swapInstructions(index, index + 1);
            } else {
                mutableMethodImplementation.addInstruction(index, jumpForward);
            }

            // create label at branch after forward jump
            Label branchLabel = mutableMethodImplementation.newLabelForIndex(index + 1);

            // insert tracer functionality at label near method end (+1 because we inserted already goto instruction at branch)
            mutableMethodImplementation.addInstruction(afterLastInstruction + 1, constString);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 2, invokeStaticRange);

            // insert goto to jump back to branch
            BuilderInstruction jumpBackward = new BuilderInstruction30t(Opcode.GOTO_32, branchLabel);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 3, jumpBackward);

        } else {
            if (swapInstructions) {
                mutableMethodImplementation.addInstruction(++index, constString);
                mutableMethodImplementation.addInstruction(++index, invokeStaticRange);

                /*
                 * We cannot directly insert our instructions after the else-branch label (those instructions
                 * would fall between the goto and else-branch label). Instead we need to insert our
                 * instructions after the first instructions there, and swap them back afterwards.
                 */
                mutableMethodImplementation.swapInstructions(index - 2, index - 1);
                mutableMethodImplementation.swapInstructions(index - 1, index);
            } else {
                mutableMethodImplementation.addInstruction(index, constString);
                mutableMethodImplementation.addInstruction(index + 1, invokeStaticRange);
            }
        }

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
        return mutableMethodImplementation;
    }

    /**
     * UPDATE: Although certain instructions are not allowed to be reachable by the control flow,
     * e.g. PACKED_SWITCH_PAYLOAD, they don't need to be at the end of the method!
     * Thus, we can always insert instructions after those pseudo-instructions.
     * <p>
     * Returns a possible position for a new label near the end of a method.
     * Note that a single instruction can only have one label. If we try to define
     * an additional label, the old label is shared/re-used (verify this behaviour!).
     *
     * @param methodInformation Encapsulates a method.
     * @return Returns a possible position for a new label.
     */
    @SuppressWarnings("unused")
    private static int getFreeLabelPosition(MethodInformation methodInformation) {

        MutableMethodImplementation mutableImplementation =
                new MutableMethodImplementation(methodInformation.getMethodImplementation());

        List<BuilderInstruction> instructions = new ArrayList<>(mutableImplementation.getInstructions());
        Collections.reverse(instructions);

        // search for possible label position at the end of method
        for (BuilderInstruction instruction : instructions) {

            // the bytecode verifier ensures that those instructions must be unreachable by control flow
            EnumSet<Opcode> opcodes = EnumSet.of(Opcode.PACKED_SWITCH_PAYLOAD,
                    Opcode.SPARSE_SWITCH_PAYLOAD, Opcode.FILL_ARRAY_DATA);

            if (!opcodes.contains(instruction.getOpcode())) {
                // valid position for new label after instruction (hence + 1)
                LOGGER.info("Position of new label: " + instruction.getLocation().getIndex() + 1);
                return instruction.getLocation().getIndex() + 1;
            }
        }
        throw new IllegalStateException("No free label position found for method: " + methodInformation.getMethodID());
    }

    /**
     * Performs the actual instrumentation. Inserts at each instrumentation point, e.g. at each basic block, a trace
     * statement.
     *
     * @param methodInformation Encapsulates a method and its instrumentation points.
     */
    public static void modifyMethod(MethodInformation methodInformation) {

        LOGGER.info("Register count before increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        // increase the register count of the method, i.e. the .register directive at each method's head
        Utility.increaseMethodRegisterCount(methodInformation, methodInformation.getTotalRegisterCount());

        LOGGER.info("Register count after increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        TreeSet<InstrumentationPoint> instrumentationPoints
                = new TreeSet<>(methodInformation.getIfAndSwitchInstrumentationPoints());
        instrumentationPoints.addAll(methodInformation.getBasicBlockInstrumentationPoints());
        Iterator<InstrumentationPoint> iterator = instrumentationPoints.descendingIterator();

        /*
         * Traverse the instrumentation points backwards, i.e. the last instrumentation point comes first, in order
         * to avoid inherent index/position updates of other instrumentation points while instrumenting.
         */
        while (iterator.hasNext()) {
            InstrumentationPoint instrumentationPoint = iterator.next();

            // If branch distance instrumentation
            if (instrumentationPoint.getType() == InstrumentationPoint.Type.IF_STMT) {
                computeBranchDistanceIf(methodInformation, instrumentationPoint);
            }
            // Switch branch distance instrumentation
            else if (instrumentationPoint.getType() == InstrumentationPoint.Type.SWITCH_STMT) {
                computeBranchDistanceSwitch(methodInformation, instrumentationPoint);
            }
            // BasicBlock Instrumentation
            else {
                String isBranch = instrumentationPoint.hasBranchType() ? "isBranch" : "noBranch";
                String trace = methodInformation.getMethodID() + "->" + instrumentationPoint.getPosition() + "->"
                        + instrumentationPoint.getCoveredInstructions() + "->" + isBranch;

                insertInstrumentationCode(methodInformation, instrumentationPoint, trace);
            }
        }
    }

    /**
     * Inserts instructions before every if stmt in order to invoke the branch distance computation.
     *
     * @param methodInformation    Encapsulates the method.
     * @param instrumentationPoint Encapsulates information about the if stmt.
     */
    private static void computeBranchDistanceIf(MethodInformation methodInformation, InstrumentationPoint instrumentationPoint) {

        // get the if instruction (new index)
        int instructionIndex = instrumentationPoint.getInstruction().getLocation().getIndex();
        AnalyzedInstruction instruction = methodInformation.getInstructionAtIndex(instructionIndex);

        LOGGER.debug("Number of instructions: " + methodInformation.getInstructions().size());

        LOGGER.debug("Old index: " + instrumentationPoint.getPosition());
        LOGGER.debug("New index: " + instructionIndex);
        LOGGER.debug("Real index: " + instruction.getInstructionIndex());

        LOGGER.debug("Saved Instruction: " + instrumentationPoint.getInstruction().getOpcode());
        LOGGER.debug("Retrieved Instruction: " + instruction.getInstruction().getOpcode());
        LOGGER.debug("Retrieved original Instruction: " + instruction.getOriginalInstruction().getOpcode());

        // map op code to internal operation code
        int operation = mapOpCodeToOperation(instruction.getOriginalInstruction().getOpcode());

        if (instruction.getOriginalInstruction().getOpcode().format == Format.Format21t) {
            // unary operation -> if-eqz v0
            BuilderInstruction21t instruction21t = (BuilderInstruction21t) instrumentationPoint.getInstruction();
            int registerA = instruction21t.getRegisterA();

            RegisterType registerTypeA = instruction.getPreInstructionRegisterType(registerA);

            LOGGER.info("Method: " + methodInformation.getMethodID());
            LOGGER.info("IF-Instruction: " + instruction.getOriginalInstruction().getOpcode() + "[" + instructionIndex + "]");
            LOGGER.info("RegisterA: " + registerA + "[" + registerTypeA + "]");

            // check whether we deal with primitive or object types
            if (registerTypeA.category != RegisterType.REFERENCE && registerTypeA.category != RegisterType.UNINIT_REF) {
                handlePrimitiveUnaryComparison(methodInformation, instrumentationPoint, operation, registerA);
            } else {
                handleObjectUnaryComparison(methodInformation, instrumentationPoint, operation, registerA);
            }

        } else {
            // binary operation -> if-eq v0, v1
            BuilderInstruction22t instruction22t = (BuilderInstruction22t) instrumentationPoint.getInstruction();
            int registerA = instruction22t.getRegisterA();
            int registerB = instruction22t.getRegisterB();

            RegisterType registerTypeA = instruction.getPreInstructionRegisterType(registerA);
            RegisterType registerTypeB = instruction.getPreInstructionRegisterType(registerB);

            LOGGER.info("Method: " + methodInformation.getMethodID());
            LOGGER.info("IF-Instruction: " + instruction.getOriginalInstruction().getOpcode() + "[" + instructionIndex + "]");
            LOGGER.info("RegisterA: " + registerA + "[" + registerTypeA + "]");
            LOGGER.info("RegisterB: " + registerB + "[" + registerTypeB + "]");

            Set<Byte> referenceTypes = new HashSet<Byte>() {{
                add(RegisterType.REFERENCE);
                add(RegisterType.UNINIT_REF);
            }};

            if (!referenceTypes.contains(registerTypeA.category) && !referenceTypes.contains(registerTypeB.category)) {
                handlePrimitiveBinaryComparison(methodInformation, instrumentationPoint, operation, registerA, registerB);
            } else if (referenceTypes.contains(registerTypeA.category) && referenceTypes.contains(registerTypeB.category)) {
                handleObjectBinaryComparison(methodInformation, instrumentationPoint, operation, registerA, registerB);
            } else {
                throw new IllegalStateException("Comparing objects with primitives!");
            }
        }
    }

    /**
     * Inserts instructions before every switch stmt in order to invoke the branch distance computation.
     *
     * @param methodInformation    Encapsulates the method.
     * @param instrumentationPoint Encapsulates information about the switch stmt.
     */
    private static void computeBranchDistanceSwitch(MethodInformation methodInformation, InstrumentationPoint instrumentationPoint) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // the location of try blocks
        Set<Range> tryBlocks = methodInformation.getTryBlocks();

        int instructionIndex = instrumentationPoint.getInstruction().getLocation().getIndex();

        // TODO: Unify the branch distance trace between if and switch statements such that we can pick any in case of
        //  a shared branch/case! That would allow us to pick the minimum of both.
        final String trace = methodInformation.getMethodID() + "->switch->" + instrumentationPoint.getPosition();

        LOGGER.debug("Switch statement: " + trace);

        // we require one parameter for the trace
        int firstFreeRegister = methodInformation.getFreeRegisters().get(0);

        // we need another free register for the switch value
        int secondFreeRegister = methodInformation.getFreeRegisters().get(1);

        // we need another free register for the case values and their positions encoded in a string
        int thirdFreeRegister = methodInformation.getFreeRegisters().get(2);

        // const/4 vA, #+B - stores the trace
        BuilderInstruction21c traceConst = new BuilderInstruction21c(Opcode.CONST_STRING, firstFreeRegister,
                new ImmutableStringReference(trace));

        BuilderInstruction31t switchInstruction = (BuilderInstruction31t) instrumentationPoint.getInstruction();
        BuilderSwitchPayload switchPayloadInstruction = (BuilderSwitchPayload) instrumentationPoint.getPayloadInstruction();

        // get the register where the switch value is residing
        int registerA = switchInstruction.getRegisterA();

        // we need to move the content of the switch instruction to the second free register
        // this enables us to use it with the invoke-static range instruction
        BuilderInstruction32x moveA = new BuilderInstruction32x(Opcode.MOVE_16, secondFreeRegister, registerA);

        // concatenate the case values and their positions
        final StringBuilder builder = new StringBuilder();

        for (BuilderSwitchElement switchElement : switchPayloadInstruction.getSwitchElements()) {

            // the position of the case statement
            int casePosition = switchElement.getTarget().getLocation().getIndex();

            // the key defines the case value
            int caseValue = switchElement.getKey();

            LOGGER.debug("Case Position: " + casePosition);
            LOGGER.debug("Case Value: " + caseValue);

            builder.append(casePosition).append(":").append(caseValue).append(",");
        }

        // remove last comma
        builder.setLength(builder.length() - 1);

        // construct the string constant encoding the case positions and their value
        BuilderInstruction21c casesConst = new BuilderInstruction21c(Opcode.CONST_STRING, thirdFreeRegister,
                new ImmutableStringReference(builder.toString()));

        // check whether switch stmt is located within a try block
        if (tryBlocks.stream().anyMatch(range -> range.contains(instrumentationPoint.getPosition()))) {
            /*
             * The bytecode verifier doesn't allow us to insert our functionality directly within
             * try blocks. Actually, only (implicit) try blocks around a synchronized block are affected,
             * but we consider here any try block. The problem arises from the fact that an invoke instruction
             * within a try block introduces an additional edge to corresponding catch blocks, although it may
             * never throw an exception. As a result, the register type of the monitor enter/exit instruction, e.g. v1,
             * might be two-fold (conflicted), which is rejected by the verifier, see
             * https://android.googlesource.com/platform/art/+/master/runtime/verifier/register_line.cc#367.
             *
             * Actually we can bypass the verifier by introducing a jump forward and backward mechanism. Instead of
             * inserting the functionality directly, we insert a goto instruction, which jumps to the end of the
             * method and calls the tracer functionality and afterwards jumps back to the original position. Since
             * a goto instruction can't throw any exception, the verifier doesn't complain. However, we have to ensure
             * that we don't introduce a control flow to the pseudo instructions packed-switch-data, sparse-switch-data
             * or fill-array-data, see the constraint B22 at https://source.android.com/devices/tech/dalvik/constraints.
             *
             * The idea of this kind of hack was taken from the paper 'Fine-grained Code Coverage Measurement in
             * Automated Black-box Android Testing', see section 4.3.
             */

            LOGGER.debug("Switch-Statement within try block at offset: "
                    + instrumentationPoint.getInstruction().getLocation().getCodeAddress());

            // the label + tracer functionality comes after the last instruction
            int afterLastInstruction = mutableMethodImplementation.getInstructions().size();

            // insert goto to jump to method end
            Label tracerLabel = mutableMethodImplementation.newLabelForIndex(afterLastInstruction);
            BuilderInstruction jumpForward = new BuilderInstruction30t(Opcode.GOTO_32, tracerLabel);

            // consider always as an 'else branch', the switch stmt could be the target of a goto instruction
            mutableMethodImplementation.addInstruction(instructionIndex + 1, jumpForward);
            mutableMethodImplementation.swapInstructions(instructionIndex, instructionIndex + 1);

            // create label at switch after forward jump
            Label branchLabel = mutableMethodImplementation.newLabelForIndex(instructionIndex + 1);

            // insert tracer functionality at label near method end (+1 because we inserted already goto instruction at branch)
            mutableMethodImplementation.addInstruction(++afterLastInstruction, moveA);
            mutableMethodImplementation.addInstruction(++afterLastInstruction, traceConst);
            mutableMethodImplementation.addInstruction(++afterLastInstruction, casesConst);

            // we encode the information about the default branch in the method name to save an additional register
            if (instrumentationPoint.containsDefaultBranch()) {
                BuilderInstruction3rc branchDistanceCall = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                        firstFreeRegister, 3,
                        new ImmutableMethodReference(TRACER,
                                "computeBranchDistanceSwitch",
                                Lists.newArrayList("Ljava/lang/String;", "I", "Ljava/lang/String;"), "V"));
                mutableMethodImplementation.addInstruction(++afterLastInstruction, branchDistanceCall);
            } else {
                BuilderInstruction3rc branchDistanceCall = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                        firstFreeRegister, 3,
                        new ImmutableMethodReference(TRACER,
                                "computeBranchDistanceSwitchNoDefaultBranch",
                                Lists.newArrayList("Ljava/lang/String;", "I", "Ljava/lang/String;"), "V"));
                mutableMethodImplementation.addInstruction(++afterLastInstruction, branchDistanceCall);
            }

            // insert goto to jump back to switch
            BuilderInstruction jumpBackward = new BuilderInstruction30t(Opcode.GOTO_32, branchLabel);
            mutableMethodImplementation.addInstruction(++afterLastInstruction, jumpBackward);
        } else {

            // We insert the instructions actually after the switch statement and then swap the instructions to avoid
            // a potential label issue. Otherwise, the instructions would appear before the label attached to the switch.
            int originalIndex = instructionIndex;

            mutableMethodImplementation.addInstruction(++instructionIndex, moveA);
            mutableMethodImplementation.addInstruction(++instructionIndex, traceConst);
            mutableMethodImplementation.addInstruction(++instructionIndex, casesConst);

            // we encode the information about the default branch in the method name to save an additional register
            if (instrumentationPoint.containsDefaultBranch()) {
                BuilderInstruction3rc branchDistanceCall = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                        firstFreeRegister, 3,
                        new ImmutableMethodReference(TRACER,
                                "computeBranchDistanceSwitch",
                                Lists.newArrayList("Ljava/lang/String;", "I", "Ljava/lang/String;"), "V"));
                mutableMethodImplementation.addInstruction(++instructionIndex, branchDistanceCall);
            } else {
                BuilderInstruction3rc branchDistanceCall = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                        firstFreeRegister, 3,
                        new ImmutableMethodReference(TRACER,
                                "computeBranchDistanceSwitchNoDefaultBranch",
                                Lists.newArrayList("Ljava/lang/String;", "I", "Ljava/lang/String;"), "V"));
                mutableMethodImplementation.addInstruction(++instructionIndex, branchDistanceCall);
            }

            mutableMethodImplementation.swapInstructions(originalIndex, originalIndex + 1);
            mutableMethodImplementation.swapInstructions(originalIndex + 1, originalIndex + 2);
            mutableMethodImplementation.swapInstructions(originalIndex + 2, originalIndex + 3);
            mutableMethodImplementation.swapInstructions(originalIndex + 3, originalIndex + 4);
        }

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }

    /**
     * Inserts code to invoke the branch distance computation for an if statement that has
     * a single primitive argument, e.g. if-eqz v0. We simply call 'branchDistance(int op_type, int argument)'.
     *
     * @param methodInformation    Encapsulates a method.
     * @param instrumentationPoint Wrapper for if instruction and its index.
     * @param operation            The operation id, e.g. 0 for if-eqz.
     * @param registerA            The register id of the argument register.
     */
    private static void handlePrimitiveUnaryComparison(MethodInformation methodInformation, InstrumentationPoint instrumentationPoint,
                                                       int operation, int registerA) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // the location of try blocks
        Set<Range> tryBlocks = methodInformation.getTryBlocks();

        int instructionIndex = instrumentationPoint.getInstruction().getLocation().getIndex();
        final String trace = methodInformation.getMethodID() + "->" + instrumentationPoint.getPosition();
        final String operationParam = operation + ":" + trace;

        // we require one parameter for the operation identifier
        int firstFreeRegister = methodInformation.getFreeRegisters().get(0);

        // we need another free register for the single argument of the if instruction
        int secondFreeRegister = methodInformation.getFreeRegisters().get(1);

        // const/4 vA, #+B - stores the operation type identifier + trace, e.g. 0:<trace-id> for if-eqz
        BuilderInstruction21c operationID = new BuilderInstruction21c(Opcode.CONST_STRING, firstFreeRegister,
                new ImmutableStringReference(operationParam));

        // we need to move the content of single if instruction argument to the second free register
        // this enables us to use it with the invoke-static range instruction
        BuilderInstruction32x move = new BuilderInstruction32x(Opcode.MOVE_16, secondFreeRegister, registerA);

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                firstFreeRegister, 2,
                new ImmutableMethodReference(TRACER,
                        "computeBranchDistance",
                        Lists.newArrayList("Ljava/lang/String;", "I"), "V"));

        // check whether if stmt is located within a try block
        if (tryBlocks.stream().anyMatch(range -> range.contains(instrumentationPoint.getPosition()))) {
            /*
             * The bytecode verifier doesn't allow us to insert our functionality directly within
             * try blocks. Actually, only (implicit) try blocks around a synchronized block are affected,
             * but we consider here any try block. The problem arises from the fact that an invoke instruction
             * within a try block introduces an additional edge to corresponding catch blocks, although it may
             * never throw an exception. As a result, the register type of the monitor enter/exit instruction, e.g. v1,
             * might be two-fold (conflicted), which is rejected by the verifier, see
             * https://android.googlesource.com/platform/art/+/master/runtime/verifier/register_line.cc#367.
             *
             * Actually we can bypass the verifier by introducing a jump forward and backward mechanism. Instead of
             * inserting the functionality directly, we insert a goto instruction, which jumps to the end of the
             * method and calls the tracer functionality and afterwards jumps back to the original position. Since
             * a goto instruction can't throw any exception, the verifier doesn't complain. However, we have to ensure
             * that we don't introduce a control flow to the pseudo instructions packed-switch-data, sparse-switch-data
             * or fill-array-data, see the constraint B22 at https://source.android.com/devices/tech/dalvik/constraints.
             *
             * The idea of this kind of hack was taken from the paper 'Fine-grained Code Coverage Measurement in
             * Automated Black-box Android Testing', see section 4.3.
             */

            LOGGER.debug("IF-Statement within try block at offset: "
                    + instrumentationPoint.getInstruction().getLocation().getCodeAddress());

            // the label + tracer functionality comes after the last instruction
            int afterLastInstruction = mutableMethodImplementation.getInstructions().size();

            // insert goto to jump to method end
            Label tracerLabel = mutableMethodImplementation.newLabelForIndex(afterLastInstruction);
            BuilderInstruction jumpForward = new BuilderInstruction30t(Opcode.GOTO_32, tracerLabel);

            // consider always as an 'else branch', the if stmt could be the target of a goto instruction
            mutableMethodImplementation.addInstruction(instructionIndex + 1, jumpForward);
            mutableMethodImplementation.swapInstructions(instructionIndex, instructionIndex + 1);

            // create label at branch after forward jump
            Label branchLabel = mutableMethodImplementation.newLabelForIndex(instructionIndex + 1);

            // insert tracer functionality at label near method end (+1 because we inserted already goto instruction at branch)
            mutableMethodImplementation.addInstruction(afterLastInstruction + 1, operationID);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 2, move);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 3, invokeStaticRange);

            // insert goto to jump back to branch
            BuilderInstruction jumpBackward = new BuilderInstruction30t(Opcode.GOTO_32, branchLabel);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 4, jumpBackward);
        } else {

            mutableMethodImplementation.addInstruction(++instructionIndex, operationID);
            mutableMethodImplementation.addInstruction(++instructionIndex, move);
            mutableMethodImplementation.addInstruction(++instructionIndex, invokeStaticRange);

            mutableMethodImplementation.swapInstructions(instructionIndex - 3, instructionIndex - 2);
            mutableMethodImplementation.swapInstructions(instructionIndex - 2, instructionIndex - 1);
            mutableMethodImplementation.swapInstructions(instructionIndex - 1, instructionIndex);
        }

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }

    /**
     * Inserts code to invoke the branch distance computation for an if statement that has
     * two primitive arguments, e.g. if-eq v0, v1. We simply call '
     * branchDistance(int op_type, int argument1, int argument2)'.
     *
     * @param methodInformation    Encapsulates a method.
     * @param instrumentationPoint Wrapper for if instruction and its index.
     * @param operation            The operation id, e.g. 0 for if-eq v0, v1.
     * @param registerA            The register id of the first argument register.
     * @param registerB            The register id of the second argument register.
     */
    private static void handlePrimitiveBinaryComparison(MethodInformation methodInformation, InstrumentationPoint instrumentationPoint,
                                                        int operation, int registerA, int registerB) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // the location of try blocks
        Set<Range> tryBlocks = methodInformation.getTryBlocks();

        int instructionIndex = instrumentationPoint.getInstruction().getLocation().getIndex();
        final String trace = methodInformation.getMethodID() + "->" + instrumentationPoint.getPosition();
        final String operationParam = operation + ":" + trace;

        // we require one parameter for the operation identifier
        int firstFreeRegister = methodInformation.getFreeRegisters().get(0);

        // we need another free register for the first argument of the if instruction
        int secondFreeRegister = methodInformation.getFreeRegisters().get(1);

        // we need another free register for the second argument of the if instruction
        int thirdFreeRegister = methodInformation.getFreeRegisters().get(2);

        // const/4 vA, #+B - stores the operation type identifier + trace, e.g. 0:<trace-id> for if-eqz
        BuilderInstruction21c operationID = new BuilderInstruction21c(Opcode.CONST_STRING, firstFreeRegister,
                new ImmutableStringReference(operationParam));

        // we need to move the content of the first if instruction argument to the second free register
        // this enables us to use it with the invoke-static range instruction
        BuilderInstruction32x moveA = new BuilderInstruction32x(Opcode.MOVE_16, secondFreeRegister, registerA);

        // we need to move the content of the second if instruction argument to the third free register
        // this enables us to use it with the invoke-static range instruction
        BuilderInstruction32x moveB = new BuilderInstruction32x(Opcode.MOVE_16, thirdFreeRegister, registerB);

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                firstFreeRegister, 3,
                new ImmutableMethodReference(TRACER,
                        "computeBranchDistance",
                        Lists.newArrayList("Ljava/lang/String;", "I", "I"), "V"));

        // check whether if stmt is located within a try block
        if (tryBlocks.stream().anyMatch(range -> range.contains(instrumentationPoint.getPosition()))) {
            /*
             * The bytecode verifier doesn't allow us to insert our functionality directly within
             * try blocks. Actually, only (implicit) try blocks around a synchronized block are affected,
             * but we consider here any try block. The problem arises from the fact that an invoke instruction
             * within a try block introduces an additional edge to corresponding catch blocks, although it may
             * never throw an exception. As a result, the register type of the monitor enter/exit instruction, e.g. v1,
             * might be two-fold (conflicted), which is rejected by the verifier, see
             * https://android.googlesource.com/platform/art/+/master/runtime/verifier/register_line.cc#367.
             *
             * Actually we can bypass the verifier by introducing a jump forward and backward mechanism. Instead of
             * inserting the functionality directly, we insert a goto instruction, which jumps to the end of the
             * method and calls the tracer functionality and afterwards jumps back to the original position. Since
             * a goto instruction can't throw any exception, the verifier doesn't complain. However, we have to ensure
             * that we don't introduce a control flow to the pseudo instructions packed-switch-data, sparse-switch-data
             * or fill-array-data, see the constraint B22 at https://source.android.com/devices/tech/dalvik/constraints.
             *
             * The idea of this kind of hack was taken from the paper 'Fine-grained Code Coverage Measurement in
             * Automated Black-box Android Testing', see section 4.3.
             */

            LOGGER.debug("IF-Statement within try block at offset: "
                    + instrumentationPoint.getInstruction().getLocation().getCodeAddress());

            // the label + tracer functionality comes after the last instruction
            int afterLastInstruction = mutableMethodImplementation.getInstructions().size();

            // insert goto to jump to method end
            Label tracerLabel = mutableMethodImplementation.newLabelForIndex(afterLastInstruction);
            BuilderInstruction jumpForward = new BuilderInstruction30t(Opcode.GOTO_32, tracerLabel);

            // consider always as an 'else branch', the if stmt could be the target of a goto instruction
            mutableMethodImplementation.addInstruction(instructionIndex + 1, jumpForward);
            mutableMethodImplementation.swapInstructions(instructionIndex, instructionIndex + 1);

            // create label at branch after forward jump
            Label branchLabel = mutableMethodImplementation.newLabelForIndex(instructionIndex + 1);

            // insert tracer functionality at label near method end (+1 because we inserted already goto instruction at branch)
            mutableMethodImplementation.addInstruction(afterLastInstruction + 1, operationID);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 2, moveA);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 3, moveB);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 4, invokeStaticRange);

            // insert goto to jump back to branch
            BuilderInstruction jumpBackward = new BuilderInstruction30t(Opcode.GOTO_32, branchLabel);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 5, jumpBackward);
        } else {

            mutableMethodImplementation.addInstruction(++instructionIndex, operationID);
            mutableMethodImplementation.addInstruction(++instructionIndex, moveA);
            mutableMethodImplementation.addInstruction(++instructionIndex, moveB);
            mutableMethodImplementation.addInstruction(++instructionIndex, invokeStaticRange);

            mutableMethodImplementation.swapInstructions(instructionIndex - 4, instructionIndex - 3);
            mutableMethodImplementation.swapInstructions(instructionIndex - 3, instructionIndex - 2);
            mutableMethodImplementation.swapInstructions(instructionIndex - 2, instructionIndex - 1);
            mutableMethodImplementation.swapInstructions(instructionIndex - 1, instructionIndex);
        }

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }

    /**
     * Inserts code to invoke the branch distance computation for an if statement that has
     * a single object argument, e.g. if-eqz v0. We simply call 'branchDistance(int op_type, Object argument)'.
     *
     * @param methodInformation    Encapsulates a method.
     * @param instrumentationPoint Wrapper for if instruction and its index.
     * @param operation            The operation id, e.g. 0 for if-eqz.
     * @param registerA            The register id of the argument register.
     */
    private static void handleObjectUnaryComparison(MethodInformation methodInformation, InstrumentationPoint instrumentationPoint,
                                                    int operation, int registerA) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // the location of try blocks
        Set<Range> tryBlocks = methodInformation.getTryBlocks();

        int instructionIndex = instrumentationPoint.getInstruction().getLocation().getIndex();
        final String trace = methodInformation.getMethodID() + "->" + instrumentationPoint.getPosition();
        final String operationParam = operation + ":" + trace;

        // we require one parameter for the operation identifier
        int firstFreeRegister = methodInformation.getFreeRegisters().get(0);

        // we need another free register for the single argument of the if instruction
        int secondFreeRegister = methodInformation.getFreeRegisters().get(1);

        // const/4 vA, #+B - stores the operation type identifier + trace, e.g. 0:<trace-id> for if-eqz
        BuilderInstruction21c operationID = new BuilderInstruction21c(Opcode.CONST_STRING, firstFreeRegister,
                new ImmutableStringReference(operationParam));

        // we need to move the content of single if instruction argument to the second free register
        // this enables us to use it with the invoke-static range instruction
        BuilderInstruction32x move = new BuilderInstruction32x(Opcode.MOVE_OBJECT_16, secondFreeRegister, registerA);

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                firstFreeRegister, 2,
                new ImmutableMethodReference(TRACER,
                        "computeBranchDistance",
                        Lists.newArrayList("Ljava/lang/String;", "Ljava/lang/Object;"), "V"));

        // check whether if stmt is located within a try block
        if (tryBlocks.stream().anyMatch(range -> range.contains(instrumentationPoint.getPosition()))) {
            /*
             * The bytecode verifier doesn't allow us to insert our functionality directly within
             * try blocks. Actually, only (implicit) try blocks around a synchronized block are affected,
             * but we consider here any try block. The problem arises from the fact that an invoke instruction
             * within a try block introduces an additional edge to corresponding catch blocks, although it may
             * never throw an exception. As a result, the register type of the monitor enter/exit instruction, e.g. v1,
             * might be two-fold (conflicted), which is rejected by the verifier, see
             * https://android.googlesource.com/platform/art/+/master/runtime/verifier/register_line.cc#367.
             *
             * Actually we can bypass the verifier by introducing a jump forward and backward mechanism. Instead of
             * inserting the functionality directly, we insert a goto instruction, which jumps to the end of the
             * method and calls the tracer functionality and afterwards jumps back to the original position. Since
             * a goto instruction can't throw any exception, the verifier doesn't complain. However, we have to ensure
             * that we don't introduce a control flow to the pseudo instructions packed-switch-data, sparse-switch-data
             * or fill-array-data, see the constraint B22 at https://source.android.com/devices/tech/dalvik/constraints.
             *
             * The idea of this kind of hack was taken from the paper 'Fine-grained Code Coverage Measurement in
             * Automated Black-box Android Testing', see section 4.3.
             */

            LOGGER.debug("IF-Statement within try block at offset: "
                    + instrumentationPoint.getInstruction().getLocation().getCodeAddress());

            // the label + tracer functionality comes after the last instruction
            int afterLastInstruction = mutableMethodImplementation.getInstructions().size();

            // insert goto to jump to method end
            Label tracerLabel = mutableMethodImplementation.newLabelForIndex(afterLastInstruction);
            BuilderInstruction jumpForward = new BuilderInstruction30t(Opcode.GOTO_32, tracerLabel);

            // consider always as an 'else branch', the if stmt could be the target of a goto instruction
            mutableMethodImplementation.addInstruction(instructionIndex + 1, jumpForward);
            mutableMethodImplementation.swapInstructions(instructionIndex, instructionIndex + 1);

            // create label at branch after forward jump
            Label branchLabel = mutableMethodImplementation.newLabelForIndex(instructionIndex + 1);

            // insert tracer functionality at label near method end (+1 because we inserted already goto instruction at branch)
            mutableMethodImplementation.addInstruction(afterLastInstruction + 1, operationID);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 2, move);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 3, invokeStaticRange);

            // insert goto to jump back to branch
            BuilderInstruction jumpBackward = new BuilderInstruction30t(Opcode.GOTO_32, branchLabel);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 4, jumpBackward);
        } else {

            mutableMethodImplementation.addInstruction(++instructionIndex, operationID);
            mutableMethodImplementation.addInstruction(++instructionIndex, move);
            mutableMethodImplementation.addInstruction(++instructionIndex, invokeStaticRange);

            mutableMethodImplementation.swapInstructions(instructionIndex - 3, instructionIndex - 2);
            mutableMethodImplementation.swapInstructions(instructionIndex - 2, instructionIndex - 1);
            mutableMethodImplementation.swapInstructions(instructionIndex - 1, instructionIndex);
        }

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }

    /**
     * Inserts code to invoke the branch distance computation for an if statement that has
     * two object arguments, e.g. if-eq v0, v1. We simply call '
     * branchDistance(int op_type, Object argument1, Object argument2)'.
     *
     * @param methodInformation    Encapsulates a method.
     * @param instrumentationPoint Wrapper for if instruction and its index.
     * @param operation            The operation id, e.g. 0 for if-eq v0, v1.
     * @param registerA            The register id of the first argument register.
     * @param registerB            The register id of the second argument register.
     */
    private static void handleObjectBinaryComparison(MethodInformation methodInformation, InstrumentationPoint instrumentationPoint,
                                                     int operation, int registerA, int registerB) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // the location of try blocks
        Set<Range> tryBlocks = methodInformation.getTryBlocks();

        int instructionIndex = instrumentationPoint.getInstruction().getLocation().getIndex();
        final String trace = methodInformation.getMethodID() + "->" + instrumentationPoint.getPosition();
        final String operationParam = operation + ":" + trace;

        // we require one parameter for the operation identifier
        int firstFreeRegister = methodInformation.getFreeRegisters().get(0);

        // we need another free register for the first argument of the if instruction
        int secondFreeRegister = methodInformation.getFreeRegisters().get(1);

        // we need another free register for the second argument of the if instruction
        int thirdFreeRegister = methodInformation.getFreeRegisters().get(2);

        // const/4 vA, #+B - stores the operation type identifier + trace, e.g. 0:<trace-id> for if-eqz
        BuilderInstruction21c operationID = new BuilderInstruction21c(Opcode.CONST_STRING, firstFreeRegister,
                new ImmutableStringReference(operationParam));

        // we need to move the content of the first if instruction argument to the second free register
        // this enables us to use it with the invoke-static range instruction
        BuilderInstruction32x moveA = new BuilderInstruction32x(Opcode.MOVE_OBJECT_16, secondFreeRegister, registerA);

        // we need to move the content of the second if instruction argument to the third free register
        // this enables us to use it with the invoke-static range instruction
        BuilderInstruction32x moveB = new BuilderInstruction32x(Opcode.MOVE_OBJECT_16, thirdFreeRegister, registerB);

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                firstFreeRegister, 3,
                new ImmutableMethodReference(TRACER,
                        "computeBranchDistance",
                        Lists.newArrayList("Ljava/lang/String;", "Ljava/lang/Object;", "Ljava/lang/Object;"),
                        "V"));

        // check whether if stmt is located within a try block
        if (tryBlocks.stream().anyMatch(range -> range.contains(instrumentationPoint.getPosition()))) {
            /*
             * The bytecode verifier doesn't allow us to insert our functionality directly within
             * try blocks. Actually, only (implicit) try blocks around a synchronized block are affected,
             * but we consider here any try block. The problem arises from the fact that an invoke instruction
             * within a try block introduces an additional edge to corresponding catch blocks, although it may
             * never throw an exception. As a result, the register type of the monitor enter/exit instruction, e.g. v1,
             * might be two-fold (conflicted), which is rejected by the verifier, see
             * https://android.googlesource.com/platform/art/+/master/runtime/verifier/register_line.cc#367.
             *
             * Actually we can bypass the verifier by introducing a jump forward and backward mechanism. Instead of
             * inserting the functionality directly, we insert a goto instruction, which jumps to the end of the
             * method and calls the tracer functionality and afterwards jumps back to the original position. Since
             * a goto instruction can't throw any exception, the verifier doesn't complain. However, we have to ensure
             * that we don't introduce a control flow to the pseudo instructions packed-switch-data, sparse-switch-data
             * or fill-array-data, see the constraint B22 at https://source.android.com/devices/tech/dalvik/constraints.
             *
             * The idea of this kind of hack was taken from the paper 'Fine-grained Code Coverage Measurement in
             * Automated Black-box Android Testing', see section 4.3.
             */

            LOGGER.debug("IF-Statement within try block at offset: "
                    + instrumentationPoint.getInstruction().getLocation().getCodeAddress());

            // the label + tracer functionality comes after the last instruction
            int afterLastInstruction = mutableMethodImplementation.getInstructions().size();

            // insert goto to jump to method end
            Label tracerLabel = mutableMethodImplementation.newLabelForIndex(afterLastInstruction);
            BuilderInstruction jumpForward = new BuilderInstruction30t(Opcode.GOTO_32, tracerLabel);

            // consider always as an 'else branch', the if stmt could be the target of a goto instruction
            mutableMethodImplementation.addInstruction(instructionIndex + 1, jumpForward);
            mutableMethodImplementation.swapInstructions(instructionIndex, instructionIndex + 1);

            // create label at branch after forward jump
            Label branchLabel = mutableMethodImplementation.newLabelForIndex(instructionIndex + 1);

            // insert tracer functionality at label near method end (+1 because we inserted already goto instruction at branch)
            mutableMethodImplementation.addInstruction(afterLastInstruction + 1, operationID);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 2, moveA);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 3, moveB);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 4, invokeStaticRange);

            // insert goto to jump back to branch
            BuilderInstruction jumpBackward = new BuilderInstruction30t(Opcode.GOTO_32, branchLabel);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 5, jumpBackward);
        } else {

            mutableMethodImplementation.addInstruction(++instructionIndex, operationID);
            mutableMethodImplementation.addInstruction(++instructionIndex, moveA);
            mutableMethodImplementation.addInstruction(++instructionIndex, moveB);
            mutableMethodImplementation.addInstruction(++instructionIndex, invokeStaticRange);

            mutableMethodImplementation.swapInstructions(instructionIndex - 4, instructionIndex - 3);
            mutableMethodImplementation.swapInstructions(instructionIndex - 3, instructionIndex - 2);
            mutableMethodImplementation.swapInstructions(instructionIndex - 2, instructionIndex - 1);
            mutableMethodImplementation.swapInstructions(instructionIndex - 1, instructionIndex);
        }

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }

    /**
     * Maps an opcode to an internal operation type identifier.
     *
     * @param opcode The given opcode.
     * @return Returns the internal operation type identifier for the given opcode.
     */
    private static int mapOpCodeToOperation(Opcode opcode) {

        switch (opcode) {
            case IF_EQZ:
            case IF_EQ:
                return 0;
            case IF_NEZ:
            case IF_NE:
                return 1;
            case IF_LEZ:
            case IF_LE:
                return 2;
            case IF_LTZ:
            case IF_LT:
                return 3;
            case IF_GEZ:
            case IF_GE:
                return 4;
            case IF_GTZ:
            case IF_GT:
                return 5;
            default:
                throw new UnsupportedOperationException("Opcode: " + opcode + " not yet supported!");
        }
    }

    /**
     * Inserts move instructions at the method entry in order to shift the parameter registers by
     * one position to the left, e.g. move p0, p1. This is necessary to make the last register(s)
     * free usable. Note that the move instructions depend on the type of the source register and
     * that wide types take up two consecutive registers, which is the actual need for two
     * additional registers, since p0 may have type wide in static methods.
     *
     * @param methodInformation Stores all relevant information about a method.
     */
    public static void shiftParamRegisters(MethodInformation methodInformation) {

        assert methodInformation.getParamRegisterTypeMap().isPresent();

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        Map<Integer, RegisterType> paramRegisterMap = methodInformation.getParamRegisterTypeMap().get();
        LOGGER.info(paramRegisterMap.toString());

        List<Integer> newRegisters = methodInformation.getNewRegisters();
        List<Integer> paramRegisters = methodInformation.getParamRegisters();

        // compute the list of destination registers, which ranges from newReg(0)...newReg(0) + #params - 1
        // TODO: should be simple the place of the original param regs, i.e. destRegs = paramRegs
        int firstDestinationRegister = newRegisters.get(0);
        int lastDestinationRegister = newRegisters.get(0) + paramRegisters.size();
        List<Integer> destinationRegisters = IntStream.
                range(firstDestinationRegister, lastDestinationRegister).boxed().collect(Collectors.toList());

        // compute the list of source registers, which is the param registers shifted by #additionalRegs
        List<Integer> sourceRegisters =
                paramRegisters.stream().map(elem -> elem + BasicBlockBranchDistance.ADDITIONAL_REGISTERS).collect(Collectors.toList());

        LOGGER.info("New Registers: " + newRegisters);
        LOGGER.info("Parameter Registers: " + paramRegisters);
        LOGGER.info("Destination Registers: " + destinationRegisters);
        LOGGER.info("Source Registers: " + sourceRegisters);

        // we need a separate counter for the insertion location of the instructions,
        // since we skip indices when facing wide types
        int pos = 0;

        // use correct move instruction depend on type of source register
        for (int index = 0; index < sourceRegisters.size(); index++) {

            // id corresponds to actual register ID of param register
            RegisterType registerType = paramRegisterMap.get(paramRegisters.get(index));

            // check whether we have a wide type or not, note that first comes low half, then high half
            if (registerType == RegisterType.LONG_LO_TYPE
                    || registerType == RegisterType.DOUBLE_LO_TYPE) {

                Opcode moveWide = Opcode.MOVE_WIDE_FROM16;

                LOGGER.info("Wide type LOW_HALF!");

                // destination register : {vnew0,vnew1,p0...pn}\{pn-1,pn}
                int destinationRegisterID = destinationRegisters.get(index);

                // source register : p0...pN
                int sourceRegisterID = sourceRegisters.get(index);

                LOGGER.info("Destination reg: " + destinationRegisterID);
                LOGGER.info("Source reg: " + sourceRegisterID);

                // move wide vNew, vShiftedOut
                BuilderInstruction22x move = new BuilderInstruction22x(moveWide, destinationRegisterID, sourceRegisterID);
                // add move as first instruction
                mutableMethodImplementation.addInstruction(pos, move);
                pos++;
            } else if (registerType == RegisterType.LONG_HI_TYPE
                    || registerType == RegisterType.DOUBLE_HI_TYPE) {

                LOGGER.info("Wide type HIGH_HALF!");

                // we reached the upper half of a wide-type, no additional move instruction necessary
                LOGGER.info("(Skipping) source reg:" + sourceRegisters.get(index));
                LOGGER.info("(Skipping) destination reg: " + destinationRegisters.get(index));
                continue;
            } else if (registerType.category == RegisterType.REFERENCE
                    || registerType.category == RegisterType.NULL
                    || registerType.category == RegisterType.UNINIT_THIS
                    || registerType.category == RegisterType.UNINIT_REF) {

                // object type
                Opcode moveObject = Opcode.MOVE_OBJECT_FROM16;


                LOGGER.info("Object type!");

                int destinationRegisterID = destinationRegisters.get(index);
                int sourceRegisterID = sourceRegisters.get(index);

                LOGGER.info("Destination reg: " + destinationRegisterID);
                LOGGER.info("Source reg: " + sourceRegisterID);

                BuilderInstruction22x move = new BuilderInstruction22x(moveObject, destinationRegisterID, sourceRegisterID);
                mutableMethodImplementation.addInstruction(pos, move);
                pos++;
            } else {
                if (registerType.category == RegisterType.CONFLICTED) {
                    /* Conflicted types cannot be read from and need to be treated as uninitialized memory
                     * See: https://stackoverflow.com/questions/55047978/dalvik-verifier-copy1-v16-v22-type-2-cat-1
                     *
                     * Found in APK bbc.mobile.news.ww, in file
                     *  bbc.mobile.news.ww/out/com/google/android/exoplayer/extractor/mp4/FragmentedMp4Extractor.smali
                     *  in method
                     *  read(Lcom/google/android/exoplayer/extractor/ExtractorInput;Lcom/google/android/exoplayer/extractor/PositionHolder;)I
                     *  which can be obtained by running
                     *      apktool d -s bbc.mobile.news.ww.apk
                     *      cd bbc.mobile.news.ww
                     *      baksmali-2.4.0.jar d -b "" --register-info ALL,FULLMERGE --offsets classes2.dex
                     *
                     * A conflicted type must be written to, before it can be read from. So we just set it to the dummy
                     * value 0. Then we can move from the register (which required reading from the register).
                     *
                     * We can set the value to 0 regardless of what the possible type of the value is because all-zeros
                     * is a valid bit representation for any type:
                     *     1) For a object reference we have (Object) null == (int) 0.
                     *     2) For a char we have (char) 0 == '\0'.
                     *     3) For any integer type (that is byte, short, int and long) we have (double) 0 == (int) 0.
                     *     4) For floating point type (that is float, double) we have (float) -0 == (int) 0.
                     *     5) For boolean we have (boolean) false == (int) 0.
                     *
                     *  Assuming the bytecode was valid before the instrumentation we know that the dummy value will
                     *  only be read by the move instruction added below. The dummy value can not be read by any other
                     *  code, because
                     *      1) We do not add any other instrumentation code which reads the dummy value, besides the one
                     *             move operation below.
                     *      2) Any instruction that we did not add still assumes that the type of the register is
                     *             conflicted which means the register has to be written to (thus overwriting our 0)
                     *             before it can be read from.
                     */
                    LOGGER.info("Conflicted type: " + sourceRegisters.get(index));
                    final BuilderInstruction11n setZero = new BuilderInstruction11n(Opcode.CONST_4, sourceRegisters.get(index), 0);
                    mutableMethodImplementation.addInstruction(pos, setZero);
                    pos++;
                }

                // primitive type
                Opcode movePrimitive = Opcode.MOVE_FROM16;

                LOGGER.info("Primitive type!");

                int destinationRegisterID = destinationRegisters.get(index);
                int sourceRegisterID = sourceRegisters.get(index);

                LOGGER.info("Destination reg: " + destinationRegisterID);
                LOGGER.info("Source reg: " + sourceRegisterID);

                BuilderInstruction22x move = new BuilderInstruction22x(movePrimitive, destinationRegisterID, sourceRegisterID);
                mutableMethodImplementation.addInstruction(pos, move);
                pos++;
            }
        }
        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }

    /**
     * Adds a basic lifecycle method to the given activity or fragment class. This already includes the instrumentation
     * of the method.
     * <p>
     * NOTE: We don't assign the instruction index to the entry and exit traces, since the original APK don't contain
     * these methods and in the graph those methods would be represented by dummy CFGs not defining any instruction
     * vertex. Hence, the lookup would fail.
     *
     * @param method   The lifecycle method name.
     * @param classDef The activity or fragment class.
     * @param superClasses The super classes of the activity or fragment class.
     * @return Returns the instrumented lifecycle method or {@code null} if the lifecycle method couldn't be instrumented.
     */
    public static Method addLifeCycleMethod(final String method, final ClassDef classDef, final List<ClassDef> superClasses) {

        // TODO: Fix implementation for basic block branch distance

        /*
         * We need to check that overriding the lifecycle method is actually permitted. It can happen that the method
         * is declared final in one of its super classes. In this case, we can't overwrite the lifecycle method.
         */
        for (ClassDef superClass : superClasses) {
            for (Method m : superClass.getMethods()) {
                if (m.toString().endsWith(method)) {
                    if (Arrays.stream(AccessFlags.getAccessFlagsForMethod(m.getAccessFlags()))
                            .anyMatch(flag -> flag == AccessFlags.FINAL)) {
                        LOGGER.info("Can't add lifecycle method " + method
                                + " because the method is declared final in the super class " + superClass + "!");
                        return null;
                    }
                }
            }
        }

        String superClass = classDef.getSuperclass();

        String methodName = method.split("\\(")[0];
        String parameters = method.split("\\(")[1].split("\\)")[0];
        String returnType = method.split("\\)")[1];

        // ASSUMPTION: all parameters are objects (this is true for lifecycle methods)
        int paramCount = StringUtils.countMatches(parameters, ";");
        List<String> params = new ArrayList<>();

        if (paramCount > 0) {
            // the params have the form L../../../..; e.g. Landroid/view/View;
            params = Arrays.stream(parameters.split(";")).map(param -> param + ";").collect(Collectors.toList());
        }

        MutableMethodImplementation implementation;

        if (returnType.equals("V")) {

            // one local register v0 required -> p0 has index 0
            int paramIndex = 1;

            // we require one additional parameter for the invisible 'this'-reference p0 + one for trace
            implementation = new MutableMethodImplementation(2 + paramCount);

            // call super method (we have one register for this reference + one register for each parameter)
            implementation.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_SUPER, 1 + paramCount,
                    paramIndex++, paramIndex++, paramIndex++, paramIndex++, paramIndex++,
                    new ImmutableMethodReference(superClass, methodName, params, returnType)));

            // we have to add return-statement as well, though void!
            implementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
        } else {

            // two local registers v0,v1 required -> p0 has index 2
            int paramIndex = 2;

            // we require one additional parameter for the invisible 'this'-reference p0, one for the trace and one
            // for the return value
            implementation = new MutableMethodImplementation(3 + paramCount);

            // call super method (we have one register for this reference + one register for each parameter)
            implementation.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_SUPER, 1 + paramCount,
                    paramIndex++, paramIndex++, paramIndex++, paramIndex++, paramIndex++,
                    new ImmutableMethodReference(superClass, methodName, params, returnType)));

            // move-result v1
            implementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, 1));

            // only onCreateView(..) returns Landroid/view/View; which is stored in v1
            implementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN_OBJECT, 1));
        }

        // Add basic block tracer instrumentation.
        int instructionCount = implementation.getInstructions().size();
        implementation.addInstruction(0,new BuilderInstruction21c(Opcode.CONST_STRING, 0,
                new ImmutableStringReference(classDef + "->" + method + "->0->" + instructionCount + "->noBranch")));

        // invoke-static-range
        implementation.addInstruction(1,new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                0, 1,
                new ImmutableMethodReference(TRACER, "trace",
                        Lists.newArrayList("Ljava/lang/String;"), "V")));


        List<MethodParameter> methodParams = params.stream().map(p ->
                new ImmutableMethodParameter(p, null,
                        // use three random letters as param names
                        RandomStringUtils.random(3, true, false).toLowerCase()))
                .collect(Collectors.toList());

        return new ImmutableMethod(
                classDef.toString(),
                methodName,
                methodParams,
                returnType,
                4,
                null,
                null,
                implementation);
    }
}
