package de.uni_passau.fim.auermich.instrumentation.branchcoverage.analysis;


import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.instrumentation.branchcoverage.core.InstrumentationPoint;
import de.uni_passau.fim.auermich.instrumentation.branchcoverage.dto.MethodInformation;
import de.uni_passau.fim.auermich.instrumentation.branchcoverage.utility.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.jf.dexlib2.util.MethodUtil;

import java.util.*;

public final class Analyzer {

    private static final Logger LOGGER = LogManager.getLogger(Analyzer.class);

    /**
     * Tracks the instrumentation points, i.e. instructions defining a branch.
     *
     * @param methodInformation Encapsulates a method.
     * @return Returns the set of instrumentation points.
     */
    public static Set<InstrumentationPoint> trackInstrumentationPoints(MethodInformation methodInformation) {

        // a tree set ensures the appropriate order (according to the instruction index of the instrumentation point)
        Set<InstrumentationPoint> instrumentationPoints = new TreeSet<>();

        MutableMethodImplementation mutableMethodImplementation =
                new MutableMethodImplementation(methodInformation.getMethodImplementation());

        List<BuilderInstruction> instructions = mutableMethodImplementation.getInstructions();

        for (BuilderInstruction instruction : instructions) {

            // check whether instruction is an if instruction
            if (instruction instanceof BuilderInstruction21t
                    || instruction instanceof BuilderInstruction22t) {

                // the successor of the if instruction defines the if branch
                InstrumentationPoint ifBranch = new InstrumentationPoint(instructions.get(instruction.getLocation().getIndex() + 1),
                        InstrumentationPoint.Type.IF_BRANCH);
                instrumentationPoints.add(ifBranch);

                // the target of the if instruction defines the else branch
                int elseBranchPosition = ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex();
                InstrumentationPoint elseBranch = new InstrumentationPoint(instructions.get(elseBranchPosition),
                        InstrumentationPoint.Type.ELSE_BRANCH);
                instrumentationPoints.add(elseBranch);
            } else if (instruction instanceof BuilderInstruction31t
                    && instruction.getOpcode() != Opcode.FILL_ARRAY_DATA) { // sparse-/packed-switch instruction

                /*
                 * The packed-switch instruction defines a switch-case construct. To access the individual cases of
                 * the switch statement, one needs to follow the packed-switch-payload instruction that contains this
                 * information. However, the default case or fall-through case is not listed in this payload instruction
                 * and needs to be addressed explicitly. This is simply the next instruction following the switch instruction.
                 */
                int switchPayloadPosition = ((BuilderInstruction31t) instruction).getTarget().getLocation().getIndex();

                BuilderSwitchPayload switchPayloadInstruction
                        = (BuilderSwitchPayload) instructions.get(switchPayloadPosition);

                for (BuilderSwitchElement switchElement : switchPayloadInstruction.getSwitchElements()) {
                    int switchCasePosition = switchElement.getTarget().getLocation().getIndex();
                    InstrumentationPoint switchCase
                            = new InstrumentationPoint(instructions.get(switchCasePosition),
                            InstrumentationPoint.Type.ELSE_BRANCH);
                    instrumentationPoints.add(switchCase);
                }

                // the direct successor of the switch instruction represents the fall-through case (default case)
                int defaultCasePosition = instruction.getLocation().getIndex() + 1;
                InstrumentationPoint defaultCase = new InstrumentationPoint(instructions.get(defaultCasePosition),
                        InstrumentationPoint.Type.ELSE_BRANCH);
                instrumentationPoints.add(defaultCase);
            }
        }

        LOGGER.debug(instrumentationPoints.toString());
        return instrumentationPoints;
    }

    /**
     * Returns a sorted set of try blocks. Each try block is identified by its start and end index.
     *
     * @param methodInformation Encapsulates a method.
     * @return Returns ranges describing the start and end of try blocks.
     */
    public static Set<Range> getTryBlocks(MethodInformation methodInformation) {

        LOGGER.debug("Retrieving try blocks of method...");

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();

        MutableMethodImplementation mutableMethodImplementation =
                new MutableMethodImplementation(methodInformation.getMethodImplementation());

        Set<Range> tryBlocks = new TreeSet<>();

        LOGGER.debug("Number of try blocks: " + methodImplementation.getTryBlocks().size());

        // TODO: this can be done in one pass over the instructions
        for (TryBlock<? extends ExceptionHandler> tryBlock : methodImplementation.getTryBlocks()) {

            LOGGER.debug("Try block size: " + tryBlock.getCodeUnitCount() + " code units");
            LOGGER.debug("Try block start address: " + tryBlock.getStartCodeAddress());
            LOGGER.debug("Associated catch blocks: " + tryBlock.getExceptionHandlers().size());

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

            LOGGER.debug("First instruction within try block: "
                    + startInstructionTryBlock.getOpcode() + "(" + startOfTryBlock + ")");
            LOGGER.debug("Last instruction within try block: "
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
     * @param methodInformation Contains the relevant information about a method.
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
     * @param dexFile The un-instrumented dex file.
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
