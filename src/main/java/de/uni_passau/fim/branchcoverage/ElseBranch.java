package de.uni_passau.fim.branchcoverage;

import de.uni_passau.fim.branchcoverage.Branch;
import org.jf.dexlib2.builder.Label;

import java.util.Set;

public class ElseBranch extends Branch {

    private Set<Label> labels;

    public ElseBranch(int index, int codeAddress, Set<Label> labels) {
        super(index, codeAddress);
        this.labels = labels;
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

}
