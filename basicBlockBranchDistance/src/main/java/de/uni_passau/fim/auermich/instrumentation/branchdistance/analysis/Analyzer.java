package de.uni_passau.fim.auermich.instrumentation.branchdistance.analysis;


import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.core.InstrumentationPoint;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.dto.MethodInformation;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.utility.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.analysis.*;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.BuilderOffsetInstruction;
import org.jf.dexlib2.builder.BuilderSwitchPayload;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction31t;
import org.jf.dexlib2.builder.instruction.BuilderSwitchElement;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.util.MethodUtil;

import java.util.*;
import java.util.stream.Collectors;

public final class Analyzer {

    private static final Logger LOGGER = LogManager.getLogger(Analyzer.class);

    /**
     * Tracks the if and switch instrumentation points, i.e. instructions that represent if or switch statements.
     *
     * @param methodInformation Encapsulates a method.
     * @return Returns the set of if and switch instrumentation points.
     */
    public static Set<InstrumentationPoint> trackIfAndSwitchInstrumentationPoints(final MethodInformation methodInformation) {

        final List<AnalyzedInstruction> instructions = methodInformation.getInstructions();
        final List<BuilderInstruction> builderInstructions
                = new MutableMethodImplementation(methodInformation.getMethodImplementation()).getInstructions();
        final Set<InstrumentationPoint> ifAndSwitchInstrumentationPoints = new TreeSet<>();

        LOGGER.debug("Method if statements: " + methodInformation.getMethodID());
        LOGGER.debug("Instructions: " + instructions.size());

        for (final AnalyzedInstruction instruction : instructions) {
            final int index = instruction.getInstructionIndex();

            if (isIfInstruction(instruction)) {
                LOGGER.debug("If statement at index: " + index);
                final BuilderInstruction ifInstruction = builderInstructions.get(index);
                final InstrumentationPoint ifIP = new InstrumentationPoint(ifInstruction, InstrumentationPoint.Type.IF_STMT);
                ifAndSwitchInstrumentationPoints.add(ifIP);
            } else if (isSwitchInstruction(instruction)) {
                final BuilderInstruction switchInstruction = builderInstructions.get(index);
                int switchPayloadPosition = ((BuilderInstruction31t) switchInstruction).getTarget().getLocation().getIndex();
                BuilderSwitchPayload switchPayloadInstruction
                        = (BuilderSwitchPayload) builderInstructions.get(switchPayloadPosition);
                // We need to compute a branch distance similar to a regular if instruction.
                InstrumentationPoint switchIP = new InstrumentationPoint(switchInstruction, switchPayloadInstruction,
                        InstrumentationPoint.Type.SWITCH_STMT);
                ifAndSwitchInstrumentationPoints.add(switchIP);

                // Check if switch statement has an explicit default branch.
                if (switchPayloadInstruction.getSwitchElements()
                        .stream()
                        .anyMatch(element -> element.getTarget().getLocation().getIndex()
                                == switchInstruction.getLocation().getIndex() + 1)) {
                    switchIP.setContainsDefaultBranch();
                }
            }
        }

        return ifAndSwitchInstrumentationPoints;
    }

