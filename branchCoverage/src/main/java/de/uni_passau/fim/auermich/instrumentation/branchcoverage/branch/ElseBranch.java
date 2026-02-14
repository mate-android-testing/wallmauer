package de.uni_passau.fim.auermich.instrumentation.branchcoverage.branch;

import com.android.tools.smali.dexlib2.builder.Label;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Set;

public class ElseBranch extends Branch {

    private Set<Label> labels;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
    public ElseBranch(int index, int codeAddress, Set<Label> labels, String id) {
        super(index, codeAddress,id);
        this.labels = labels;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP")
    public Set<Label> getLabels() {
        return labels;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "ELSE-Branch Position: " + getIndex();
    }

}
