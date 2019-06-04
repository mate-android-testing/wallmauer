package de.uni_passau.fim.branchcoverage;

import org.jf.dexlib2.analysis.AnalyzedInstruction;
import org.jf.dexlib2.analysis.MethodAnalyzer;
import org.jf.dexlib2.analysis.RegisterType;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.BuilderOffsetInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22t;
import org.jf.dexlib2.iface.Method;

import java.util.*;

public final class Analyzer {


    /**
     * We like to derive the register types at each branching instruction. For the family of if-instructions
     * we need to collect the type after the instruction, while for else-branches (which are labels and
     * can be only accessed by the instruction after the label) we need to collect the type before the
     * instruction.
     *
     * @param analyzer A method analyzer instance.
     * @param method The method we want to analyze.
     * @param branches A map for collecting the branch id and a reference to the actual branch.
     * @param registerTypeMap A map collecting the register types for all registers at each branch.
     */
    public static void analyzeRegisterTypes(MethodAnalyzer analyzer, Method method, Map<Integer,Branch> branches,
                                            Map<Integer, Map<Integer,RegisterType>> registerTypeMap) {

        assert analyzer != null;

        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(method.getImplementation());

        List<BuilderInstruction> instructions = mutableMethodImplementation.getInstructions();
        List<AnalyzedInstruction> analyzedInstructions = analyzer.getAnalyzedInstructions();

        Set<BuilderInstruction> coveredInstructions = new HashSet<>();

        int branchID = 0;
        int totalRegisters = mutableMethodImplementation.getRegisterCount();

        for (int i = 0; i < instructions.size(); i++) {

            BuilderInstruction instruction = instructions.get(i);

            if (instruction instanceof BuilderInstruction21t
                    || instruction instanceof BuilderInstruction22t && !coveredInstructions.contains(instruction)) {

                // avoid iterating over same instruction multiple times
                coveredInstructions.add(instruction);

                AnalyzedInstruction analyzedInstruction = analyzedInstructions.get(i);

                assert analyzedInstruction.getInstruction().getOpcode().equals(instruction.getOpcode())
                        && analyzedInstruction.getInstructionIndex() == instruction.getLocation().getIndex();

                Branch ifBranch = new IfBranch(instruction.getLocation().getIndex(), instruction.getLocation().getCodeAddress());
                branches.put(branchID, ifBranch);

                // we want to track all register types after the if instruction
                Map<Integer, RegisterType> registerTypesAtIfBranch = new HashMap<>();
                for (int v=0; v < totalRegisters; v++) {
                    registerTypesAtIfBranch.put(v, analyzedInstruction.getPostInstructionRegisterType(v));
                }
                registerTypeMap.put(branchID, registerTypesAtIfBranch);

                branchID++;

                // additionally we need to consider the first instruction at the else branch (label)
                analyzedInstruction = analyzedInstructions.get(((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex());

                Branch elseBranch = new ElseBranch(((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex(),
                        ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getCodeAddress(),
                        ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getLabels());

                branches.put(branchID, elseBranch);

                // we want to track all register types at the else branch (before the first instruction there)
                Map<Integer, RegisterType> registerTypesAtElseBranch = new HashMap<>();
                for (int v=0; v < totalRegisters; v++) {
                    registerTypesAtElseBranch.put(v, analyzedInstruction.getPreInstructionRegisterType(v));
                }
                registerTypeMap.put(branchID, registerTypesAtElseBranch);
                branchID++;
            }
        }
    }

    public static List<RegisterType> analyzeShiftedRegisterTypes(MethodAnalyzer analyzer, List<Integer> registerIDs) {

        List<RegisterType> registerTypes = new ArrayList<>();

        // we want the register type at the method head, that is before the first instruction
        AnalyzedInstruction instruction = analyzer.getAnalyzedInstructions().get(0);

        for (Integer registerID : registerIDs) {
            registerTypes.add(instruction.getPreInstructionRegisterType(registerID));
        }

        return registerTypes;
    }
}
