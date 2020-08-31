package de.uni_passau.fim.auermich.branchdistance.branch;


import org.jf.dexlib2.analysis.AnalyzedInstruction;

public class IfBranch extends Branch {

    public IfBranch(AnalyzedInstruction instruction, String id) {
        super(instruction);
    }

    @Override
    public String toString() {
        return "IF-Branch Position: " + getIndex();
    }

}