    /**
     * Tracks the instrumentation points, i.e. instructions that define new basic blocks.
     *
     * @param methodInformation Encapsulates a method.
     * @return Returns the set of instrumentation points.
     */
    public static Set<InstrumentationPoint> trackBasicBlockInstrumentationPoints(final MethodInformation methodInformation) {

        final List<AnalyzedInstruction> instructions = methodInformation.getInstructions();
        final List<BuilderInstruction> builderInstructions
                = new MutableMethodImplementation(methodInformation.getMethodImplementation()).getInstructions();
        final Set<InstrumentationPoint> instrumentationPoints = new TreeSet<>();

        // the first instruction defines a new basic block
        instrumentationPoints.add(new InstrumentationPoint(builderInstructions.get(0),
                InstrumentationPoint.Type.NO_BRANCH));

        // each entry refers to the code address of the first instruction within a catch block
        final Set<Integer> catchBlocks = methodInformation.getMethodImplementation()
                .getTryBlocks().stream().flatMap(t -> t.getExceptionHandlers().stream())
                .map(ExceptionHandler::getHandlerCodeAddress).collect(Collectors.toSet());
        LOGGER.debug("Catch blocks located at code addresses: " + catchBlocks);

        int consumedCodeUnits = 0;

        /*
         * We only compare instrumentation points by their position, this means it can happen that another instrumentation
         * point for the same position was added previously. However, we need to ensure that a 'branch' instrumentation
         * point has a higher priority, thus we keep track of branch instrumentation points in a second set. This is
         * necessary to construct the correct traces for the branch coverage computation.
         * If we can tell which kind of label (goto, branch, try/catch) is attached to the instruction,
         * we could avoid this. Track the progress of: https://github.com/JesusFreke/smali/issues/808.
         */
        final Set<InstrumentationPoint> branches = new HashSet<>();

        for (final AnalyzedInstruction instruction : instructions) {
            final int index = instruction.getInstructionIndex();

            // branches define a new basic block
            if (isIfInstruction(instruction)) {

                LOGGER.debug("If branch at index: " + index);

                final int ifTarget = instruction.getInstructionIndex() + 1;
                LOGGER.debug("If target at index: " + ifTarget);

                final BuilderInstruction ifTargetInstruction = builderInstructions.get(ifTarget);
                final InstrumentationPoint ifBranchIP
                        = new InstrumentationPoint(ifTargetInstruction, InstrumentationPoint.Type.IS_BRANCH);
                branches.add(ifBranchIP);

                final int elseTarget = ((BuilderOffsetInstruction) builderInstructions.get(index)).getTarget()
                        .getLocation().getIndex();
                LOGGER.debug("Else target at index: " + elseTarget);

                final BuilderInstruction elseTargetInstruction = builderInstructions.get(elseTarget);
                final InstrumentationPoint elseBranchIP
                        = new InstrumentationPoint(elseTargetInstruction, InstrumentationPoint.Type.IS_BRANCH);
                branches.add(elseBranchIP);

            } else if (isGotoInstruction(instruction)) {
                // the target of a goto instruction defines a new basic block

                LOGGER.debug("Found goto instruction at index: " + index);
                final int target = ((BuilderOffsetInstruction) builderInstructions.get(index)).getTarget()
                        .getLocation().getIndex();
                final BuilderInstruction targetInstruction = builderInstructions.get(target);
                LOGGER.debug("Found goto target at index: " + target);
                final InstrumentationPoint ip
                        = new InstrumentationPoint(targetInstruction, InstrumentationPoint.Type.NO_BRANCH);
                instrumentationPoints.add(ip);
            } else if (isSwitchInstruction(instruction)) {
                // each case of the switch defines a new basic block

                LOGGER.debug("Found switch instruction at index: " + index);

                /*
                 * The packed-switch instruction defines a switch-case construct. To access the individual cases of
                 * the switch statement, one needs to follow the packed-switch-payload instruction that contains this
                 * information. However, the default case or fall-through case is not listed in this payload instruction
                 * and needs to be addressed explicitly. This is simply the next instruction following the switch instruction.
                 */
                final BuilderInstruction31t switchInstruction = (BuilderInstruction31t) builderInstructions.get(index);
                int switchPayloadPosition = switchInstruction.getTarget().getLocation().getIndex();

                BuilderSwitchPayload switchPayloadInstruction
                        = (BuilderSwitchPayload) builderInstructions.get(switchPayloadPosition);

                LOGGER.debug("Number of case statements: " + switchPayloadInstruction.getSwitchElements().size() + 1);

                for (BuilderSwitchElement switchElement : switchPayloadInstruction.getSwitchElements()) {
                    int switchCasePosition = switchElement.getTarget().getLocation().getIndex();
                    InstrumentationPoint switchCase
                            = new InstrumentationPoint(builderInstructions.get(switchCasePosition),
                            InstrumentationPoint.Type.IS_BRANCH);
                    branches.add(switchCase);
                }

                // the direct successor of the switch instruction represents the fall-through case (default case)
                int defaultCasePosition = switchInstruction.getLocation().getIndex() + 1;
                InstrumentationPoint defaultCase = new InstrumentationPoint(builderInstructions.get(defaultCasePosition),
                        InstrumentationPoint.Type.IS_BRANCH);
                branches.add(defaultCase);
            }

            // the first instruction in a catch block defines a new basic block
            if (!catchBlocks.isEmpty()) {
                if (catchBlocks.contains(consumedCodeUnits)) {
                    LOGGER.debug("First instruction within catch-block at index: " + index);
                    instrumentationPoints.add(new InstrumentationPoint(builderInstructions.get(index),
                            InstrumentationPoint.Type.NO_BRANCH));
                }
                consumedCodeUnits += instruction.getInstruction().getCodeUnits();
            }

            /*
            * Every other instruction that has more than one successor also defines a new basic block.
            * Those instructions are within try blocks and have a link to the attached catch block. In particular,
            * any instruction within a try block where the direct successor can potentially throw an exception is affected.
            * That is like control flow would not execute an instruction if it has thrown an exception, i.e. the
            * predecessor defines the link to the catch block and not the instruction itself.
             */
            final Set<Integer> successors = instruction.getSuccessors().stream()
                    .map(AnalyzedInstruction::getInstructionIndex).collect(Collectors.toSet());
            if (successors.size() >= 2) {
                LOGGER.debug("Exceptional flow!");
                LOGGER.debug("From: " + index);
                for (final int successor : successors) {
                    LOGGER.debug("    To: " + successor);
                    final BuilderInstruction targetInstruction = builderInstructions.get(successor);
                    instrumentationPoints.add(new InstrumentationPoint(targetInstruction, InstrumentationPoint.Type.NO_BRANCH));
                }
            }
        }

        assert !instrumentationPoints.isEmpty() : "Should always have at least one instrumentation point.";

        /*
         * We only need to instrument each basic block once, but instrumentation points referring to branches have a
         * higher priority. This is necessary to ensure that branch coverage is computed correctly. Thus, since IPs
         * are only compared based on their positions, we remove and add the branch IPs. This will effectively remove
         * all other IPs that were co-located with branch IPs.
         */
        instrumentationPoints.removeAll(branches);
        instrumentationPoints.addAll(branches);

        // assign the size (number of covered instructions) to each basic block
        final Iterator<InstrumentationPoint> ascendingIterator = instrumentationPoints.iterator();
        InstrumentationPoint current = ascendingIterator.next();
        while (ascendingIterator.hasNext()) {
            final InstrumentationPoint next = ascendingIterator.next();
            current.setCoveredInstructions(next.getPosition() - current.getPosition());
            current = next;
        }

        // set the size of the last basic block
        current.setCoveredInstructions(instructions.size() - current.getPosition());

        LOGGER.info(instrumentationPoints.toString());
        return instrumentationPoints;
    }

