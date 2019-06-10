package de.uni_passau.fim.branchcoverage;

import org.jf.dexlib2.builder.Label;

import java.util.Set;

public class ElseBranch extends Branch {

    private Set<Label> labels;

    public ElseBranch(int index, int codeAddress, Set<Label> labels, String id) {
        super(index, codeAddress,id);
        this.labels = labels;
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "ELSE-Branch Position: " + getIndex();
    }

}
