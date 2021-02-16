package de.uni_passau.fim.auermich.basicBlockCoverage.analysis;


import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.basicBlockCoverage.dto.MethodInformation;
import de.uni_passau.fim.auermich.basicBlockCoverage.instrumentation.InstrumentationPoint;
import de.uni_passau.fim.auermich.basicBlockCoverage.utility.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.analysis.*;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.BuilderOffsetInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22t;
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
     * Tracks the instrumentation points, i.e. instructions starting a branch or being an if stmt.
     *
     * @param methodInformation Encapsulates a method.
     * @return Returns the set of instrumentation points.
     */
    public static Set<InstrumentationPoint> trackInstrumentationPointsForBlocks(final MethodInformation methodInformation) {
        final Map<Integer, InstrumentationPoint.Type> instrumentationPoints = new HashMap<>();
        final List<AnalyzedInstruction> instructions = methodInformation.getInstructions();

        // each entry refers to the code address of the first instruction within a catch block
        final Set<Integer> catchBlocks = methodInformation.getMethodImplementation().getTryBlocks().stream().flatMap(t -> t.getExceptionHandlers().stream()).map(ExceptionHandler::getHandlerCodeAddress).collect(Collectors.toSet());
        LOGGER.debug("Catch Blocks located at code addresses: " + catchBlocks);

        int consumedCodeUnits = 0;
        for (final AnalyzedInstruction instruction : instructions) {

            // Instrument if-else branch
            if (isBranchingInstruction(instruction)) {
                LOGGER.debug("Found if branch: " + instruction.getInstructionIndex());
                final int ifTarget = instruction.getInstructionIndex() + 1;
                LOGGER.debug("If target: " + ifTarget);
                instrumentationPoints.put(ifTarget, InstrumentationPoint.Type.IF_BRANCH);

                final int elseTarget = instruction.getSuccessors().stream().mapToInt(AnalyzedInstruction::getInstructionIndex).max().getAsInt();
                assert instruction.getSuccessors().stream().map(AnalyzedInstruction::getInstructionIndex).collect(Collectors.toList()).equals(List.of(ifTarget, elseTarget));
                LOGGER.debug("Else target: " + elseTarget);
                instrumentationPoints.put(elseTarget, InstrumentationPoint.Type.ELSE_BRANCH);
            }

            // Instrument method entry instructions
            if(instruction.isBeginningInstruction()) {
                LOGGER.debug("Entry instruction: " + instruction.getInstructionIndex());
                 instrumentationPoints.putIfAbsent(instruction.getInstructionIndex(), InstrumentationPoint.Type.ENTRY_STMT);
            }

            // Instrument target of goto instruction
            if (isGotoInstruction(instruction)) {
                LOGGER.debug("Found goto instruction: " + instruction.getInstructionIndex());
                final List<AnalyzedInstruction> successors = instruction.getSuccessors();
                assert successors.size() == 1;
                instrumentationPoints.putIfAbsent(successors.get(0).getInstructionIndex(), InstrumentationPoint.Type.GOTO_BRANCH);
            }


            // Instrument the first instruction of each catch-block
            if (!catchBlocks.isEmpty()) {
                if (catchBlocks.contains(consumedCodeUnits)) {
                    // first instruction of a catch block is a leader instruction
                    LOGGER.debug("First instruction within catch block at pos: " + instruction.getInstructionIndex());
                    instrumentationPoints.putIfAbsent(instruction.getInstructionIndex(), InstrumentationPoint.Type.CATCH_BLOCK_START);
                }
                consumedCodeUnits += instruction.getInstruction().getCodeUnits();
            }

            // Instrument any other instruction that has more then one successor
            // For example, every instruction inside a try block which can throw an exception has its corresponding catch block as an successor.
            final Set<Integer> successors = instruction.getSuccessors().stream().map(AnalyzedInstruction::getInstructionIndex).collect(Collectors.toSet());
            successors.removeAll(instrumentationPoints.keySet());
            successors.remove(instruction.getInstructionIndex() + 1);
            if (!successors.isEmpty()) {
                LOGGER.debug("Exceptional flow");
                LOGGER.debug("From: " + instruction.getInstructionIndex());
                for(final int successor : successors) {
                        LOGGER.debug("    To: " + successor);
                        instrumentationPoints.putIfAbsent(successor, InstrumentationPoint.Type.EXCEPTIONAL_SUCCESSOR);
                }
            }
        }

        final List<BuilderInstruction> bInstructions = new MutableMethodImplementation(methodInformation.getMethodImplementation()).getInstructions();
        final List<Integer> covered_instructions = new ArrayList<>(instrumentationPoints.keySet());
        Collections.sort(covered_instructions);
        covered_instructions.add(instructions.get(instructions.size() - 1).getInstructionIndex() + 1);

        final Set<InstrumentationPoint> result = new HashSet<>();
        for(int i = 0; i < covered_instructions.size() - 1; ++i){
            final int index = covered_instructions.get(i);
            final BuilderInstruction builderInstruction = bInstructions.get(index);
            final InstrumentationPoint.Type type = instrumentationPoints.get(index);
            final int block_size = covered_instructions.get(i + 1) - index;
            final InstrumentationPoint p = new InstrumentationPoint(builderInstruction,  type, block_size);
            result.add(p);
        }

        LOGGER.info(result.toString());
        return result;
    }

    /**
     * Checks whether the given instruction refers to a goto instruction.
     *
     * @param analyzedInstruction The instruction to be analyzed.
     * @return Returns {@code true} if the instruction is a goto instruction,
     * otherwise {@code false} is returned.
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
     * @return Returns {@code true} if the instruction is a branching instruction,
     * otherwise {@code false} is returned.
     */
    public static boolean isBranchingInstruction(final AnalyzedInstruction analyzedInstruction) {
        final Instruction instruction = analyzedInstruction.getInstruction();
        final EnumSet<Format> branchingInstructions = EnumSet.of(Format.Format21t, Format.Format22t);
        return branchingInstructions.contains(instruction.getOpcode().format);
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
