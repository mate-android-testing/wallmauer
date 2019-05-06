package de.uni_passau.fim.branchcoverage;

import com.google.common.collect.Lists;
import de.uni_passau.fim.RegisterInformation;
import de.uni_passau.fim.utility.Utility;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.analysis.MethodAnalyzer;
import org.jf.dexlib2.analysis.RegisterType;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.BuilderOffsetInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

public final class Instrumenter {

    public static final int MAX_USABLE_REGS = 16;

    /**
     * Checks if {@code implementation} contains a branching instruction.
     *
     * @param implementation The implementation to check.
     * @return Return {@code true} if implementation contains branching instruction.
     */
    public static boolean methodNeedsModification(MethodImplementation implementation) {

        List<Instruction> instructions = Lists.newArrayList(implementation.getInstructions());

        /*
         * Search for branch instructions. Those are identified by their opcode,
         * which ranges from 32-3D, where 32-37 refers to register comparison
         * and 38-3D compares against 0, e.g. IF_EQZ or IF_NEZ. Alternatively,
         * all branching instructions start with the prefix 'IF_'.
         */
        for (Instruction instruction : instructions) {
            if (instruction.getOpcode().name().startsWith("IF_"))
                return true;
        }

        return false;
    }

    /**
     * Inserts a simple onDestroy method in the 'MainActivity' if it is not
     * present yet. The only purpose of this method is calling the 'write' method
     * of our tracer functionality, which in turn writes the collected traces
     * into the app-internal storage. The onDestroy method should be always called
     * upon the (normal) termination of the app.
     *
     * @return Returns the implementation of the onDestroy method.
     */
    public static MethodImplementation insertOnDestroy(String packageName) {

        System.out.println("Inserting onDestroy method into 'MainActivity'");

        MutableMethodImplementation implementation = new MutableMethodImplementation(2);

        // TODO: verify that addInstruction inserts the instruction at the end!
        // const-string instruction has format '21c' and opcode '1a', registerA defines local register starting from v0,v1,...
        implementation.addInstruction(new BuilderInstruction21c(Opcode.CONST_STRING, 0,
                new ImmutableStringReference(packageName)));

        //     invoke-static {v0}, Lde/uni_passau/fim/auermich/tracer/Tracer;->write(Ljava/lang/String;)V

        // invoke-static instruction has format '35c' and opcode '71'
        implementation.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1
                , 0, 0, 0, 0, 0,
                new ImmutableMethodReference("Lde/uni_passau/fim/auermich/tracer/Tracer;", "write",
                        Lists.newArrayList("Ljava/lang/String;"), "V")));

        // we have to add return-statement as well, though void!
        implementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));

        return implementation;
    }

    /**
     * Performs the instrumentation, i.e. inserts instructions at each branch.
     *
     * @param implementation The implementation to instrument.
     * @param identifier     The unique identifier for the method, i.e. packageName->className->methodName
     * @param totalRegisters The total amount of registers, i.e. the .register directive at the method head
     * @return Return the instrumented {@code MethodImplementation}.
     * @throws NoSuchFieldException   Should never happen, a byproduct of using reflection.
     * @throws IllegalAccessException Should never happen, a byproduct of using reflection.
     */
    public static MethodImplementation modifyMethod(MethodImplementation implementation, String identifier,
                                                    int totalRegisters, Map<Integer,Branch> branches, Map<Integer,
                                                    Map<Integer,RegisterType>> registerTypeMap, RegisterInformation registerInformation) {

        int branchIndex = 0;
        int branchCounter = 0;

        // contains the covered instructions, those we inspected for modification of the register order (shift of register number)
        Set<BuilderInstruction> coveredInstructions = new HashSet<>();

        // contains the covered else branches
        Set<Branch> coveredElseBranches = new HashSet<>();
        Set<BuilderInstruction> coveredIfBranches = new HashSet<>();

        MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(implementation);
        List<BuilderInstruction> instructions = mutableImplementation.getInstructions();

        // increase the register count of the method, i.e. the .register directive at each method's head
        Utility.increaseMethodRegisterCount(mutableImplementation, totalRegisters);

        // check whether increasing worked
        if (mutableImplementation.getRegisterCount() != totalRegisters) {
            System.out.println("Couldn't increase register count, abort instrumentation!");
            return mutableImplementation;
        }

        for (int i = 0; i < instructions.size(); i++) {

            BuilderInstruction instruction = instructions.get(i);

            if (!coveredInstructions.contains(instruction)) {
                coveredInstructions.add(instruction);
                // re-order param registers (shift of register number)
                Utility.reOrderRegister(instruction, localRegisterID);
            }

            /**
             * Branching instructions are either identified by their opcode,
             * the prefix they share (i.e. IF_) or the format, which
             * is either '21t' (e.g. IF_EQZ) or '22t' (e.g. IF_EQ).
             */
            if ((instruction instanceof BuilderInstruction21t
                    || instruction instanceof BuilderInstruction22t)
                    && !coveredIfBranches.contains(instruction)) {

                // avoid iterating over the same instruction several times
                coveredIfBranches.add(instruction);

                String id = identifier;
                id += "->" + branchIndex;

                Branch ifBranch = branches.get(branchIndex);
                int ifBranchIndex = instruction.getLocation().getIndex();

                Map<Integer, RegisterType> registerTypes = registerTypeMap.getOrDefault(branchIndex, null);

                insertInstrumentationCode(mutableImplementation, registerTypes, ifBranchIndex, id, "trace", coveredInstructions);

                branchCounter++;
                branchIndex++;

                Branch elseBranch = branches.get(branchIndex);

                if (!coveredElseBranches.contains(elseBranch)) {

                    coveredElseBranches.add(elseBranch);

                    id = identifier;
                    id += "->" + branchIndex;

                    int elseBranchIndex = ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex();
                    registerTypes = registerTypeMap.getOrDefault(branchIndex, null);
                    insertInstrumentationCode(mutableImplementation, registerTypes, elseBranchIndex, id, "trace", coveredInstructions);

                    if (localRegisterID <= MAX_LOCAL_USABLE_REG) {
                        mutableImplementation.swapInstructions(elseBranchIndex, elseBranchIndex + 1);
                        mutableImplementation.swapInstructions(elseBranchIndex + 1, elseBranchIndex + 2);
                    } else {
                        if (registerTypes != null) {
                            RegisterType selectedRegisterType = findSuitableRegister(registerTypes).getValue();
                            if (mapRegisterType(selectedRegisterType) == DUMMY_INSTRUCTION_TYPE) {
                                mutableImplementation.swapInstructions(elseBranchIndex, elseBranchIndex + 1);
                                mutableImplementation.swapInstructions(elseBranchIndex + 1, elseBranchIndex + 2);
                                mutableImplementation.swapInstructions(elseBranchIndex + 2, elseBranchIndex + 3);
                                mutableImplementation.swapInstructions(elseBranchIndex + 3, elseBranchIndex + 4);
                                mutableImplementation.swapInstructions(elseBranchIndex + 4, elseBranchIndex + 5);
                            } else {
                                mutableImplementation.swapInstructions(elseBranchIndex, elseBranchIndex + 1);
                                mutableImplementation.swapInstructions(elseBranchIndex + 1, elseBranchIndex + 2);
                                mutableImplementation.swapInstructions(elseBranchIndex + 2, elseBranchIndex + 3);
                                mutableImplementation.swapInstructions(elseBranchIndex + 3, elseBranchIndex + 4);
                            }
                        }
                    }
                    branchCounter++;
                }
                // we have to update the branchIndex in any way
                branchIndex++;
            }
        }

        // whether we already modified onDestroy or not (if branches of it), we need to further modify it to call Tracer.write()
        if (isMainActivity && isOnDestroy && !modifiedOnDestroy) {
            modifyOnDestroy(mutableImplementation, coveredInstructions);
        }
        return mutableImplementation;
    }



}
