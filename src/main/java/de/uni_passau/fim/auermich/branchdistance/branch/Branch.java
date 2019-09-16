package de.uni_passau.fim.auermich.branchdistance.branch;

import org.jf.dexlib2.builder.BuilderInstruction;

public abstract class Branch implements Comparable<Branch> {

    private BuilderInstruction instruction;
    private String id;

    public Branch(BuilderInstruction instruction, String id) {
        this.instruction = instruction;
        this.id = id;
    }

    public int getIndex() {
        return instruction.getLocation().getIndex();
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

        return this.instruction.getLocation().getIndex()
                == o.instruction.getLocation().getIndex();
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
        return instruction.getLocation().getIndex();
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
        return Integer.compare(this.instruction.getLocation().getIndex(),
                other.instruction.getLocation().getIndex());
    }

    @Override
    public String toString() {
        return "Branch Position: " + this.instruction.getLocation().getIndex();
    }

}
