package de.uni_passau.fim.auermich.instrumentation.branchdistance.dto;

import com.android.tools.smali.dexlib2.analysis.*;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.DexFile;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.iface.MethodImplementation;
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod;
import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.core.InstrumentationPoint;
import de.uni_passau.fim.auermich.instrumentation.branchdistance.utility.Range;

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
    private Method method;

    private final int initialInstructionCount;

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

    // list of analyzed instructions so far
    private List<AnalyzedInstruction> analyzedInstructions;

    // contains the locations where we need to instrument, i.e. at every basic block
    private Set<InstrumentationPoint> basicBlockInstrumentationPoints;

    // describes the ranges of try blocks
    private Set<Range> tryBlocks = new TreeSet<>();

    // track the location of the if and switch instructions
    private Set<InstrumentationPoint> ifAndSwitchInstrumentationPoints;

    public MethodInformation(String methodID, ClassDef classDef, Method method, DexFile dexFile) {
        this.methodID = methodID;
        this.classDef = classDef;
        this.method = method;
        this.methodImplementation = method.getImplementation();
        this.dexFile = dexFile;
        this.initialInstructionCount = getInstructions().size();
        this.basicBlockInstrumentationPoints = new TreeSet<>();
        this.ifAndSwitchInstrumentationPoints = new TreeSet<>();

        if (methodImplementation != null) {
            MethodAnalyzer analyzer = new MethodAnalyzer(new ClassPath(Lists.newArrayList(new DexClassProvider(dexFile)),
                    true, ClassPath.NOT_ART), method,
                    null, false);
            this.analyzedInstructions = analyzer.getAnalyzedInstructions();
        }

    }

    public int getInitialInstructionCount() {
        return initialInstructionCount;
    }

    public AnalyzedInstruction getInstructionAtIndex(int index) {
        return analyzedInstructions.get(index);
    }

    public List<AnalyzedInstruction> getInstructions() {
        if(method.getImplementation() != null) {
            MethodAnalyzer analyzer = new MethodAnalyzer(new ClassPath(Lists.newArrayList(new DexClassProvider(dexFile)),
                    true, ClassPath.NOT_ART), method,
                    null, false);

            return analyzer.getAnalyzedInstructions();
        } else {
            return new ArrayList<>(0);
        }
    }

    public Set<Range> getTryBlocks() {
        return tryBlocks;
    }

    public void setTryBlocks(Set<Range> tryBlocks) {
        this.tryBlocks = tryBlocks;
    }

    public Set<InstrumentationPoint> getBasicBlockInstrumentationPoints() {
        return basicBlockInstrumentationPoints;
    }

    public void setBasicBlockInstrumentationPoints(Set<InstrumentationPoint> basicBlockInstrumentationPoints) {
        this.basicBlockInstrumentationPoints = basicBlockInstrumentationPoints;
    }

    public void setIfAndSwitchInstrumentationPoints(Set<InstrumentationPoint> ifAndSwitchInstrumentationPoints) {
        this.ifAndSwitchInstrumentationPoints = ifAndSwitchInstrumentationPoints;
    }

    public Set<InstrumentationPoint> getIfAndSwitchInstrumentationPoints() {
        return ifAndSwitchInstrumentationPoints;
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

    public void setFreeRegisters(List<Integer> freeRegisters) {
        this.freeRegisters = freeRegisters;
    }

    public List<Integer> getNewRegisters() {
        return newRegisters;
    }

    public void setNewRegisters(List<Integer> newRegisters) {
        this.newRegisters = newRegisters;
    }

    public MethodImplementation getMethodImplementation() {
        return methodImplementation;
    }

    public void setMethodImplementation(MethodImplementation methodImplementation) {
        this.methodImplementation = methodImplementation;
        // whenever the method implementation changes also the method object has to be updated
        updateMethod(methodImplementation);
    }

    private void updateMethod(MethodImplementation methodImplementation) {
        this.method = new ImmutableMethod(
                method.getDefiningClass(),
                method.getName(),
                method.getParameters(),
                method.getReturnType(),
                method.getAccessFlags(),
                method.getAnnotations(),
                null,
                methodImplementation);
    }

    public int getTotalRegisterCount() {
        return totalRegisterCount;
    }

    public void setTotalRegisterCount(int totalRegisters) {
        this.totalRegisterCount = totalRegisters;
    }

    public int getLocalRegisterCount() {
        return localRegisterCount;
    }

    public void setLocalRegisterCount(int localRegisters) {
        this.localRegisterCount = localRegisters;
    }

    public int getParamRegisterCount() {
        return paramRegisterCount;
    }

    public void setParamRegisterCount(int paramRegisters) {
        this.paramRegisterCount = paramRegisters;
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

    public void setParamRegisterTypeMap(Optional<Map<Integer, RegisterType>> paramRegisterTypeMap) {
        this.paramRegisterTypeMap = paramRegisterTypeMap;
    }

    public int getNumberOfBranches() {
        return (int) basicBlockInstrumentationPoints.stream().filter(InstrumentationPoint::hasBranchType).count();
    }
}
