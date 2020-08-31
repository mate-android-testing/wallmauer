package de.uni_passau.fim.auermich.branchdistance.branch;

import org.jf.dexlib2.analysis.AnalyzedInstruction;

public abstract class Branch implements Comparable<Branch> {

    private AnalyzedInstruction instruction;

    public Branch(AnalyzedInstruction instruction) {
        this.instruction = instruction;
    }

    public int getIndex() {
        return instruction.getInstructionIndex();
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

        return this.instruction.getInstructionIndex()
                == o.instruction.getInstructionIndex();
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
        return instruction.getInstructionIndex();
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
        return Integer.compare(this.instruction.getInstructionIndex(),
                other.instruction.getInstructionIndex());
    }

    @Override
    public String toString() {
        return "Branch Position: " + this.instruction.getInstructionIndex();
    }

}
