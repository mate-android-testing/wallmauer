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
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

                // if branch location (uses the instruction/instruction id following the if-instruction)
                Branch ifBranch = new IfBranch(instructions.get(i + 1), id);
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
     * Tracks the instruction IDs of entry/beginning instructions. A method can have multiple such
     * instructions due to try catch blocks at the beginning of a method.
     *
     * @param methodInformation The method information.
     * @param dexFile           The dexFile containing the method implementation.
     * @return Returns a list of instruction IDs referring to the entry/beginning instructions.
     */
    public static List<Integer> analyzeEntryInstructions(MethodInformation methodInformation, DexFile dexFile) {

        MethodAnalyzer analyzer = new MethodAnalyzer(new ClassPath(Lists.newArrayList(new DexClassProvider(dexFile)),
                true, ClassPath.NOT_ART), methodInformation.getMethod(),
                null, false);

        List<Integer> entryInstructionIDs = new ArrayList<>();

        for (AnalyzedInstruction analyzedInstruction : analyzer.getAnalyzedInstructions()) {
            if (analyzedInstruction.isBeginningInstruction()) {
                entryInstructionIDs.add(analyzedInstruction.getInstructionIndex());
            }
        }

        return entryInstructionIDs;
    }

    /**
     * Tracks the first instruction, more precisely its instruction id, of each try and catch block.
     *
     * @param methodInformation The method information.
     * @return Returns a sorted list of instruction ids describing the beginning of each try and catch block.
     */
    public static List<Integer> analyzeTryCatchBlocks(MethodInformation methodInformation) {

        Set<Integer> tryCatchBlockIDs = new HashSet<>();

        MethodImplementation implementation = methodInformation.getMethodImplementation();
        int consumedCodeUnits = 0;

        for (TryBlock<? extends ExceptionHandler> tryBlock : implementation.getTryBlocks()) {

            List<Instruction> instructions = Lists.newArrayList(implementation.getInstructions());

            for (int index = 0; index < instructions.size(); index++) {

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
                    // reached the beginning of the try block
                    tryCatchBlockIDs.add(index);
                }
                consumedCodeUnits += instructions.get(index).getCodeUnits();
            }

            // iterate over attached catch blocks
            tryBlock.getExceptionHandlers().forEach(h -> {

                /*
                 * The (absolute) position of the catch block expressed in terms of code units. The catch
                 * block starts after n-th code units. So, we need to map an instruction to its
                 * size (code units) and count them.
                 */
                AtomicInteger ctrCodeUnits = new AtomicInteger(0);

                for (int index = 0; index < instructions.size(); index++) {
                    if (ctrCodeUnits.get() == h.getHandlerCodeAddress()) {
                        // reached the beginning of the catch block
                        tryCatchBlockIDs.add(index);
                        break;
                    }
                    ctrCodeUnits.set(ctrCodeUnits.get() + instructions.get(index).getCodeUnits());
                }
            });
        }

        // ensure ascending order
        return tryCatchBlockIDs.stream().sorted().collect(Collectors.toList());
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
