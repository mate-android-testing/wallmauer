package de.uni_passau.fim.auermich.basicBlockCoverage.branch;


public class IfBranch extends Branch {

    public IfBranch(int index, int codeAddress, String id) {
        super(index, codeAddress,id);
    }

    @Override
    public String toString() {
        return "IF-Branch Position: " + getIndex();
    }

}
