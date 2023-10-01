package de.uni_passau.fim.auermich.instrumentation.basicblockcoverage.core;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.instrumentation.basicblockcoverage.BasicBlockCoverage;
import de.uni_passau.fim.auermich.instrumentation.basicblockcoverage.dto.MethodInformation;
import de.uni_passau.fim.auermich.instrumentation.basicblockcoverage.utility.Range;
import de.uni_passau.fim.auermich.instrumentation.basicblockcoverage.utility.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.analysis.RegisterType;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.MethodImplementation;
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

        // the position of the basic block
        int index = instrumentationPoint.getPosition();

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
                LOGGER.debug("Position of new label: " + instruction.getLocation().getIndex() + 1);
                return instruction.getLocation().getIndex() + 1;
            }
        }
        throw new IllegalStateException("No free label position found for method: " + methodInformation.getMethodID());
    }

    /**
     * Performs the actual instrumentation. Inserts at each instrumentation point, i.e. at each basic block, a trace
     * statement.
     *
     * @param methodInformation Encapsulates a method and its instrumentation points.
     */
    public static void modifyMethod(MethodInformation methodInformation) {

        LOGGER.debug("Register count before increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        // increase the register count of the method, i.e. the .register directive at each method's head
        Utility.increaseMethodRegisterCount(methodInformation, methodInformation.getTotalRegisterCount());

        LOGGER.debug("Register count after increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        TreeSet<InstrumentationPoint> instrumentationPoints
                = (TreeSet<InstrumentationPoint>) methodInformation.getInstrumentationPoints();
        Iterator<InstrumentationPoint> iterator = instrumentationPoints.descendingIterator();

        /*
         * Traverse the basic blocks backwards, i.e. the last basic block comes first, in order
         * to avoid inherent index/position updates of other basic blocks while instrumenting.
         */
        while (iterator.hasNext()) {
            InstrumentationPoint instrumentationPoint = iterator.next();
            String isBranch = instrumentationPoint.hasBranchType() ? "isBranch" : "noBranch";
            String trace = methodInformation.getMethodID() + "->" + instrumentationPoint.getPosition() + "->"
                    + instrumentationPoint.getCoveredInstructions() + "->" + isBranch;

            insertInstrumentationCode(methodInformation, instrumentationPoint, trace);
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
        LOGGER.debug(paramRegisterMap.toString());

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
                paramRegisters.stream().map(elem -> elem + BasicBlockCoverage.ADDITIONAL_REGISTERS).collect(Collectors.toList());

        LOGGER.debug("New Registers: " + newRegisters);
        LOGGER.debug("Parameter Registers: " + paramRegisters);
        LOGGER.debug("Destination Registers: " + destinationRegisters);
        LOGGER.debug("Source Registers: " + sourceRegisters);

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

                LOGGER.debug("Wide type LOW_HALF!");

                // destination register : {vnew0,vnew1,p0...pn}\{pn-1,pn}
                int destinationRegisterID = destinationRegisters.get(index);

                // source register : p0...pN
                int sourceRegisterID = sourceRegisters.get(index);

                LOGGER.debug("Destination reg: " + destinationRegisterID);
                LOGGER.debug("Source reg: " + sourceRegisterID);

                // move wide vNew, vShiftedOut
                BuilderInstruction22x move = new BuilderInstruction22x(moveWide, destinationRegisterID, sourceRegisterID);
                // add move as first instruction
                mutableMethodImplementation.addInstruction(pos, move);
                pos++;
            } else if (registerType == RegisterType.LONG_HI_TYPE
                    || registerType == RegisterType.DOUBLE_HI_TYPE) {

                LOGGER.debug("Wide type HIGH_HALF!");

                // we reached the upper half of a wide-type, no additional move instruction necessary
                LOGGER.debug("(Skipping) source reg:" + sourceRegisters.get(index));
                LOGGER.debug("(Skipping) destination reg: " + destinationRegisters.get(index));
                continue;
            } else if (registerType.category == RegisterType.REFERENCE
                    || registerType.category == RegisterType.NULL
                    || registerType.category == RegisterType.UNINIT_THIS
                    || registerType.category == RegisterType.UNINIT_REF) {

                // object type
                Opcode moveObject = Opcode.MOVE_OBJECT_FROM16;


                LOGGER.debug("Object type!");

                int destinationRegisterID = destinationRegisters.get(index);
                int sourceRegisterID = sourceRegisters.get(index);

                LOGGER.debug("Destination reg: " + destinationRegisterID);
                LOGGER.debug("Source reg: " + sourceRegisterID);

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
                    LOGGER.debug("Conflicted type: " + sourceRegisters.get(index));
                    final BuilderInstruction11n setZero = new BuilderInstruction11n(Opcode.CONST_4, sourceRegisters.get(index), 0);
                    mutableMethodImplementation.addInstruction(pos, setZero);
                    pos++;
                }

                // primitive type
                Opcode movePrimitive = Opcode.MOVE_FROM16;

                LOGGER.debug("Primitive type!");

                int destinationRegisterID = destinationRegisters.get(index);
                int sourceRegisterID = sourceRegisters.get(index);

                LOGGER.debug("Destination reg: " + destinationRegisterID);
                LOGGER.debug("Source reg: " + sourceRegisterID);

                BuilderInstruction22x move = new BuilderInstruction22x(movePrimitive, destinationRegisterID, sourceRegisterID);
                mutableMethodImplementation.addInstruction(pos, move);
                pos++;
            }
        }
        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }
}
