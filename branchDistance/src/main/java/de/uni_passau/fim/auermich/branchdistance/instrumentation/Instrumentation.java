package de.uni_passau.fim.auermich.branchdistance.instrumentation;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.branchdistance.BranchDistance;
import de.uni_passau.fim.auermich.branchdistance.analysis.Analyzer;
import de.uni_passau.fim.auermich.branchdistance.dto.MethodInformation;
import de.uni_passau.fim.auermich.branchdistance.utility.Range;
import de.uni_passau.fim.auermich.branchdistance.utility.Utility;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.analysis.AnalyzedInstruction;
import org.jf.dexlib2.analysis.RegisterType;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides the functionality to instrument a given method. This includes
 * the insertion of certain lifecycle methods, e.g. onCreate(), shifting of
 * param registers and the instrumentation of branches.
 */
public final class Instrumentation {

    private static final Logger LOGGER = LogManager.getLogger(Instrumentation.class);

    /**
     * Adds a basic lifecycle method to the given activity or fragment class. This already
     * includes the instrumentation of the method.
     *
     * @param method   The lifecycle method name.
     * @param methods  The list of methods belonging to the class.
     * @param classDef The activity or fragment class.
     */
    public static void addLifeCycleMethod(String method, List<Method> methods, ClassDef classDef) {

        String superClass = classDef.getSuperclass();

        String methodName = method.split("\\(")[0];
        String parameters = method.split("\\(")[1].split("\\)")[0];
        String returnType = method.split("\\)")[1];

        // ASSUMPTION: all parameters are objects (this is true for lifecycle methods)
        int paramCount = StringUtils.countMatches(parameters, ";");
        List<String> params = new ArrayList<>();

        if (paramCount > 0) {
            // the params have the form L../../../..; -> Landroid/view/View;
            params = Arrays.stream(parameters.split(";")).map(param -> param + ";").collect(Collectors.toList());
        }

        MutableMethodImplementation implementation;

        if (returnType.equals("V")) {

            // one local register v0 required -> p0 has index 0
            int paramIndex = 1;

            // we require one additional parameter for the invisible this reference p0 + one for trace
            implementation = new MutableMethodImplementation(2 + paramCount);

            // entry string trace
            implementation.addInstruction(new BuilderInstruction21c(Opcode.CONST_STRING, 0,
                    new ImmutableStringReference(classDef.toString() + "->" + method + "->entry")));

            // invoke-static-range
            implementation.addInstruction(new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                    0, 1,
                    new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;", "trace",
                            Lists.newArrayList("Ljava/lang/String;"), "V")));


