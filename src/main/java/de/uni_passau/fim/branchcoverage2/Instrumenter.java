package de.uni_passau.fim.branchcoverage2;

import com.google.common.collect.Lists;
import de.uni_passau.fim.branchcoverage.Branch;
import de.uni_passau.fim.branchcoverage.RegisterInformation;
import de.uni_passau.fim.utility.Utility;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.analysis.RegisterType;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.BuilderOffsetInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.util.MethodUtil;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Instrumenter {

    public static final int MAX_USABLE_REGS = 16;

    // the logger instance
    private static final Logger LOGGER = Logger.getLogger(Instrumenter.class
            .getName());

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
     * Determines the new total amount of registers and derives the register IDs of
     * the new registers as well as the free/usable registers.
     *
     * @param methodInformation Contains the relevant information about a method.
     * @param additionalRegisters The amount of additional registers.
     */
    public static void computeRegisterStates(MethodInformation methodInformation, int additionalRegisters) {

        assert methodInformation.getImplementation().isPresent();

        MethodImplementation methodImplementation = methodInformation.getImplementation().get();

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
        for (int i=0; i < additionalRegisters; i++) {
            newRegisters.add(localRegisters+i);
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
        for (int i=0; i < additionalRegisters; i++) {
            freeRegisters.add(totalRegisters+i);
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
        // stays changed, no additional param register
        methodInformation.setParamRegisterCount(paramRegisters);
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
    public static MethodImplementation insertOnDestroy(String packageName, String superClass) {

        System.out.println("Inserting onDestroy method into 'MainActivity'");
        System.out.println("Super class: " + superClass);

        MutableMethodImplementation implementation = new MutableMethodImplementation(2);

        // TODO: check which super class (activity class) is used for the mainActivity, this is not necessarily
        // TODO: the super class of the mainActivity, newer apps should use: "Landroid/support/v7/app/AppCompatActivity;"

        // call super.onDestroy() first, AppCompatActivity seems to be the current API standard for activity classes
        implementation.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_SUPER, 1,
                1, 0, 0, 0, 0,
                new ImmutableMethodReference(superClass, "onDestroy",
                        Lists.newArrayList(), "V")));

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

    public static void insertOnDestroyForSuperClasses(List<ClassDef> classes, List<Method> methods, String superClass) {

        // we need to determine the activity super class
        while (superClass != null && !superClass.equals("Landroid/app/Activity;")
                && !superClass.equals("Landroid/support/v7/app/AppCompatActivity;")
                && !superClass.equals("Landroid/support/v7/app/ActionBarActivity;")
                && !superClass.equals("Landroid.support.v4.app.FragmentActivity;")) {
            // iterate over classDef and follow link of superClass until we reach a suitable one
            for (ClassDef classesDef : classes) {
                if (classesDef.toString().equals(superClass)) {
                    superClass = classesDef.getSuperclass();
                    break;
                }
            }
        }
    }

    /**
     * If the 'MainActivity' already contains an onDestroy method, we have to integrate our tracer functionality
     * into it instead of creating an own onDestroy method. In general, we need to place before each 'return' statement
     * instructions that call our tracer.
     *
     * @param methodInformation Stores all relevant information about a method.
     * @param packageName The package name declared in the AndroidManifest.xml file.
     */
    public static MethodImplementation modifyOnDestroy(MethodInformation methodInformation, String packageName) {

        assert methodInformation.getImplementation().isPresent();

        LOGGER.info("Modifying onDestroy method of 'MainActivity'");

        MethodImplementation methodImplementation = methodInformation.getImplementation().get();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);
        List<BuilderInstruction> instructions = mutableMethodImplementation.getInstructions();

        // we need to insert the code before each return statement
        for (int i=0; i < instructions.size(); i++) {

            // onDestroy has return type void, which opcodes refers to '0E'
            if (instructions.get(i).getOpcode().name.equals("return-void")) {

                LOGGER.info("Inserting Tracer.write(packageName) invocation before return statement!");

                // we require one parameter containing the unique branch id
                int freeRegisterID = methodInformation.getFreeRegisters().get(0);

                // const-string pN, "packageName" (pN refers to the free register at the end)
                BuilderInstruction21c constString = new BuilderInstruction21c(Opcode.CONST_STRING, freeRegisterID,
                        new ImmutableStringReference(packageName));

                // invoke-static-range
                BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                        freeRegisterID, 1,
                        new ImmutableMethodReference("Lde/uni_passau/fim/auermich/tracer/Tracer;", "write",
                                Lists.newArrayList("Ljava/lang/String;"), "V"));

                mutableMethodImplementation.addInstruction(i, constString);
                mutableMethodImplementation.addInstruction(++i,invokeStaticRange);
            }
        }
        return mutableMethodImplementation;
    }

    private static Map.Entry<Integer, RegisterType> findSuitableRegister(Map<Integer, RegisterType> registerTypes) {

        Map.Entry<Integer, RegisterType> selectedRegister = registerTypes.entrySet().stream().findFirst().get();
        // Map.Entry<Integer, RegisterType> selectedRegister= new AbstractMap.SimpleEntry<Integer, RegisterType>(-1, RegisterType.LONG_HI_TYPE);

        // TODO: we can't select 'newLocalRegisters' as they contain the original content of v14,v15 respectively v16,v17
        /*
        // find a register type that is not long or double (would require 2 registers)
        for (Map.Entry<Integer, RegisterType> entry : registerTypes.entrySet()) {
            if (entry.getValue().category == RegisterType.LONG_HI || entry.getValue().category== RegisterType.LONG_LO
                    || entry.getValue().category == RegisterType.DOUBLE_HI
                    || entry.getValue().category == RegisterType.DOUBLE_LO) {
                continue;
            } else {
                // we found a suitable type
                // TODO: verify that we don't get undesired type
                selectedRegister = entry;
                break;
            }
        }
        */
        System.out.println("Selected Register: v" + selectedRegister.getKey() + ", " + selectedRegister.getValue());
        return selectedRegister;
    }

    /**
     * Maps a given {@param registerType} to a value defined by the following encoding:
     * Reference         ->  0
     * Primitive         ->  1
     * Primitive-Wide    ->  2
     * Conflicted/UNINIT ->  3
     *
     * @param registerType The register type we like to map to its value.
     * @return Returns the value denoting the register type.
     */
    private static int mapRegisterType(final RegisterType registerType) {

        if (registerType.category == RegisterType.REFERENCE || registerType.category == RegisterType.NULL) {
            return 0;
        } else if (registerType.category == RegisterType.BOOLEAN || registerType.category == RegisterType.CHAR
                || registerType.category == RegisterType.INTEGER || registerType.category == RegisterType.BYTE
                || registerType.category == RegisterType.SHORT || registerType.category == RegisterType.FLOAT
                || registerType.category == RegisterType.ONE || registerType.category == RegisterType.POS_BYTE
                || registerType.category == RegisterType.POS_SHORT) {
            return 1;
        } else if (registerType.category == RegisterType.LONG_HI || registerType.category == RegisterType.LONG_LO
                || registerType.category == RegisterType.DOUBLE_HI || registerType.category == RegisterType.DOUBLE_LO) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Instruments the given branch with the tracer functionality.
     *
     * @param methodInformation Stores all relevant information about the given method.
     * @param index The position where we insert our instrumented code.
     * @param id    The id which identifies the given branch, i.e. packageName->className->method->branchID.
     */
    private static void insertInstrumentationCode(MethodInformation methodInformation, int index, final String id) {

        assert methodInformation.getImplementation().isPresent();

        MethodImplementation methodImplementation = methodInformation.getImplementation().get();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // we require one parameter containing the unique branch id
        int freeRegisterID = methodInformation.getFreeRegisters().get(0);

        // const-string pN, "unique-branch-id" (pN refers to the free register at the end)
        BuilderInstruction21c constString = new BuilderInstruction21c(Opcode.CONST_STRING, freeRegisterID,
                new ImmutableStringReference(id));

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                freeRegisterID, 1,
                new ImmutableMethodReference("Lde/uni_passau/fim/auermich/tracer/Tracer;", "trace",
                        Lists.newArrayList("Ljava/lang/String;"), "V"));

        mutableMethodImplementation.addInstruction(++index, constString);
        mutableMethodImplementation.addInstruction(++index, invokeStaticRange);
    }

    /**
     * Performs the instrumentation, i.e. inserts the following instructions at each branch:
     * 1) const-string/16 pN, "unique-branch-id" (pN refers to the register with the highest ID)
     * 2) invoke-range-static Tracer.trace(pN)
     *
     * @return Return the instrumented {@code MethodImplementation}.
     */
    public static MethodImplementation modifyMethod(MethodInformation methodInformation) {

        assert methodInformation.getImplementation().isPresent();

        LOGGER.info("Instrumenting branches now...");

        int branchIndex = 0;

        Set<BuilderInstruction> coveredBranches = new HashSet<>();

        MutableMethodImplementation mutableImplementation =
                new MutableMethodImplementation(methodInformation.getImplementation().get());

        List<BuilderInstruction> instructions = mutableImplementation.getInstructions();

        // increase the register count of the method, i.e. the .register directive at each method's head
        Utility.increaseMethodRegisterCount(mutableImplementation, methodInformation.getTotalRegisterCount());


        for (int i = 0; i < instructions.size(); i++) {

            BuilderInstruction instruction = instructions.get(i);

            /**
             * Branching instructions are either identified by their opcode,
             * the prefix they share (i.e. IF_) or the format, which
             * is either '21t' (e.g. IF_EQZ) or '22t' (e.g. IF_EQ).
             */
            if ((instruction instanceof BuilderInstruction21t
                    || instruction instanceof BuilderInstruction22t)
                    && !coveredBranches.contains(instruction)) {

                // do not instrument the same branch multiple times
                coveredBranches.add(instruction);

                // unique branch id
                String id = methodInformation.getMethodID();
                id += "->" + branchIndex;

                int ifBranchIndex = instruction.getLocation().getIndex();

                insertInstrumentationCode(methodInformation, ifBranchIndex, id);

                branchIndex++;

                int elseBranchIndex = ((BuilderOffsetInstruction) instruction).getTarget().getLocation().getIndex();
                BuilderInstruction elseBranch = instructions.get(elseBranchIndex);

                if (!coveredBranches.contains(elseBranch)) {

                    coveredBranches.add(elseBranch);

                    id = methodInformation.getMethodID();
                    id += "->" + branchIndex;

                    insertInstrumentationCode(methodInformation, elseBranchIndex, id);

                    /*
                    * We cannot directly insert our instructions after the else-branch label (those instructions
                    * would fall between the goto and else-branch label). Instead we need to insert our
                    * instructions after the first instructions there, and swap them back afterwards.
                     */
                    mutableImplementation.swapInstructions(elseBranchIndex, elseBranchIndex + 1);
                    mutableImplementation.swapInstructions(elseBranchIndex + 1, elseBranchIndex + 2);
                }
                // we have to update the branchIndex in any way
                branchIndex++;
            }
        }
        return mutableImplementation;
    }


    /**
     * Inserts move instructions at the method entry in order to shift the parameter registers by
     * one position to the left, e.g. move p0, p1. This is necessary to make the last register(s)
     * free usable. Note that the move instructions depend on the type of the source register and
     * that wide types take up two consecutive registers, which is the actual need for two
     * additional registers, since p0 may have type wide in static methods.
     *
     * @param methodInformation Stores all relevant information about a method.
     * @return Returns the adapted implementation including the move instructions.
     */
    public static MethodImplementation shiftParamRegisters(MethodInformation methodInformation) {

        assert methodInformation.getImplementation().isPresent();
        assert methodInformation.getParamRegisterTypeMap().isPresent();

        MethodImplementation methodImplementation = methodInformation.getImplementation().get();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);
        Map<Integer,RegisterType> paramRegisterMap = methodInformation.getParamRegisterTypeMap().get();

        /*
        * The union of both lists represent basically the destination registers for
        * the move instructions, apart the last two registers which are getting
        * free registers.
         */
        List<Integer> newRegisters = methodInformation.getNewRegisters();
        List<Integer> paramRegisters = methodInformation.getParamRegisters();

        List<Integer> destinationRegisters = Stream.concat(newRegisters.stream(), paramRegisters.stream())
                .collect(Collectors.toList());

        // use correct move instruction depend on type of source register
        for (int index=0; index < paramRegisterMap.size() - 2; index++) {

            // check whether we have a wide type or not, note that first comes low half, then high half
            if (paramRegisterMap.get(index) == RegisterType.LONG_LO_TYPE
                    || paramRegisterMap.get(index) == RegisterType.DOUBLE_LO_TYPE) {

                Opcode moveWide = Opcode.MOVE_WIDE_FROM16;

                // destination register : {vnew0,vnew1,p0...pn}\{pn-1,pn}
                int destinationRegisterID  = destinationRegisters.get(index);
                LOGGER.info("Destination reg: " + destinationRegisterID);

                // source register : the next param register
                int sourceRegisterID = paramRegisters.get(index);
                LOGGER.info("Source reg: " + sourceRegisterID);

                // move wide vNew, vShiftedOut
                BuilderInstruction22x move = new BuilderInstruction22x(moveWide, destinationRegisterID, sourceRegisterID);
                // add move as first instruction
                mutableMethodImplementation.addInstruction(0, move);
            } else if (paramRegisterMap.get(index) == RegisterType.LONG_HI_TYPE
                    || paramRegisterMap.get(index) == RegisterType.DOUBLE_HI_TYPE) {

                // we reached the upper half of a wide-type, no additional move instruction necessary
                continue;
            } else if (paramRegisterMap.get(index).category == RegisterType.REFERENCE
                    || paramRegisterMap.get(index).category == RegisterType.NULL
                    || paramRegisterMap.get(index).category == RegisterType.UNINIT_THIS
                    || paramRegisterMap.get(index).category == RegisterType.UNINIT_REF) {

                // object type
                Opcode moveObject = Opcode.MOVE_OBJECT_FROM16;

                int destinationRegisterID  = destinationRegisters.get(index);
                int sourceRegisterID = paramRegisters.get(index);

                BuilderInstruction22x move = new BuilderInstruction22x(moveObject, destinationRegisterID, sourceRegisterID);
                mutableMethodImplementation.addInstruction(0, move);
            } else {

                // primitive type
                Opcode movePrimitive = Opcode.MOVE_FROM16;

                int destinationRegisterID  = destinationRegisters.get(index);
                int sourceRegisterID = paramRegisters.get(index);

                BuilderInstruction22x move = new BuilderInstruction22x(movePrimitive, destinationRegisterID, sourceRegisterID);
                mutableMethodImplementation.addInstruction(0, move);
            }
        }
        return mutableMethodImplementation;
    }


    public static MethodImplementation modifyShiftedRegisters(MethodImplementation implementation,
                                                            RegisterInformation information, List<RegisterType> registerTypes) {

        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(implementation);

        // check whether we have a wide type or not, assuming that high half comes first
        if (registerTypes.get(0) == RegisterType.LONG_HI_TYPE
                || registerTypes.get(0) == RegisterType.DOUBLE_HI_TYPE) {

            Opcode moveWide = Opcode.MOVE_WIDE_FROM16;
            // destination register : first new local register
            int destinationRegisterID  = information.getNewLocalRegisters().get(0);
            System.out.println("Destination reg: " + destinationRegisterID);
            // source register : first usable register
            int sourceRegisterID = information.getUsableRegisters().get(0);
            System.out.println("Source reg: " + sourceRegisterID);
            // move wide vNew, vShiftedOut
            BuilderInstruction22x move = new BuilderInstruction22x(moveWide, destinationRegisterID, sourceRegisterID);
            // add move as first instruction
            mutableMethodImplementation.addInstruction(0, move);
        } else {
            // either object or primitive -> individual move
            for (int i=0; i < registerTypes.size(); i++) {

                if (registerTypes.get(i).category == RegisterType.REFERENCE
                        || registerTypes.get(i).category == RegisterType.NULL) {

                    // object type
                    Opcode moveObject = Opcode.MOVE_OBJECT_FROM16;
                    // destination register : first new local register
                    int destinationRegisterID  = information.getNewLocalRegisters().get(i);
                    // source register : first usable register
                    int sourceRegisterID = information.getUsableRegisters().get(i);
                    // move wide vNew, vShiftedOut
                    BuilderInstruction22x move = new BuilderInstruction22x(moveObject, destinationRegisterID, sourceRegisterID);
                    // add move as first instruction
                    mutableMethodImplementation.addInstruction(i, move);
                } else {

                    // primitive type
                    Opcode movePrimitive = Opcode.MOVE_FROM16;
                    // destination register : first new local register
                    int destinationRegisterID  = information.getNewLocalRegisters().get(i);
                    // source register : first usable register
                    int sourceRegisterID = information.getUsableRegisters().get(i);
                    // move wide vNew, vShiftedOut
                    BuilderInstruction22x move = new BuilderInstruction22x(movePrimitive, destinationRegisterID, sourceRegisterID);
                    // add move as first instruction
                    mutableMethodImplementation.addInstruction(i, move);
                }
            }
        }

        // we need to finally replace the register IDs (usable with new local)

        return mutableMethodImplementation;
    }

}
