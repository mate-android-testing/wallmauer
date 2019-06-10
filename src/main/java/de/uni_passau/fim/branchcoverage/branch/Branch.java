package de.uni_passau.fim.branchcoverage.branch;

public abstract class Branch implements Comparable<Branch> {

    private int index;
    private int codeAddress;
    private String id;

    public Branch(int index, int codeAddress, String id) {
        this.index = index;
        this.codeAddress = codeAddress;
        this.id = id;
    }

    public int getIndex() {
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

    /**
     * Two branches are identical, if they share the
     * same position in the code/method.
     *
     * @param other The branch to compare against.
     * @return Returns {@code true} if the branches are identical,
     *      otherwise {@code false}.
     */
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

    /**
     * If two branches are equal, i.e. equals() returned true,
     * then additionally the hashCode must be identical.
     * That is, the index must be the same.
     *
     * @return Returns the index (position of instruction) as hashCode.
     */
    @Override
    public int hashCode() {
        return index;
    }

    /**
     * Compares two branches by their position (index)
     * in the code.
     *
     * @param other The branch to compare with.
     * @return Returns a value indicating the numerical order.
     */
    @Override
    public int compareTo(Branch other) {
        return Integer.compare(this.index, other.index);
    }

    @Override
    public String toString() {
        return "Branch Position: " + this.index;
    }

}
