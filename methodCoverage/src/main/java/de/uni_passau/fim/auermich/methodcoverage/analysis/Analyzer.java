package de.uni_passau.fim.auermich.methodcoverage.analysis;


import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.methodcoverage.dto.MethodInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.dexlib2.analysis.*;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.util.MethodUtil;

import java.util.*;

public final class Analyzer {

    private static final Logger LOGGER = LogManager.getLogger(Analyzer.class);

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
        // stays unchanged, no additional param register
        methodInformation.setParamRegisterCount(paramRegisters);
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