            // call super method (we have one register for this reference + one register for each parameter)
            implementation.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_SUPER, 1 + paramCount,
                    paramIndex++, paramIndex++, paramIndex++, paramIndex++, paramIndex++,
                    new ImmutableMethodReference(superClass, methodName,
                            params, returnType)));

            // exit string trace
            implementation.addInstruction(new BuilderInstruction21c(Opcode.CONST_STRING, 0,
                    new ImmutableStringReference(classDef.toString() + "->" + method + "->exit")));

            // invoke-static-range
            implementation.addInstruction(new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                    0, 1,
                    new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;", "trace",
                            Lists.newArrayList("Ljava/lang/String;"), "V")));

            // we have to add return-statement as well, though void!
            implementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
        } else {

            // two local registers v0,v1 required -> p0 has index 2
            int paramIndex = 2;

            // we require one additional parameter for the invisible this reference p0, one for the trace and one for the return value
            implementation = new MutableMethodImplementation(3 + paramCount);

            // entry string trace
            implementation.addInstruction(new BuilderInstruction21c(Opcode.CONST_STRING, 0,
                    new ImmutableStringReference(classDef.toString() + "->" + method + "->entry")));

            // invoke-static-range
            implementation.addInstruction(new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                    0, 1,
                    new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;", "trace",
                            Lists.newArrayList("Ljava/lang/String;"), "V")));

            // call super method (we have one register for this reference + one register for each parameter)
            implementation.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_SUPER, 1 + paramCount,
                    paramIndex++, paramIndex++, paramIndex++, paramIndex++, paramIndex++,
                    new ImmutableMethodReference(superClass, methodName,
                            params, returnType)));

            // move-result v1
            implementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, 1));

            // exit string trace
            implementation.addInstruction(new BuilderInstruction21c(Opcode.CONST_STRING, 0,
                    new ImmutableStringReference(classDef.toString() + "->" + method + "->exit")));

            // invoke-static-range
            implementation.addInstruction(new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                    0, 1,
                    new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;", "trace",
                            Lists.newArrayList("Ljava/lang/String;"), "V")));

            // only onCreateView(..) returns Landroid/view/View; which is stored in v1
            implementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN_OBJECT, 1));
        }

        List<MethodParameter> methodParams = params.stream().map(p ->
                new ImmutableMethodParameter(p, null,
                        // use three random letters as param names
                        RandomStringUtils.random(3, true, false).toLowerCase()))
                .collect(Collectors.toList());

        // add instrumented method to set of methods
        methods.add(new ImmutableMethod(
                classDef.toString(),
                methodName,
                methodParams,
                returnType,
                4,
                null,
                implementation));
    }

    /**
     * Instruments the given branch with the tracer functionality.
     *
     * @param methodInformation Stores all relevant information about the given method.
     * @param index             Describes the location where we need to instrument.
     * @param id                The id which identifies the given branch, i.e. packageName->className->method->branchID.
     * @param elseBranch        Whether the location where we instrument refers to an else branch.
     * @return Returns the instrumented method implementation.
     */
    private static MutableMethodImplementation insertInstrumentationCode(MethodInformation methodInformation,
                                                                         int index,
                                                                         final String id, boolean elseBranch) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // the location of try blocks
        Set<Range> tryBlocks = methodInformation.getTryBlocks();

        // we require one parameter containing the unique branch id
        int freeRegisterID = methodInformation.getFreeRegisters().get(0);

        // the position of the instrumentation point
        final int instrumentationPoint  = index;

        // const-string pN, "unique-branch-id" (pN refers to the free register at the end)
        BuilderInstruction21c constString = new BuilderInstruction21c(Opcode.CONST_STRING, freeRegisterID,
                new ImmutableStringReference(id));

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                freeRegisterID, 1,
                new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;", "trace",
                        Lists.newArrayList("Ljava/lang/String;"), "V"));

        // check whether the branch is located within a try block
        if (tryBlocks.stream().anyMatch(range -> range.contains(instrumentationPoint))) {

            /*
             * The bytecode verifier doesn't allow us to insert our tracer functionality directly within
             * try blocks. Actually, only (implicit) try blocks around a synchronized block are affected,
             * but we consider here any try block. The problem arises from the fact that an invoke instruction
             * within a try block introduces an additional edge to corresponding catch blocks, although it may
             * never throw an exception. As a result, the register type of the monitor enter/exit instruction, e.g. v1,
             * might be two-fold (conflicted), which is rejected by the verifier, see
             * https://android.googlesource.com/platform/art/+/master/runtime/verifier/register_line.cc#367.
             *
             * Actually we can bypass the verifier by introducing a jump forward and backward mechanism. Instead of
             * inserting the tracer functionality directly, we insert a goto instruction, which jumps to the end of the
             * method and calls the tracer functionality and afterwards jumps back to the original position. Since
             * a goto instruction can't throw any exception, the verifier doesn't complain. However, we have to ensure
             * that we don't introduce a control flow to the pseudo instructions packed-switch-data, sparse-switch-data
             * or fill-array-data, see the constraint B22 at https://source.android.com/devices/tech/dalvik/constraints.
             *
             * The idea of this kind of hack was taken from the paper 'Fine-grained Code Coverage Measurement in
             * Automated Black-box Android Testing', see section 4.3.
             */

            LOGGER.debug("Instrumentation point within try block!");

            // the label + tracer functionality comes after the last instruction
            int afterLastInstruction = mutableMethodImplementation.getInstructions().size();

            // insert goto to jump to method end
            Label tracerLabel = mutableMethodImplementation.newLabelForIndex(afterLastInstruction);
            BuilderInstruction jumpForward = new BuilderInstruction30t(Opcode.GOTO_32, tracerLabel);

            if (elseBranch) {
                mutableMethodImplementation.addInstruction(index + 1, jumpForward);
                mutableMethodImplementation.swapInstructions(index, index + 1);
            } else {
                mutableMethodImplementation.addInstruction(index, jumpForward);
            }

            // create label at branch after forward jump
            Label branchLabel = mutableMethodImplementation.newLabelForIndex(index + 1);

            // insert tracer functionality at label near method end (+1 because we inserted already goto instruction at branch)
            mutableMethodImplementation.addInstruction(afterLastInstruction + 1, constString);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 2, invokeStaticRange);

            // insert goto to jump back to branch
            BuilderInstruction jumpBackward = new BuilderInstruction30t(Opcode.GOTO_32, branchLabel);
            mutableMethodImplementation.addInstruction(afterLastInstruction + 3, jumpBackward);
        } else {
            if (elseBranch) {
                mutableMethodImplementation.addInstruction(++index, constString);
                mutableMethodImplementation.addInstruction(++index, invokeStaticRange);

                /*
                 * We cannot directly insert our instructions after the else-branch label (those instructions
                 * would fall between the goto and else-branch label). Instead we need to insert our
                 * instructions after the first instructions there, and swap them back afterwards.
                 */
                mutableMethodImplementation.swapInstructions(index - 2, index - 1);
                mutableMethodImplementation.swapInstructions(index - 1, index);
            } else {
                mutableMethodImplementation.addInstruction(index, constString);
                mutableMethodImplementation.addInstruction(index + 1, invokeStaticRange);
            }
        }

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
        return mutableMethodImplementation;
    }

    /**
     * Performs the actual instrumentation. Inserts at each instrumentation point, i.e. branch or if stmt, a trace
     * statement. For each if stmt, the branch distance is computed as well. In addition, also method entry
     * and exit is instrumented.
     *
     * @param methodInformation Encapsulates a method and its instrumentation points.
     * @param dexFile           The dex file containing the method.
     */
    public static void modifyMethod(MethodInformation methodInformation, DexFile dexFile) {

        LOGGER.info("Register count before increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        // increase the register count of the method, i.e. the .register directive at each method's head
        Utility.increaseMethodRegisterCount(methodInformation, methodInformation.getTotalRegisterCount());

        LOGGER.info("Register count after increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        // instrument the branches first
        Set<Integer> coveredInstructionPoints = new HashSet<>();
        Set<InstrumentationPoint> instrumentationPoints = new TreeSet<>(methodInformation.getInstrumentationPoints());
        Iterator<InstrumentationPoint> iterator = ((TreeSet<InstrumentationPoint>) instrumentationPoints).descendingIterator();

        /*
         * Traverse the branches backwards, i.e. the last branch comes first, in order
         * to avoid inherent index/position updates of other branches while instrumenting.
         */
        while (iterator.hasNext()) {

            InstrumentationPoint instrumentationPoint = iterator.next();
            String trace = methodInformation.getMethodID() + "->" + instrumentationPoint.getPosition();

            if (instrumentationPoint.getType() == InstrumentationPoint.Type.IF_STMT) {
                // compute branch distance + trace
                computeBranchDistance(methodInformation, instrumentationPoint);

                // we only need to add a trace if it is not yet covered -> avoids instrumenting same location multiple times
                // this would happen when an if stmt is the first instruction of another branch
                if (!coveredInstructionPoints.contains(instrumentationPoint.getPosition())) {
                    // instrument branch with trace
                    coveredInstructionPoints.add(instrumentationPoint.getPosition());
                    insertInstrumentationCode(methodInformation, instrumentationPoint.getPosition(), trace, true);
                }

            } else {
                // instrument branch with trace
                coveredInstructionPoints.add(instrumentationPoint.getPosition());

                /*
                 * We can't directly insert a statement before the else branch, instead
                 * we need to insert our code after the first instruction of the else branch
                 * and later swap those instructions.
                 */
                boolean shiftInstruction = instrumentationPoint.getType() == InstrumentationPoint.Type.ELSE_BRANCH;
                insertInstrumentationCode(methodInformation, instrumentationPoint.getPosition(), trace, shiftInstruction);
            }
        }

        instrumentMethodEntry(methodInformation, dexFile);
        instrumentMethodExit(methodInformation);
        // instrumentTryCatchBlocks(methodInformation);
    }

    /**
     * Inserts instructions before every if stmt in order to invoke the branch distance computation.
     *
     * @param methodInformation    Encapsulates the method.
     * @param instrumentationPoint Encapsulates information about the if stmt.
     */
    private static void computeBranchDistance(MethodInformation methodInformation, InstrumentationPoint instrumentationPoint) {

        MutableMethodImplementation mutableImplementation =
                new MutableMethodImplementation(methodInformation.getMethodImplementation());

        // get the if instruction
        int instructionIndex = instrumentationPoint.getPosition();
        AnalyzedInstruction instruction = methodInformation.getInstructionAtIndex(instructionIndex);

        // map op code to internal operation code
        int operation = mapOpCodeToOperation(instruction.getOriginalInstruction().getOpcode());

        if (instruction.getOriginalInstruction().getOpcode().format == Format.Format21t) {
            // unary operation -> if-eqz v0
            BuilderInstruction21t instruction21t = (BuilderInstruction21t) instrumentationPoint.getInstruction();
            int registerA = instruction21t.getRegisterA();

            RegisterType registerTypeA = instruction.getPreInstructionRegisterType(registerA);

            LOGGER.info("Method: " + methodInformation.getMethodID());
            LOGGER.info("IF-Instruction: " + instruction.getOriginalInstruction().getOpcode() + "[" + instructionIndex + "]");
            LOGGER.info("RegisterA: " + registerA + "[" + registerTypeA + "]");

            // check whether we deal with primitive or object types
            if (registerTypeA.category != RegisterType.REFERENCE && registerTypeA.category != RegisterType.UNINIT_REF) {
                handlePrimitiveUnaryComparison(methodInformation, instructionIndex, operation, registerA);
            } else {
                handleObjectUnaryComparison(methodInformation, instructionIndex, operation, registerA);
            }

        } else {
            // binary operation -> if-eq v0, v1
            BuilderInstruction22t instruction22t = (BuilderInstruction22t) instrumentationPoint.getInstruction();
            int registerA = instruction22t.getRegisterA();
            int registerB = instruction22t.getRegisterB();

            RegisterType registerTypeA = instruction.getPreInstructionRegisterType(registerA);
            RegisterType registerTypeB = instruction.getPreInstructionRegisterType(registerB);

            LOGGER.info("Method: " + methodInformation.getMethodID());
            LOGGER.info("IF-Instruction: " + instruction.getOriginalInstruction().getOpcode() + "[" + instructionIndex + "]");
            LOGGER.info("RegisterA: " + registerA + "[" + registerTypeA + "]");
            LOGGER.info("RegisterB: " + registerB + "[" + registerTypeB + "]");

            Set<Byte> referenceTypes = new HashSet<Byte>() {{
                add(RegisterType.REFERENCE);
                add(RegisterType.UNINIT_REF);
            }};

            if (!referenceTypes.contains(registerTypeA.category) && !referenceTypes.contains(registerTypeB.category)) {
                handlePrimitiveBinaryComparison(methodInformation, instructionIndex, operation, registerA, registerB);
            } else if (referenceTypes.contains(registerTypeA.category) && referenceTypes.contains(registerTypeB.category)) {
                handleObjectBinaryComparison(methodInformation, instructionIndex, operation, registerA, registerB);
            } else {
                throw new IllegalStateException("Comparing objects with primitives!");
            }
        }
    }

    /**
     * Inserts code to invoke the branch distance computation for an if statement that has
     * a single primitive argument, e.g. if-eqz v0. We simply call 'branchDistance(int op_type, int argument)'.
     *
     * @param methodInformation Encapsulates a method.
     * @param instructionIndex  The instruction index of the if statement.
     * @param operation         The operation id, e.g. 0 for if-eqz.
     * @param registerA         The register id of the argument register.
     */
    private static void handlePrimitiveUnaryComparison(MethodInformation methodInformation, int instructionIndex,
                                                       int operation, int registerA) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // the location of try blocks
        Set<Range> tryBlocks = methodInformation.getTryBlocks();

        final int instrumentationPoint = instructionIndex;

        // we require one parameter for the operation identifier
        int firstFreeRegister = methodInformation.getFreeRegisters().get(0);

        // we need another free register for the single argument of the if instruction
        int secondFreeRegister = methodInformation.getFreeRegisters().get(1);

        // const/4 vA, #+B - stores the operation type identifier, e.g. 0 for if-eqz
        BuilderInstruction31i operationID = new BuilderInstruction31i(Opcode.CONST, firstFreeRegister, operation);

        // we need to move the content of single if instruction argument to the second free register
        // this enables us to us it with the invoke-static range instruction
        BuilderInstruction32x move = new BuilderInstruction32x(Opcode.MOVE_16, secondFreeRegister, registerA);

        // FIXME: invoke-static can only handle register IDs < 16, so any free register above v15 is unusable
        // IDEA: USE 3 ADDITIONAL_REGISTERS and move the arguments of if-instruction into those registers
        // 1 for operation code,  1 for argument 1,  for argument 2 (only for binary)

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                firstFreeRegister, 2,
                new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;",
                        "computeBranchDistance",
                        Lists.newArrayList("II"), "V"));

        // TODO: I have the fear that using the newly created for both branches (strings) and arguments (any type)
        //  could break the verification process. As far as I remember, the type of a register must be consistent
        //  throughout entire try-catch blocks. Thus, we may require 5 additional registers, where the first two
        //  are used for branches (actually only 1, the second is for shifting of wide params) and the remaining 3
        //  solely for the operation opcode and the max 2 args of if stmts.

        // check whether if stmt is located within a try block
        // TODO: check whether 'contains()' is valid for if instruction, may adjust index or strictness relation
        if (tryBlocks.stream().anyMatch(range -> range.contains(instrumentationPoint))) {
            // TODO: introduce same mechanism as used in branchcoverage
        } else {

            mutableMethodImplementation.addInstruction(++instructionIndex, operationID);
            mutableMethodImplementation.addInstruction(++instructionIndex, move);
            mutableMethodImplementation.addInstruction(++instructionIndex, invokeStaticRange);

            mutableMethodImplementation.swapInstructions(instructionIndex - 3, instructionIndex - 2);
            mutableMethodImplementation.swapInstructions(instructionIndex - 2, instructionIndex - 1);
            mutableMethodImplementation.swapInstructions(instructionIndex - 1, instructionIndex);
        }

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }

    /**
     * Inserts code to invoke the branch distance computation for an if statement that has
     * two primitive arguments, e.g. if-eq v0, v1. We simply call '
     * branchDistance(int op_type, int argument1, int argument2)'.
     *
     * @param methodInformation Encapsulates a method.
     * @param instructionIndex  The instruction index of the if statement.
     * @param operation         The operation id, e.g. 0 for if-eq v0, v1.
     * @param registerA         The register id of the first argument register.
     * @param registerB         The register id of the second argument register.
     */
    private static void handlePrimitiveBinaryComparison(MethodInformation methodInformation, int instructionIndex,
                                                        int operation, int registerA, int registerB) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // we require one parameter for the operation identifier
        int firstFreeRegister = methodInformation.getFreeRegisters().get(0);

        // we need another free register for the first argument of the if instruction
        int secondFreeRegister = methodInformation.getFreeRegisters().get(1);

        // we need another free register for the second argument of the if instruction
        int thirdFreeRegister = methodInformation.getFreeRegisters().get(2);

        // const/4 vA, #+B - stores the operation type identifier, e.g. 0 for if-eqz
        BuilderInstruction31i operationID = new BuilderInstruction31i(Opcode.CONST, firstFreeRegister, operation);

        // we need to move the content of the first if instruction argument to the second free register
        // this enables us to us it with the invoke-static range instruction
        BuilderInstruction32x moveA = new BuilderInstruction32x(Opcode.MOVE_16, secondFreeRegister, registerA);

        // we need to move the content of the second if instruction argument to the third free register
        // this enables us to us it with the invoke-static range instruction
        BuilderInstruction32x moveB = new BuilderInstruction32x(Opcode.MOVE_16, thirdFreeRegister, registerB);

        // FIXME: invoke-static can only handle register IDs < 16, so any free register above v15 is unusable
        // IDEA: USE 3 ADDITIONAL_REGISTERS and move the arguments of if-instruction into those registers
        // 1 for operation code,  1 for argument 1,  for argument 2 (only for binary)

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                firstFreeRegister, 3,
                new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;",
                        "computeBranchDistance",
                        Lists.newArrayList("III"), "V"));

        // TODO: I have the fear that using the newly created for both branches (strings) and arguments (any type)
        //  could break the verification process. As far as I remember, the type of a register must be consistent
        //  throughout entire try-catch blocks. Thus, we may require 5 additional registers, where the first two
        //  are used for branches (actually only 1, the second is for shifting of wide params) and the remaining 3
        //  solely for the operation opcode and the max 2 args of if stmts.

        mutableMethodImplementation.addInstruction(++instructionIndex, operationID);
        mutableMethodImplementation.addInstruction(++instructionIndex, moveA);
        mutableMethodImplementation.addInstruction(++instructionIndex, moveB);
        mutableMethodImplementation.addInstruction(++instructionIndex, invokeStaticRange);

        mutableMethodImplementation.swapInstructions(instructionIndex - 4, instructionIndex - 3);
        mutableMethodImplementation.swapInstructions(instructionIndex - 3, instructionIndex - 2);
        mutableMethodImplementation.swapInstructions(instructionIndex - 2, instructionIndex - 1);
        mutableMethodImplementation.swapInstructions(instructionIndex - 1, instructionIndex);

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }

    /**
     * Inserts code to invoke the branch distance computation for an if statement that has
     * a single object argument, e.g. if-eqz v0. We simply call 'branchDistance(int op_type, Object argument)'.
     *
     * @param methodInformation Encapsulates a method.
     * @param instructionIndex  The instruction index of the if statement.
     * @param operation         The operation id, e.g. 0 for if-eqz.
     * @param registerA         The register id of the argument register.
     */
    private static void handleObjectUnaryComparison(MethodInformation methodInformation, int instructionIndex,
                                                    int operation, int registerA) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // we require one parameter for the operation identifier
        int firstFreeRegister = methodInformation.getFreeRegisters().get(0);

        // we need another free register for the single argument of the if instruction
        int secondFreeRegister = methodInformation.getFreeRegisters().get(1);

        // const/4 vA, #+B - stores the operation type identifier, e.g. 0 for if-eqz
        BuilderInstruction31i operationID = new BuilderInstruction31i(Opcode.CONST, firstFreeRegister, operation);

        // we need to move the content of single if instruction argument to the second free register
        // this enables us to us it with the invoke-static range instruction
        BuilderInstruction32x move = new BuilderInstruction32x(Opcode.MOVE_OBJECT_16, secondFreeRegister, registerA);

        // FIXME: invoke-static can only handle register IDs < 16, so any free register above v15 is unusable
        // IDEA: USE 3 ADDITIONAL_REGISTERS and move the arguments of if-instruction into those registers
        // 1 for operation code,  1 for argument 1,  for argument 2 (only for binary)

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                firstFreeRegister, 2,
                new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;",
                        "computeBranchDistance",
                        Lists.newArrayList("ILjava/lang/Object;"), "V"));

        // TODO: I have the fear that using the newly created for both branches (strings) and arguments (any type)
        //  could break the verification process. As far as I remember, the type of a register must be consistent
        //  throughout entire try-catch blocks. Thus, we may require 5 additional registers, where the first two
        //  are used for branches (actually only 1, the second is for shifting of wide params) and the remaining 3
        //  solely for the operation opcode and the max 2 args of if stmts.

        mutableMethodImplementation.addInstruction(++instructionIndex, operationID);
        mutableMethodImplementation.addInstruction(++instructionIndex, move);
        mutableMethodImplementation.addInstruction(++instructionIndex, invokeStaticRange);

        mutableMethodImplementation.swapInstructions(instructionIndex - 3, instructionIndex - 2);
        mutableMethodImplementation.swapInstructions(instructionIndex - 2, instructionIndex - 1);
        mutableMethodImplementation.swapInstructions(instructionIndex - 1, instructionIndex);

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }

    /**
     * Inserts code to invoke the branch distance computation for an if statement that has
     * two object arguments, e.g. if-eq v0, v1. We simply call '
     * branchDistance(int op_type, Object argument1, Object argument2)'.
     *
     * @param methodInformation Encapsulates a method.
     * @param instructionIndex  The instruction index of the if statement.
     * @param operation         The operation id, e.g. 0 for if-eq v0, v1.
     * @param registerA         The register id of the first argument register.
     * @param registerB         The register id of the second argument register.
     */
    private static void handleObjectBinaryComparison(MethodInformation methodInformation, int instructionIndex,
                                                     int operation, int registerA, int registerB) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // we require one parameter for the operation identifier
        int firstFreeRegister = methodInformation.getFreeRegisters().get(0);

        // we need another free register for the first argument of the if instruction
        int secondFreeRegister = methodInformation.getFreeRegisters().get(1);

        // we need another free register for the second argument of the if instruction
        int thirdFreeRegister = methodInformation.getFreeRegisters().get(2);

        // const/4 vA, #+B - stores the operation type identifier, e.g. 0 for if-eqz
        BuilderInstruction31i operationID = new BuilderInstruction31i(Opcode.CONST, firstFreeRegister, operation);

        // we need to move the content of the first if instruction argument to the second free register
        // this enables us to us it with the invoke-static range instruction
        BuilderInstruction32x moveA = new BuilderInstruction32x(Opcode.MOVE_OBJECT_16, secondFreeRegister, registerA);

        // we need to move the content of the second if instruction argument to the third free register
        // this enables us to us it with the invoke-static range instruction
        BuilderInstruction32x moveB = new BuilderInstruction32x(Opcode.MOVE_OBJECT_16, thirdFreeRegister, registerB);

        // FIXME: invoke-static can only handle register IDs < 16, so any free register above v15 is unusable
        // IDEA: USE 3 ADDITIONAL_REGISTERS and move the arguments of if-instruction into those registers
        // 1 for operation code,  1 for argument 1,  for argument 2 (only for binary)

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                firstFreeRegister, 3,
                new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;",
                        "computeBranchDistance",
                        Lists.newArrayList("ILjava/lang/Object;Ljava/lang/Object;"), "V"));

        // TODO: I have the fear that using the newly created for both branches (strings) and arguments (any type)
        //  could break the verification process. As far as I remember, the type of a register must be consistent
        //  throughout entire try-catch blocks. Thus, we may require 5 additional registers, where the first two
        //  are used for branches (actually only 1, the second is for shifting of wide params) and the remaining 3
        //  solely for the operation opcode and the max 2 args of if stmts.

        mutableMethodImplementation.addInstruction(++instructionIndex, operationID);
        mutableMethodImplementation.addInstruction(++instructionIndex, moveA);
        mutableMethodImplementation.addInstruction(++instructionIndex, moveB);
        mutableMethodImplementation.addInstruction(++instructionIndex, invokeStaticRange);

        mutableMethodImplementation.swapInstructions(instructionIndex - 4, instructionIndex - 3);
        mutableMethodImplementation.swapInstructions(instructionIndex - 3, instructionIndex - 2);
        mutableMethodImplementation.swapInstructions(instructionIndex - 2, instructionIndex - 1);
        mutableMethodImplementation.swapInstructions(instructionIndex - 1, instructionIndex);

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }

    /**
     * Maps an opcode to an internal operation type identifier.
     *
     * @param opcode The given opcode.
     * @return Returns the internal operation type identifier
     * for the given opcode.
     */
    private static int mapOpCodeToOperation(Opcode opcode) {

        switch (opcode) {
            case IF_EQZ:
            case IF_EQ:
                return 0;
            case IF_NEZ:
            case IF_NE:
                return 1;
            case IF_LEZ:
            case IF_LE:
                return 2;
            case IF_LTZ:
            case IF_LT:
                return 3;
            case IF_GEZ:
            case IF_GE:
                return 4;
            case IF_GTZ:
            case IF_GT:
                return 5;
            default:
                throw new UnsupportedOperationException("Opcode: " + opcode + " not yet supported!");
        }
    }

    /**
     * Instruments try-catch blocks of a method. This is necessary to ensure which path was taken
     * in the control-flow graph. Currently unused.
     *
     * @param methodInformation Encapsulates the method to be instrumented.
     */
    private static void instrumentTryCatchBlocks(MethodInformation methodInformation) {

        /*
         * TODO: To get the full path going through a try-catch block, it is probably
         *   necessary to instrument AFTER each statement within a try block, which
         *   is linked to the catch block, and, in addition, it is necessary to instrument
         *   the beginning of a catch block. But how should we handle for instance return/throw
         *   instructions within try blocks, we can't insert our trace after those instructions.
         *   Probably we need to find a trade-off here.
         */

        List<Integer> tryCatchBlocks = Analyzer.analyzeTryCatchBlocks(methodInformation);
        Collections.reverse(tryCatchBlocks);

        for (Integer tryCatchBlock : tryCatchBlocks) {
            insertInstrumentationCode(methodInformation, tryCatchBlock,
                    methodInformation.getMethodID() + "->tryCatchBlock" + tryCatchBlock, false);
        }

    }

    /**
     * Instruments the method entry, i.e. before the first or the first instruction within a catch block a trace is inserted.
     *
     * @param methodInformation Encapsulates the method to be instrumented.
     * @param dexFile           The dex file containing the method.
     */
    private static void instrumentMethodEntry(MethodInformation methodInformation, DexFile dexFile) {

        // request entry instructions in place, otherwise instructions ids are out of date
        List<Integer> entryInstructionIDs = Analyzer.trackEntryInstructions(methodInformation, dexFile);
        Collections.reverse(entryInstructionIDs);

        final String trace = methodInformation.getMethodID() + "->entry";

        for (Integer entryInstructionID : entryInstructionIDs) {
            /*
             * We need to treat method entries similar to else branches in order to avoid that
             * label/instruction issue. Moreover, a method entry that is not the first instruction
             * (it is the first instruction of the catch block) needs a special treatment. The bytecode
             * verifier ensures that the move-exception instruction must be the first instruction of
             * the catch block, thus we can't insert any instruction before move-exception. Instead,
             * we have to insert our trace after the move-exception instruction.
             */
            if (entryInstructionID > 0) {
                entryInstructionID++;
            }
            insertInstrumentationCode(methodInformation, entryInstructionID, trace, true);
        }

    }

    /**
     * Instruments the method exit, i.e. before each return or throw statement a trace is inserted.
     *
     * @param methodInformation Encapsulates the method to be instrumented.
     */
    private static void instrumentMethodExit(MethodInformation methodInformation) {

        MutableMethodImplementation mutableImplementation =
                new MutableMethodImplementation(methodInformation.getMethodImplementation());

        List<Integer> returnOrThrowStmtIndices = new ArrayList<>();

        // collect the indices of the return or throw statements
        for (BuilderInstruction instruction : mutableImplementation.getInstructions()) {
            if (instruction.getOpcode() == Opcode.RETURN
                    || instruction.getOpcode() == Opcode.RETURN_VOID
                    || instruction.getOpcode() == Opcode.RETURN_OBJECT
                    || instruction.getOpcode() == Opcode.RETURN_WIDE
                    || instruction.getOpcode() == Opcode.RETURN_VOID_BARRIER
                    || instruction.getOpcode() == Opcode.RETURN_VOID_NO_BARRIER
                    || instruction.getOpcode() == Opcode.THROW
                    || instruction.getOpcode() == Opcode.THROW_VERIFICATION_ERROR) {
                returnOrThrowStmtIndices.add(instruction.getLocation().getIndex());
            }
        }

        // backwards traverse the return instructions -> no shifting issue
        Collections.reverse(returnOrThrowStmtIndices);

        final String trace = methodInformation.getMethodID() + "->exit";

        for (Integer returnOrThrowStmtIndex : returnOrThrowStmtIndices) {
            /*
             * If a label is attached to a return statement, which is often the case, the insertion
             * between the label and the return statement is not directly possible. Instead, we
             * need to use the same approach as for else branches.
             */
            insertInstrumentationCode(methodInformation, returnOrThrowStmtIndex, trace, true);
        }
    }


    /**
     * Inserts move instructions at the method entry in order to shift the parameter registers by
     * one position to the left, e.g. move p0, p1. This is necessary to make the last register(s)
     * free usable. Note that the move instructions depend on the type of the source register and
     * that wide types take up two consecutive registers, which is the actual need for two
     * additional registers, since p0 may have type wide in static methods.
     *
     * @param methodInformation Stores all relevant information about a method.
     */
    public static void shiftParamRegisters(MethodInformation methodInformation) {

        assert methodInformation.getParamRegisterTypeMap().isPresent();

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        Map<Integer, RegisterType> paramRegisterMap = methodInformation.getParamRegisterTypeMap().get();
        LOGGER.debug(paramRegisterMap.toString());

        List<Integer> newRegisters = methodInformation.getNewRegisters();
        List<Integer> paramRegisters = methodInformation.getParamRegisters();

        // compute the list of destination registers, which ranges from newReg(0)...newReg(0) + #params - 1
        // TODO: should be simple the place of the original param regs, i.e. destRegs = paramRegs
        int firstDestinationRegister = newRegisters.get(0);
        int lastDestinationRegister = newRegisters.get(0) + paramRegisters.size();
        List<Integer> destinationRegisters = IntStream.
                range(firstDestinationRegister, lastDestinationRegister).boxed().collect(Collectors.toList());

        // compute the list of source registers, which is the param registers shifted by #additionalRegs
        List<Integer> sourceRegisters =
                paramRegisters.stream().map(elem -> elem + BranchDistance.ADDITIONAL_REGISTERS).collect(Collectors.toList());

        LOGGER.info("New Registers: " + newRegisters);
        LOGGER.info("Parameter Registers: " + paramRegisters);
        LOGGER.info("Destination Registers: " + destinationRegisters);
        LOGGER.info("Source Registers: " + sourceRegisters);

        // we need a seperate counter for the insertion location of the instructions, since we skip indices when facing wide tpyes
        int pos = 0;

        // use correct move instruction depend on type of source register
        for (int index = 0; index < sourceRegisters.size(); index++) {

            // id corresponds to actual register ID of param register
            RegisterType registerType = paramRegisterMap.get(paramRegisters.get(index));

            // check whether we have a wide type or not, note that first comes low half, then high half
            if (registerType == RegisterType.LONG_LO_TYPE
                    || registerType == RegisterType.DOUBLE_LO_TYPE) {

                Opcode moveWide = Opcode.MOVE_WIDE_FROM16;

                LOGGER.info("Wide type LOW_HALF!");

                // destination register : {vnew0,vnew1,p0...pn}\{pn-1,pn}
                int destinationRegisterID = destinationRegisters.get(index);

                // source register : p0...pN
                int sourceRegisterID = sourceRegisters.get(index);

                LOGGER.info("Destination reg: " + destinationRegisterID);
                LOGGER.info("Source reg: " + sourceRegisterID);

                // move wide vNew, vShiftedOut
                BuilderInstruction22x move = new BuilderInstruction22x(moveWide, destinationRegisterID, sourceRegisterID);
                // add move as first instruction
                mutableMethodImplementation.addInstruction(pos, move);
                pos++;
            } else if (registerType == RegisterType.LONG_HI_TYPE
                    || registerType == RegisterType.DOUBLE_HI_TYPE) {

                LOGGER.info("Wide type HIGH_HALF!");

                // we reached the upper half of a wide-type, no additional move instruction necessary
                LOGGER.info("(Skipping) source reg:" + sourceRegisters.get(index));
                LOGGER.info("(Skipping) destination reg: " + destinationRegisters.get(index));
                continue;
            } else if (registerType.category == RegisterType.REFERENCE
                    || registerType.category == RegisterType.NULL
                    || registerType.category == RegisterType.UNINIT_THIS
                    || registerType.category == RegisterType.UNINIT_REF) {

                // object type
                Opcode moveObject = Opcode.MOVE_OBJECT_FROM16;

                LOGGER.info("Object type!");

                int destinationRegisterID = destinationRegisters.get(index);
                int sourceRegisterID = sourceRegisters.get(index);

                LOGGER.info("Destination reg: " + destinationRegisterID);
                LOGGER.info("Source reg: " + sourceRegisterID);

                BuilderInstruction22x move = new BuilderInstruction22x(moveObject, destinationRegisterID, sourceRegisterID);
                mutableMethodImplementation.addInstruction(pos, move);
                pos++;
            } else {

                // primitive type
                Opcode movePrimitive = Opcode.MOVE_FROM16;

                LOGGER.info("Primitive type!");

                int destinationRegisterID = destinationRegisters.get(index);
                int sourceRegisterID = sourceRegisters.get(index);

                LOGGER.info("Destination reg: " + destinationRegisterID);
                LOGGER.info("Source reg: " + sourceRegisterID);

                BuilderInstruction22x move = new BuilderInstruction22x(movePrimitive, destinationRegisterID, sourceRegisterID);
                mutableMethodImplementation.addInstruction(pos, move);
                pos++;
            }
        }
        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
    }
}
