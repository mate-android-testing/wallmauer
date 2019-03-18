package de.uni_passau.fim.branchcoverage;

public abstract class Branch {

    private int index;
    private int codeAddress;

    protected Branch(int index, int codeAddress) {
        this.index = index;
        this.codeAddress = codeAddress;
    }

    protected int getIndex() {
        return index;
    }

    protected void setIndex(int index) {
        this.index = index;
    }

    protected int getCodeAddress() {
        return codeAddress;
    }

    protected void setCodeAddress(int codeAddress) {
        this.codeAddress = codeAddress;
    }

    @Override
    public boolean equals(Object other) {

        if (this == other)
            return true;

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Branch o = (Branch) other;

        return this.index == o.index;
    }

    @Override
    public int hashCode() {
        /*
        * If two branches are equal, i.e. equals() returned true, then additionally
        * the hashCode must be identical. Thus, we simply return 0 to conform
        * with the contract between equals() and hashCode().
        */
        return index;
    }

}
