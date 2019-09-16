package de.uni_passau.fim.auermich.branchdistance.branch;


import org.jf.dexlib2.builder.BuilderInstruction;

public class IfBranch extends Branch {

    public IfBranch(BuilderInstruction instruction, String id) {
        super(instruction, id);
    }

    @Override
    public String toString() {
        return "IF-Branch Position: " + getIndex();
    }

}