    /**
     * Checks whether the given instruction refers to a goto instruction.
     *
     * @param analyzedInstruction The instruction to be analyzed.
     * @return Returns {@code true} if the instruction is a goto instruction, otherwise {@code false} is returned.
     */
    public static boolean isGotoInstruction(AnalyzedInstruction analyzedInstruction) {
        Instruction instruction = analyzedInstruction.getInstruction();
        EnumSet<Format> gotoInstructions = EnumSet.of(Format.Format10t, Format.Format20t, Format.Format30t);
        return gotoInstructions.contains(instruction.getOpcode().format);
    }

    /**
     * Checks whether the given instruction refers to an if instruction.
     *
     * @param analyzedInstruction The instruction to be analyzed.
     * @return Returns {@code true} if the instruction is a branching instruction, otherwise {@code false} is returned.
     */
    public static boolean isIfInstruction(final AnalyzedInstruction analyzedInstruction) {
        final Instruction instruction = analyzedInstruction.getInstruction();
        final EnumSet<Format> branchingInstructions = EnumSet.of(Format.Format21t, Format.Format22t);
        return branchingInstructions.contains(instruction.getOpcode().format);
    }

    /**
     * Checks whether the given instruction refers to a switch instruction.
     *
     * @param analyzedInstruction The instruction to be analyzed.
     * @return Returns {@code true} if the instruction is a switch instruction, otherwise {@code false} is returned.
     */
    public static boolean isSwitchInstruction(final AnalyzedInstruction analyzedInstruction) {
        final Instruction instruction = analyzedInstruction.getInstruction();
        return instruction.getOpcode().format == Format.Format31t
                && instruction.getOpcode() != Opcode.FILL_ARRAY_DATA;       // sparse-/packed-switch instruction
    }

