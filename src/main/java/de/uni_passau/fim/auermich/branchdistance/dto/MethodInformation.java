package de.uni_passau.fim.auermich.branchdistance.dto;

import de.uni_passau.fim.auermich.branchdistance.branch.Branch;
import org.jf.dexlib2.analysis.RegisterType;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;

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

    // a list of free/usable register IDs
    private List<Integer> freeRegisters;

    // a list of the additional register IDs
    private List<Integer> newRegisters;

    // a set of branches included in the method
    private Set<Branch> branches;

    // the total register count
    private int totalRegisterCount;

    // the number of local registers (v0...vN)
    private int localRegisterCount;

    // the number of param registers (p0...pN)
    private int paramRegisterCount;

    // the register IDs of the param registers (might be empty)
    private List<Integer> paramRegisters = new ArrayList<>();

    // map of param register IDs and its register type if present
    private Optional<Map<Integer,RegisterType>> paramRegisterTypeMap = Optional.empty();

    // a reference to the (immutable) method implementation
    private MethodImplementation methodImplementation;

    // the instruction IDs of entry/beginning instructions
    private List<Integer> entryInstructionIDs = new ArrayList<>();

    public MethodInformation(String methodID, ClassDef classDef, Method method) {
        this.methodID = methodID;
        this.classDef = classDef;
        this.method = method;
        methodImplementation = method.getImplementation();
    }

    public List<Integer> getEntryInstructionIDs() {
        return entryInstructionIDs;
    }

    public void setEntryInstructionIDs(List<Integer> entryInstructionIDs) {
        this.entryInstructionIDs = entryInstructionIDs;
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

    public Set<Branch> getBranches() {
        return branches;
    }

    public void setFreeRegisters(List<Integer> freeRegisters) {
        this.freeRegisters = freeRegisters;
    }

    public void setNewRegisters(List<Integer> newRegisters) {
        this.newRegisters = newRegisters;
    }

    public void setBranches(Set<Branch> branches) {
        this.branches = branches;
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
}
