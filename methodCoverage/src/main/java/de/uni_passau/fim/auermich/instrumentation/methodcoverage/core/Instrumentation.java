package de.uni_passau.fim.auermich.instrumentation.methodcoverage.core;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.instrumentation.methodcoverage.MethodCoverage;
import de.uni_passau.fim.auermich.instrumentation.methodcoverage.dto.MethodInformation;
import de.uni_passau.fim.auermich.instrumentation.methodcoverage.utility.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.analysis.RegisterType;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11n;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides the functionality to instrument a given method and the shifting of parameter registers.
 */
public final class Instrumentation {

    private static final Logger LOGGER = LogManager.getLogger(Instrumentation.class);

    private static final String TRACER = "Lde/uni_passau/fim/auermich/tracer/Tracer;";

    /**
     * Instruments the method entry with a trace. Also increases the method's registry count.
     *
     * @param methodInformation Encapsulates a method.
     */
    public static void modifyMethod(MethodInformation methodInformation) {

        LOGGER.info("Register count before increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        // increase the register count of the method, i.e. the .register directive at each method's head
        Utility.increaseMethodRegisterCount(methodInformation, methodInformation.getTotalRegisterCount());

        LOGGER.info("Register count after increase: " + methodInformation.getMethodImplementation().getRegisterCount());

        MethodImplementation methodImplementation = methodInformation.getMethodImplementation();
        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(methodImplementation);

        // we require one parameter containing the unique method id
        int freeRegisterID = methodInformation.getFreeRegisters().get(0);

        // store method id in register
        BuilderInstruction21c constString = new BuilderInstruction21c(Opcode.CONST_STRING, freeRegisterID,
                new ImmutableStringReference(methodInformation.getMethodID()));

        // invoke trace(method id)
        BuilderInstruction3rc invokeStaticRange = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE,
                freeRegisterID, 1,
                new ImmutableMethodReference(TRACER, "trace",
                        Lists.newArrayList("Ljava/lang/String;"), "V"));

        mutableMethodImplementation.addInstruction(0, constString);
        mutableMethodImplementation.addInstruction(1, invokeStaticRange);

        // update implementation
        methodInformation.setMethodImplementation(mutableMethodImplementation);
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
        LOGGER.info(paramRegisterMap.toString());

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
                paramRegisters.stream().map(elem -> elem + MethodCoverage.ADDITIONAL_REGISTERS).collect(Collectors.toList());

        LOGGER.info("New Registers: " + newRegisters);
        LOGGER.info("Parameter Registers: " + paramRegisters);
        LOGGER.info("Destination Registers: " + destinationRegisters);
        LOGGER.info("Source Registers: " + sourceRegisters);

        // we need a separate counter for the insertion location of the instructions, since we skip indices
        // when facing wide types
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
            } else if (registerType == RegisterType.LONG_HI_TYPE || registerType == RegisterType.DOUBLE_HI_TYPE) {

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
                if (registerType.category == RegisterType.CONFLICTED) {
                    /* Conflicted types cannot be read from and need to be treated as uninitialized memory, see
                     * https://stackoverflow.com/questions/55047978/dalvik-verifier-copy1-v16-v22-type-2-cat-1.
                     *
                     * Found in APK bbc.mobile.news.ww,
                     * in class
                     * com/google/android/exoplayer/extractor/mp4/FragmentedMp4Extractor.smali
                     * in method
                     * read(Lcom/google/android/exoplayer/extractor/ExtractorInput;
                     *                                      Lcom/google/android/exoplayer/extractor/PositionHolder;)I
                     *
                     * A conflicted type must be written to, before it can be read from. So we just set it to the dummy
                     * value 0. Then we can move from the register (which requires reading from the register).
                     *
                     * We can set the value to 0 regardless of what the possible type of the value is because all-zeros
                     * is a valid bit representation for any type:
                     *     1) For a object reference we have (Object) null == (int) 0.
                     *     2) For a char we have (char) 0 == '\0'.
                     *     3) For any integer type (that is byte, short, int and long) we have (double) 0 == (int) 0.
                     *     4) For floating point type (that is float, double) we have (float) -0 == (int) 0.
                     *     5) For boolean we have (boolean) false == (int) 0.
                     *
                     *  Assuming the bytecode was valid before the instrumentation we know that the dummy value will
                     *  only be read by the move instruction added below. The dummy value can not be read by any other
                     *  code, because
                     *      1) We do not add any other instrumentation code which reads the dummy value, besides the one
                     *             move operation below.
                     *      2) Any instruction that we did not add still assumes that the type of the register is
                     *             conflicted which means the register has to be written to (thus overwriting our 0)
                     *             before it can be read from.
                     */
                    LOGGER.info("Conflicted type: " + sourceRegisters.get(index));
                    final BuilderInstruction11n constZero
                            = new BuilderInstruction11n(Opcode.CONST_4, sourceRegisters.get(index), 0);
                    mutableMethodImplementation.addInstruction(pos, constZero);
                    pos++;
                }

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