    /**
     * Returns a sorted set of try blocks. Each try block is identified by its start and end index.
     *
     * @param methodInformation Encapsulates a method.
     * @return Returns ranges describing the start and end of try blocks.
     */
    public static Set<Range> getTryBlocks(MethodInformation methodInformation) {

        LOGGER.info("Retrieving try blocks of method...");

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();

        MutableMethodImplementation mutableMethodImplementation =
                new MutableMethodImplementation(methodInformation.getMethodImplementation());

        Set<Range> tryBlocks = new TreeSet<>();

        LOGGER.info("Number of try blocks: " + methodImplementation.getTryBlocks().size());

        // TODO: this can be done in one pass over the instructions
        for (TryBlock<? extends ExceptionHandler> tryBlock : methodImplementation.getTryBlocks()) {

            LOGGER.info("Try block size: " + tryBlock.getCodeUnitCount() + " code units");
            LOGGER.info("Try block start address: " + tryBlock.getStartCodeAddress());
            LOGGER.info("Associated catch blocks: " + tryBlock.getExceptionHandlers().size());

            int consumedCodeUnits = 0;
            BuilderInstruction startInstructionTryBlock = null;
            BuilderInstruction endInstructionTryBlock = null;

            for (BuilderInstruction instruction : mutableMethodImplementation.getInstructions()) {

                /*
                 * The relation between a code unit and an instruction is as follows:
                 *
                 * code unit | instruction
                 *      0
                 *               instr1
                 *      k
                 *               instr2
                 *      n
                 *
                 * This means to check whether we reached a starting point, e.g., the first instruction
                 * of a try block, we need to compare the code unit counter before consuming the next instruction.
                 *
                 * However, if we want to check some end point, e.g., the end of a try block, we need to compare
                 * the code unit counter after the consumption of the next instruction.
                 */

                // the starting point is before the actual instruction
                if (consumedCodeUnits == tryBlock.getStartCodeAddress()) {
                    startInstructionTryBlock = instruction;
                }

                // the end point is after the actual instruction
                if (consumedCodeUnits + instruction.getCodeUnits() == tryBlock.getStartCodeAddress()
                        + tryBlock.getCodeUnitCount()) {
                    endInstructionTryBlock = instruction;
                    break;
                }

                consumedCodeUnits += instruction.getCodeUnits();
            }

            // the instruction indices describe the range of the try block
            int startOfTryBlock = startInstructionTryBlock.getLocation().getIndex();
            int endOfTryBlock = endInstructionTryBlock.getLocation().getIndex();

            LOGGER.info("First instruction within try block: "
                    + startInstructionTryBlock.getOpcode() + "(" + startOfTryBlock + ")");
            LOGGER.info("Last instruction within try block: "
                    + endInstructionTryBlock.getOpcode() + "(" + endOfTryBlock + ")");

            Range tryBlockRange = new Range(startOfTryBlock, endOfTryBlock);
            tryBlocks.add(tryBlockRange);
        }
        return tryBlocks;
    }

