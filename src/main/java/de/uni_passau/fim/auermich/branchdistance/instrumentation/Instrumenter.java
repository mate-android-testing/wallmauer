package de.uni_passau.fim.auermich.branchdistance.instrumentation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.branchdistance.BranchDistance;
import de.uni_passau.fim.auermich.branchdistance.analysis.Analyzer;
import de.uni_passau.fim.auermich.branchdistance.branch.Branch;
import de.uni_passau.fim.auermich.branchdistance.branch.ElseBranch;
import de.uni_passau.fim.auermich.branchdistance.branch.IfBranch;
import de.uni_passau.fim.auermich.branchdistance.dto.MethodInformation;
import de.uni_passau.fim.auermich.branchdistance.utility.Utility;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.analysis.RegisterType;
import org.jf.dexlib2.base.BaseMethodParameter;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.util.MethodUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        // stays changed, no additional param register
        methodInformation.setParamRegisterCount(paramRegisters);
    }

    /**
     * Inserts a simple onDestroy method in the 'MainActivity' if it is not
     * present yet. The only purpose of this method is calling the 'write' method
     * of our tracer functionality, which in turn writes the collected traces
     * into the app-internal storage. The onDestroy method should be always called
     * upon the (normal) termination of the app.
     */
    public static void insertOnDestroy(List<Method> methods, ClassDef classDef, String packageName) {

        LOGGER.info("Inserting onDestroy method into 'MainActivity'");

        // onDestroy needs to call super()
        String superClass = classDef.getSuperclass();
        LOGGER.info("Super class: " + superClass);

        MutableMethodImplementation implementation = new MutableMethodImplementation(2);

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
                new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;", "write",
                        Lists.newArrayList("Ljava/lang/String;"), "V")));

        // we have to add return-statement as well, though void!
        implementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));

        // add method to final instrumented dex file
        methods.add(new ImmutableMethod(
                classDef.toString(),
                "onDestroy",
                null,
                "V",
                4,
                null,
                implementation));
    }

    /**
     * Adds a custom onDestroy method in the super classes of the MainActivity class
     * if not present yet.
     *
     * @param classes      The set of classes part of the instrumented classes.dex file.
     * @param mainActivity The class describing the MainActivity.
     */
    public static List<ClassDef> insertOnDestroyForSuperClasses(List<ClassDef> classes, ClassDef mainActivity) {

        LOGGER.info("Inserting onDestroy into super classes!");

        // track the classes which we gonna modify
        List<ClassDef> modifiedClasses = new ArrayList<>();

        // super class of MainActivity
        String superClass = mainActivity.getSuperclass();

        // when we found an onDestroy method in some super class, we can abort
        boolean abort = false;

        // we need to insert a custom onDestroy method until we reach the activity super class
        while (!abort && !superClass.equals("Landroid/app/Activity;")
                && !superClass.equals("Landroid/support/v7/app/AppCompatActivity;")
                && !superClass.equals("Landroid/support/v7/app/ActionBarActivity;")
                && !superClass.equals("Landroid/support/v4/app/FragmentActivity;")) {
            // find classDef of super class
            for (ClassDef classDef : classes) {
                if (classDef.toString().equals(superClass)) {
                    // we found classDef of super class

                    LOGGER.info("Super class of " + classDef.toString() + " is " + superClass);

                    // check whether class contains onDestroy method
                    List<Method> methods = Lists.newArrayList(classDef.getMethods());
                    boolean containsOnDestroy = false;

                    for (Method method : methods) {
                        if (Utility.isOnDestroy(method)) {
                            containsOnDestroy = true;
                        }
                    }

                    if (!containsOnDestroy) {

                        // insert custom onDestroy method
                        insertBasicOnDestroy(methods, classDef);

                        modifiedClasses.add(new ImmutableClassDef(
                                classDef.getType(),
                                classDef.getAccessFlags(),
                                classDef.getSuperclass(),
                                classDef.getInterfaces(),
                                classDef.getSourceFile(),
                                classDef.getAnnotations(),
                                classDef.getFields(),
                                methods));

                        // step up in activity hierarchy
                        superClass = classDef.getSuperclass();

                    } else {
                        // we are done
                        abort = true;
                    }

                    // speed up, don' need to consider other classes than super class
                    break;
                }
            }
        }

        // replace original classes with modified ones (including a custom onDestroy)
        List<ClassDef> finalClasses = new ArrayList<>();

        for (ClassDef classDef : classes) {

            boolean contained = false;

            for (ClassDef modifiedClassDef : modifiedClasses) {
                if (classDef.toString().equals(modifiedClassDef.toString())) {
                    // replace classDef with modifiedClassDef
                    finalClasses.add(modifiedClassDef);
                    contained = true;
                    break;
                }
            }

            if (!contained) {
                // add original classDef
                finalClasses.add(classDef);
            }
        }

        LOGGER.info("New number of classes: " + finalClasses.size());
        return finalClasses;
    }

    private static void insertBasicOnDestroy(List<Method> methods, ClassDef classDef) {

        LOGGER.info("Inserting basic onDestroy into class " + classDef);

        // onDestroy needs to call super()
        String superClass = classDef.getSuperclass();
        LOGGER.info("Super class: " + superClass);

        MutableMethodImplementation implementation = new MutableMethodImplementation(1);

        // call super.onDestroy() first, AppCompatActivity seems to be the current API standard for activity classes
        implementation.addInstruction(new BuilderInstruction35c(Opcode.INVOKE_SUPER, 1,
                0, 0, 0, 0, 0,
                new ImmutableMethodReference(superClass, "onDestroy",
                        Lists.newArrayList(), "V")));

        // we have to add return-statement as well, though void!
        implementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));

        // add method to final instrumented dex file
        methods.add(new ImmutableMethod(
                classDef.toString(),
                "onDestroy",
                null,
                "V",
                4,
                null,
                implementation));
    }

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
                    new ImmutableStringReference(classDef.toString()+"->"+method+"->entry")));

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
                    new ImmutableStringReference(classDef.toString()+"->"+method+"->exit")));

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
                    new ImmutableStringReference(classDef.toString()+"->"+method+"->entry")));

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
            implementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT,1));

            // exit string trace
            implementation.addInstruction(new BuilderInstruction21c(Opcode.CONST_STRING, 0,
                    new ImmutableStringReference(classDef.toString()+"->"+method+"->exit")));

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
     * If the 'MainActivity' already contains an onDestroy method, we have to integrate our tracer functionality
     * into it instead of creating an own onDestroy method. In general, we need to place before each 'return' statement
     * instructions that call our tracer.
     *
     * @param methodInformation Stores all relevant information about a method.
     * @param packageName       The package name declared in the AndroidManifest.xml file.
     */
    public static void modifyOnDestroy(MethodInformation methodInformation, String packageName) {

        LOGGER.info("Modifying onDestroy method of 'MainActivity'");

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);
        List<BuilderInstruction> instructions = mutableMethodImplementation.getInstructions();

        /*
         * Since we iterate over the instructions and insert before each return statement our code,
         * the insertion causes to re-iterate over the same return statement infinitely often.
         * Thus, we need to track the covered return statements.
         */
        Set<BuilderInstruction> coveredReturnStatements = new HashSet<>();

        // we need to insert the code before each return statement
        for (int i = 0; i < instructions.size(); i++) {

            BuilderInstruction instruction = instructions.get(i);

            // onDestroy has return type void, which opcodes refers to '0E'
            if (instructions.get(i).getOpcode().name.equals("return-void")
                    && !coveredReturnStatements.contains(instruction)) {

                coveredReturnStatements.add(instruction);

                LOGGER.info("Inserting Tracer.write(packageName) invocation before return statement!");

                // we require one parameter containing the unique branch id
                int freeRegisterID = methodInformation.getFreeRegisters().get(0);

                // const-string pN, "packageName" (pN refers to the free register at the end)
                BuilderInstruction21c constString = new BuilderInstruction21c(Opcode.CONST_STRING, freeRegisterID,
                        new ImmutableStringReference(packageName));

                // invoke-static-range
                BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                        freeRegisterID, 1,
                        new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;", "write",
                                Lists.newArrayList("Ljava/lang/String;"), "V"));

                mutableMethodImplementation.addInstruction(i, constString);
                mutableMethodImplementation.addInstruction(++i, invokeStaticRange);
            }
        }
        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
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
     * @param index             The position where we insert our instrumented code.
     * @param id                The id which identifies the given branch, i.e. packageName->className->method->branchID.
     * @return Returns the instrumented method implementation.
     */
    private static MutableMethodImplementation insertInstrumentationCode(MethodInformation methodInformation, int index, final String id) {

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // we require one parameter containing the unique branch id
        int freeRegisterID = methodInformation.getFreeRegisters().get(0);

        // const-string pN, "unique-branch-id" (pN refers to the free register at the end)
        BuilderInstruction21c constString = new BuilderInstruction21c(Opcode.CONST_STRING, freeRegisterID,
                new ImmutableStringReference(id));

        // invoke-static-range
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                freeRegisterID, 1,
                new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;", "trace",
                        Lists.newArrayList("Ljava/lang/String;"), "V"));

        mutableMethodImplementation.addInstruction(++index, constString);
        mutableMethodImplementation.addInstruction(++index, invokeStaticRange);

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
        return mutableMethodImplementation;
    }

    /**
     * Performs the instrumentation, i.e. inserts the following instructions at each branch:
     * 1) const-string/16 pN, "unique-branch-id" (pN refers to the register with the highest ID)
     * 2) invoke-range-static Tracer.trace(pN)
     * Also instruments the method entry and exit.
     */
    public static void modifyMethod(MethodInformation methodInformation) {

        LOGGER.info("Instrumenting branches now...");

        MutableMethodImplementation mutableImplementation =
                new MutableMethodImplementation(methodInformation.getMethodImplementation());

        LOGGER.info("Register count before increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        // increase the register count of the method, i.e. the .register directive at each method's head
        mutableImplementation = new MutableMethodImplementation(
                Utility.increaseMethodRegisterCount(methodInformation, methodInformation.getTotalRegisterCount()));

        LOGGER.info("Register count after increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        // sort the branches by their position/location within the method
        Set<Branch> sortedBranches = new TreeSet<>(methodInformation.getBranches());

        LOGGER.info(sortedBranches.toString());
        LOGGER.info("Number of Instructions: " + mutableImplementation.getInstructions().size());

        mutableImplementation.getInstructions().stream().forEach(
                instruction -> {
                    LOGGER.fine(instruction.getOpcode().toString());
                });

        /*
         * Traverse the branches backwards, i.e. the last branch comes first, in order
         * to avoid inherent index/position updates of other branches while instrumenting.
         * This resolves the issue of branches that share the same position, e.g. two
         * if branches refer to the same else branch. Since branches are only comparable
         * by their position, an intermediate index change would make branches incomparable,
         * thus leading to a multiple instrumentation of the same branch.
         */
        Iterator<Branch> iterator = ((TreeSet<Branch>) sortedBranches).descendingIterator();

        int branchIndex = sortedBranches.size() - 1;

        while (iterator.hasNext()) {

            Branch branch = iterator.next();

            // unique branch id: full-qualified method name + id of first instruction at branch
            String id = methodInformation.getMethodID();

            id += "->" + branch.getIndex();
            int branchPosition = branch.getIndex();

            // we need to insert our code before the first instruction residing at the if branch
            if (branch instanceof IfBranch) {
                branchPosition--;
            }

            LOGGER.info(branch.toString());

            // instrument branch
            mutableImplementation = insertInstrumentationCode(methodInformation, branchPosition, id);

            LOGGER.fine("Number of Instructions after Instrumentation: " + mutableImplementation.getInstructions().size());

            // swap instructions to right position when dealing with an else branch
            if (branch instanceof ElseBranch) {

                /*
                 * We cannot directly insert our instructions after the else-branch label (those instructions
                 * would fall between the goto and else-branch label). Instead we need to insert our
                 * instructions after the first instructions there, and swap them back afterwards.
                 */
                mutableImplementation.swapInstructions(branchPosition, branchPosition + 1);
                mutableImplementation.swapInstructions(branchPosition + 1, branchPosition + 2);
            }
            branchIndex--;
        }
        // update implementation
        methodInformation.setMethodImplementation(mutableImplementation);

        // we need to instrument the method entry (do this after branches, otherwise branch ids are corrupted)
        List<Integer> entryInstructionIDs = methodInformation.getEntryInstructionIDs();
        Collections.reverse(entryInstructionIDs);

        /*
        for (Integer entryInstructionID : entryInstructionIDs) {
            // consider offset of 1
            mutableImplementation = insertInstrumentationCode(methodInformation, entryInstructionID,
                    methodInformation.getMethodID() + "->entry" + entryInstructionID);
        }
        */

        // we need to instrument the try catch blocks
        List<Integer> tryCatchBlocks = Analyzer.analyzeTryCatchBlocks(methodInformation);
        Collections.reverse(tryCatchBlocks);

        for (Integer tryCatchBlock : tryCatchBlocks) {
            mutableImplementation = insertInstrumentationCode(methodInformation, tryCatchBlock,
                    methodInformation.getMethodID() + "->tryCatchBlock" + tryCatchBlock);
        }

        methodInformation.setMethodImplementation(mutableImplementation);

        // finally we need to instrument the method exit
        List<Integer> returnStmtIndices = new ArrayList<>();

        // collect the indices of the return statements
        for (BuilderInstruction instruction : mutableImplementation.getInstructions()) {
            // search for return instructions
            if (instruction.getOpcode() == Opcode.RETURN
                    || instruction.getOpcode() == Opcode.RETURN_VOID
                    || instruction.getOpcode() == Opcode.RETURN_OBJECT
                    || instruction.getOpcode() == Opcode.RETURN_WIDE
                    || instruction.getOpcode() == Opcode.RETURN_VOID_BARRIER
                    || instruction.getOpcode() == Opcode.RETURN_VOID_NO_BARRIER) {
                returnStmtIndices.add(instruction.getLocation().getIndex());
            }
        }

        // backwards traverse the return instructions -> no shifting issue
        Collections.reverse(returnStmtIndices);

        for (Integer returnStmtIndex : returnStmtIndices) {
            // insert the tracer functionality prior to each return statement
            mutableImplementation = insertInstrumentationCode(methodInformation, returnStmtIndex - 1,
                    methodInformation.getMethodID() + "->exit");
            // update implementation
            methodInformation.setMethodImplementation(mutableImplementation);
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
        LOGGER.fine(paramRegisterMap.toString());

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
