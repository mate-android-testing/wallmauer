package de.uni_passau.fim.auermich.instrumentation.branchcoverage.dto;

import com.android.tools.smali.dexlib2.analysis.*;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.DexFile;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.iface.MethodImplementation;
import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.instrumentation.branchcoverage.core.InstrumentationPoint;
import de.uni_passau.fim.auermich.instrumentation.branchcoverage.utility.Range;

import java.util.*;

/**
 * Stores all the relevant information
 * for a method.
 */
public class MethodInformation {

    // a method descriptor (id)
    private final String methodID;

    // a reference to the class object
    private final ClassDef classDef;

    // a reference to the actual method
    private final Method method;

    // a reference to the dex file
    private final DexFile dexFile;

    // a list of free/usable register IDs
    private List<Integer> freeRegisters;

    // a list of the additional register IDs
    private List<Integer> newRegisters;

    // the total register count
    private int totalRegisterCount;

    // the number of local registers (v0...vN)
    private int localRegisterCount;

    // the number of param registers (p0...pN)
    private int paramRegisterCount;

    // the register IDs of the param registers (might be empty)
    private List<Integer> paramRegisters = new ArrayList<>();

    // map of param register IDs and its register type if present
    private Optional<Map<Integer, RegisterType>> paramRegisterTypeMap = Optional.empty();

    // a reference to the (immutable) method implementation
    private MethodImplementation methodImplementation;

    // contains the locations where we need to instrument
    private Set<InstrumentationPoint> instrumentationPoints;

    // describes the ranges of try blocks
    private Set<Range> tryBlocks = new TreeSet<>();

    public MethodInformation(String methodID, ClassDef classDef, Method method, DexFile dexFile) {
        this.methodID = methodID;
        this.classDef = classDef;
        this.method = method;
        methodImplementation = method.getImplementation();
        this.dexFile = dexFile;
    }

    public AnalyzedInstruction getInstructionAtIndex(int index) {

        MethodAnalyzer analyzer = new MethodAnalyzer(new ClassPath(Lists.newArrayList(new DexClassProvider(dexFile)),
                true, ClassPath.NOT_ART), method,
                null, false);

        return analyzer.getAnalyzedInstructions().get(index);
    }

    public List<AnalyzedInstruction> getInstructions() {

        MethodAnalyzer analyzer = new MethodAnalyzer(new ClassPath(Lists.newArrayList(new DexClassProvider(dexFile)),
                true, ClassPath.NOT_ART), method,
                null, false);

        return analyzer.getAnalyzedInstructions();
    }

    public Set<Range> getTryBlocks() {
        return tryBlocks;
    }

    public void setTryBlocks(Set<Range> tryBlocks) {
        this.tryBlocks = tryBlocks;
    }

    public Set<InstrumentationPoint> getInstrumentationPoints() {
        return instrumentationPoints;
    }

    public void setInstrumentationPoints(Set<InstrumentationPoint> instrumentationPoints) {
        this.instrumentationPoints = instrumentationPoints;
    }

    public String getMethodID() {
        return methodID;
    }

    public ClassDef getClassDef() {
        return classDef;
    }

    public Method getMethod() {
        return method;
    }

    public List<Integer> getFreeRegisters() {
        return freeRegisters;
    }

    public List<Integer> getNewRegisters() {
        return newRegisters;
    }

    public MethodImplementation getMethodImplementation() {
        return methodImplementation;
    }

    public void setMethodImplementation(MethodImplementation methodImplementation) {
        this.methodImplementation = methodImplementation;
    }

    public int getTotalRegisterCount() {
        return totalRegisterCount;
    }

    public int getLocalRegisterCount() {
        return localRegisterCount;
    }

    public int getParamRegisterCount() {
        return paramRegisterCount;
    }

    public void setFreeRegisters(List<Integer> freeRegisters) {
        this.freeRegisters = freeRegisters;
    }

    public void setNewRegisters(List<Integer> newRegisters) {
        this.newRegisters = newRegisters;
    }

    public void setTotalRegisterCount(int totalRegisters) {
        this.totalRegisterCount= totalRegisters;
    }

    public void setLocalRegisterCount(int localRegisters) {
        this.localRegisterCount = localRegisters;
    }

    public void setParamRegisterCount(int paramRegisters) {
        this.paramRegisterCount = paramRegisters;
    }

    public void setParamRegisterTypeMap(Optional<Map<Integer, RegisterType>> paramRegisterTypeMap) {
        this.paramRegisterTypeMap = paramRegisterTypeMap;
    }

    public List<Integer> getParamRegisters() {
        return paramRegisters;
    }

    public void setParamRegisters(List<Integer> paramRegisters) {
        this.paramRegisters = paramRegisters;
    }

    public Optional<Map<Integer, RegisterType>> getParamRegisterTypeMap() {
        return paramRegisterTypeMap;
    }

    public int getNumberOfBranches() {
        // each instrumentation point represents a branch
        return instrumentationPoints != null ? instrumentationPoints.size() : 0;
    }
}