    /**
     * Tracks the number of branches contained in a given method.
     *
     * @param methodInformation Encapsulates a given method.
     * @return Returns the number of branches in the given method.
     */
    @SuppressWarnings("unused")
    public static int trackNumberOfBranches(MethodInformation methodInformation) {

        MutableMethodImplementation mutableMethodImplementation =
                new MutableMethodImplementation(methodInformation.getMethodImplementation());

        List<BuilderInstruction> instructions = mutableMethodImplementation.getInstructions();

        Set<BuilderInstruction> branches = new HashSet<>();

        for (BuilderInstruction instruction : instructions) {

            if (instruction instanceof BuilderInstruction21t
                    || instruction instanceof BuilderInstruction22t) {

                branches.add(instructions.get(instruction.getLocation().getIndex() + 1));
                branches.add(instructions.get(((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex()));
            }
        }

        return branches.size();
    }

    /**
     * Determines the new total amount of registers and derives the register IDs of
     * the new registers as well as the free/usable registers.
     *
     * @param methodInformation   Contains the relevant information about a method.
     * @param additionalRegisters The amount of additional registers.
     */
    public static void computeRegisterStates(MethodInformation methodInformation, int additionalRegisters) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();

        int totalRegisters = methodImplementation.getRegisterCount();
        int paramRegisters = MethodUtil.getParameterRegisterCount(methodInformation.getMethod());
        int localRegisters = totalRegisters - paramRegisters;

        // contains the register IDs of the new and free/usable registers
        List<Integer> newRegisters = new ArrayList<>();
        List<Integer> freeRegisters = new ArrayList<>();

        // contains the register IDs of the param registers
        List<Integer> parameterRegisters = new ArrayList<>();

        /*
         * When we increase the number of local registers, the additional
         * registers reside at the end of the local registers, that is:
         *       v0...vN -> v0...vN,vNew1...vNewN
         * The index of the first newly created register resides at
         * the original count of local registers (#localRegisters).
         */
        for (int i = 0; i < additionalRegisters; i++) {
            newRegisters.add(localRegisters + i);
        }
        methodInformation.setNewRegisters(newRegisters);

        /*
         * The idea is to use the last registers for the actual instrumentation by
         * shifting their content into the newly created local registers.
         * This resolves the issue of invoke-range instructions spanning over
         * the newly created local registers.
         * The index of the first usable/free register resides at the original
         * total count of registers (#totalRegisters).
         */
        for (int i = 0; i < additionalRegisters; i++) {
            freeRegisters.add(totalRegisters + i);
        }
        methodInformation.setFreeRegisters(freeRegisters);

        // we need to track the register IDs of the param registers as we shift them later
        for (int p = 0; p < paramRegisters; p++) {
            parameterRegisters.add(localRegisters + p);
        }
        methodInformation.setParamRegisters(parameterRegisters);

        // compute the new count for total/local/param registers
        methodInformation.setTotalRegisterCount(totalRegisters + additionalRegisters);
        methodInformation.setLocalRegisterCount(localRegisters + additionalRegisters);
        // stays unchanged, no additional param register
        methodInformation.setParamRegisterCount(paramRegisters);
    }

    /**
     * Determines the register types of the parameter registers at the method entry.
     *
     * @param methodInformation Stores relevant information about a method.
     * @param dexFile           The un-instrumented dex file.
     */
    public static void analyzeParamRegisterTypes(MethodInformation methodInformation, DexFile dexFile) {

        MethodAnalyzer analyzer = new MethodAnalyzer(new ClassPath(Lists.newArrayList(new DexClassProvider(dexFile)),
                true, ClassPath.NOT_ART), methodInformation.getMethod(),
                null, false);

        Map<Integer, RegisterType> registerTypes = new HashMap<>();

        // we want the register type at the method head, that is before the first instruction
        AnalyzedInstruction instruction = analyzer.getAnalyzedInstructions().get(0);

        for (int registerID : methodInformation.getParamRegisters()) {
            registerTypes.put(registerID, instruction.getPreInstructionRegisterType(registerID));
        }

        methodInformation.setParamRegisterTypeMap(Optional.of(registerTypes));
    }
}
