package de.uni_passau.fim.auermich.branchcoverage.analysis;


import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.branchcoverage.dto.MethodInformation;
import de.uni_passau.fim.auermich.branchcoverage.instrumentation.InstrumentationPoint;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Analyzer {

    private static final Logger LOGGER = Logger.getLogger(Analyzer.class
            .getName());

    /**
     * Tracks the instrumentation points, i.e. instructions starting a branch or being an if stmt.
     *
     * @param methodInformation Encapsulates a method.
     * @return Returns the set of instrumentation points.
     */
    public static Set<InstrumentationPoint> trackInstrumentationPoints(MethodInformation methodInformation) {

        Set<InstrumentationPoint> instrumentationPoints = new TreeSet<>();

        MutableMethodImplementation mutableMethodImplementation =
                new MutableMethodImplementation(methodInformation.getMethodImplementation());

        List<BuilderInstruction> instructions = mutableMethodImplementation.getInstructions();

        for (BuilderInstruction instruction : instructions) {

            // check whether instruction is an if instruction
            if (instruction instanceof BuilderInstruction21t
                    || instruction instanceof BuilderInstruction22t) {

                // The if branch starts at the next instruction, which we also need to trace.
                InstrumentationPoint ifBranch = new InstrumentationPoint(instructions.get(instruction.getLocation().getIndex() + 1), InstrumentationPoint.Type.IF_BRANCH);
                instrumentationPoints.add(ifBranch);

                // We also need to instrument the else branch.
                int elseBranchPosition = ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex();
                InstrumentationPoint elseBranch = new InstrumentationPoint(instructions.get(elseBranchPosition), InstrumentationPoint.Type.ELSE_BRANCH);
                instrumentationPoints.add(elseBranch);
            }
        }

        LOGGER.info(instrumentationPoints.toString());
        return instrumentationPoints;
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

        for(BuilderInstruction instruction : instructions) {

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
