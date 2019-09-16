package de.uni_passau.fim.auermich.branchdistance.branch;

import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.Label;

import java.util.Set;

public class ElseBranch extends Branch {

    public ElseBranch(BuilderInstruction instruction, String id) {
        super(instruction, id);
    }

    @Override
    public String toString() {
        return "ELSE-Branch Position: " + getIndex();
    }

}
