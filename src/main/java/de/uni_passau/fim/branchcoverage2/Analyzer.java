package de.uni_passau.fim.branchcoverage2;


import com.google.common.collect.Lists;
import org.jf.dexlib2.analysis.*;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.BuilderOffsetInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22t;
import org.jf.dexlib2.iface.DexFile;
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
     */
    public static void trackBranchLocations(MethodInformation methodInformation) {

        Method method = methodInformation.getMethod();

        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(method.getImplementation());

        List<BuilderInstruction> instructions = mutableMethodImplementation.getInstructions();

        Set<BuilderInstruction> coveredInstructions = new HashSet<>();
        Set<Branch> branches = new HashSet<>();

        int branchID = 0;

        for (int i = 0; i < instructions.size(); i++) {

            BuilderInstruction instruction = instructions.get(i);

            if (instruction instanceof BuilderInstruction21t
                    || instruction instanceof BuilderInstruction22t && !coveredInstructions.contains(instruction)) {

                // avoid iterating over same instruction multiple times
                coveredInstructions.add(instruction);

                String id = methodInformation.getMethodID() + "->" + branchID;

                // if branch location
                Branch ifBranch = new IfBranch(instruction.getLocation().getIndex(), instruction.getLocation().getCodeAddress(),id);
                branches.add(ifBranch);
                branchID++;

                // else branch location
                Branch elseBranch = new ElseBranch(((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex(),
                        ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getCodeAddress(),
                        ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getLabels(), id);
                branches.add(elseBranch);
                branchID++;
            }
        }
        methodInformation.setBranches(branches);
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

        Map<Integer,RegisterType> registerTypes = new HashMap<>();

        // we want the register type at the method head, that is before the first instruction
        AnalyzedInstruction instruction = analyzer.getAnalyzedInstructions().get(0);

        for (int registerID : methodInformation.getParamRegisters()) {
            registerTypes.put(registerID,instruction.getPreInstructionRegisterType(registerID));
        }
    }
}
