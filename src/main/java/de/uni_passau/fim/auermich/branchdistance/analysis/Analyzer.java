package de.uni_passau.fim.auermich.branchdistance.analysis;


import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.branchdistance.branch.Branch;
import de.uni_passau.fim.auermich.branchdistance.branch.ElseBranch;
import de.uni_passau.fim.auermich.branchdistance.branch.IfBranch;
import de.uni_passau.fim.auermich.branchdistance.dto.MethodInformation;
import org.jf.dexlib2.analysis.*;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.BuilderOffsetInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22t;
import org.jf.dexlib2.iface.DexFile;

import java.util.*;

public final class Analyzer {


    /**
     * Traverses and collects the branches for the given method. This yields
     * the number of branches per method, whereas duplicates are removed inherently.
     *
     * @param methodInformation Stores all relevant information about a method.
     * @return Returns a set of branches (duplicates eliminated) for the given method.
     */
    public static Set<Branch> trackBranches(MethodInformation methodInformation) {

        MutableMethodImplementation mutableMethodImplementation =
                new MutableMethodImplementation(methodInformation.getMethodImplementation());

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
                Branch ifBranch = new IfBranch(instruction,id);
                branches.add(ifBranch);
                branchID++;

                // else branch location
                Branch elseBranch = new ElseBranch(instructions.get(
                        ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex()), id);
                branches.add(elseBranch);
                branchID++;
            }
        }
        methodInformation.setBranches(branches);
        return branches;
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

        methodInformation.setParamRegisterTypeMap(Optional.of(registerTypes));
    }
}
