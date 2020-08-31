package de.uni_passau.fim.auermich.branchdistance.branch;

import org.jf.dexlib2.analysis.AnalyzedInstruction;

public class ElseBranch extends Branch {

    public ElseBranch(AnalyzedInstruction instruction, String id) {
        super(instruction);
    }

    @Override
    public String toString() {
        return "ELSE-Branch Position: " + getIndex();
    }

}
