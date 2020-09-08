package de.uni_passau.fim.auermich.branchcoverage.instrumentation;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.branchcoverage.BranchCoverage;
import de.uni_passau.fim.auermich.branchcoverage.analysis.Analyzer;
import de.uni_passau.fim.auermich.branchcoverage.dto.MethodInformation;
import de.uni_passau.fim.auermich.branchcoverage.utility.Utility;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.analysis.AnalyzedInstruction;
import org.jf.dexlib2.analysis.RegisterType;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides the functionality to instrument a given method. This includes
 * the insertion of certain lifecycle methods, e.g. onCreate(), shifting of
 * param registers and the instrumentation of branches.
 */
public final class Instrumentation {

    private static final Logger LOGGER = Logger.getLogger(Instrumentation.class
            .getName());

    /**
     * Instruments the given branch with the tracer functionality.
     *
     * @param methodInformation Stores all relevant information about the given method.
     * @param index             The position where we insert our instrumented code.
     * @param id                The id which identifies the given branch, i.e. packageName->className->method->branchID.
     * @param elseBranch        Whether the location where we instrument refers to an else branch.
     * @return Returns the instrumented method implementation.
     */
    private static MutableMethodImplementation insertInstrumentationCode(MethodInformation methodInformation, int index, final String id, boolean elseBranch) {

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
                new ImmutableMethodReference("Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;", "trace",
                        Lists.newArrayList("Ljava/lang/String;"), "V"));

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

        MutableMethodImplementation mutableImplementation =
                new MutableMethodImplementation(methodInformation.getMethodImplementation());

        LOGGER.info("Register count before increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        // increase the register count of the method, i.e. the .register directive at each method's head
        mutableImplementation = new MutableMethodImplementation(
                Utility.increaseMethodRegisterCount(methodInformation, methodInformation.getTotalRegisterCount()));

        LOGGER.info("Register count after increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        Set<InstrumentationPoint> instrumentationPoints = new TreeSet<>(methodInformation.getInstrumentationPoints());
        Iterator<InstrumentationPoint> iterator = ((TreeSet<InstrumentationPoint>) instrumentationPoints).descendingIterator();

        /*
         * Traverse the branches backwards, i.e. the last branch comes first, in order
         * to avoid inherent index/position updates of other branches while instrumenting.
         */
        while (iterator.hasNext()) {

            InstrumentationPoint instrumentationPoint = iterator.next();
            String trace = methodInformation.getMethodID() + "->" + instrumentationPoint.getPosition();

            /*
             * We can't directly insert a statement before the else branch, instead
             * we need to insert our code after the first instruction of the else branch
             * and later swap those instructions.
             */
            boolean shiftInstruction = instrumentationPoint.getType() == InstrumentationPoint.Type.ELSE_BRANCH;
            mutableImplementation = insertInstrumentationCode(methodInformation, instrumentationPoint.getPosition(), trace, shiftInstruction);
        }

        // update implementation
        methodInformation.setMethodImplementation(mutableImplementation);
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
                paramRegisters.stream().map(elem -> elem + BranchCoverage.ADDITIONAL_REGISTERS).collect(Collectors.toList());

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
