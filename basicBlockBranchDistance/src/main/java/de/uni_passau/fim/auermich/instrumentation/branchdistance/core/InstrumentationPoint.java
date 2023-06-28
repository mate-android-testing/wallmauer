package de.uni_passau.fim.auermich.instrumentation.branchdistance.core;

import org.jf.dexlib2.builder.BuilderInstruction;

import java.util.Objects;

/**
 * Defines an instrumentation point, i.e. a location within a method.
 */
public final class InstrumentationPoint implements Comparable<InstrumentationPoint> {

    private BuilderInstruction instruction;

    // the original position in the un-instrumented code
    private final int position;

    private final Type type;
    private final boolean attachedToLabel;
    private int coveredInstructions = -1;

    public InstrumentationPoint(BuilderInstruction instruction, Type type) {
        this.instruction = instruction;
        this.position = instruction.getLocation().getIndex();
        this.type = type;
        this.attachedToLabel = !instruction.getLocation().getLabels().isEmpty();
    }

    /**
     * Whether a label is attached to the instruction.
     *
     * @return Returns {@code true} if a label is attached to the instruction,
     *          otherwise {@code false} is returned.
     */
    public boolean isAttachedToLabel() {
        return attachedToLabel;
    }

    public int getCoveredInstructions() {
        return coveredInstructions;
    }

    public void setCoveredInstructions(int coveredInstructions) {
        this.coveredInstructions = coveredInstructions;
    }

    public int getPosition() {
        return position;
    }

    public Type getType() {
        return type;
    }

    public BuilderInstruction getInstruction() {
        return instruction;
    }

    public void setInstruction(BuilderInstruction instruction) {
        this.instruction = instruction;
    }

    public boolean hasBranchType() {
        return this.type.isBranchType();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o instanceof InstrumentationPoint) {

            InstrumentationPoint other = (InstrumentationPoint) o;
            return this.position == other.position && this.type == other.type;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, type);
    }

    @Override
    public int compareTo(InstrumentationPoint other) {
        int comparePosition = Integer.compare(this.position, other.position);

        // Whenever an else branch starts with an if statement, we end up with similar positions between a
        // basicBlock and a branching instrumentation point. In these scenarios, we put the basicBlock statement
        // before the if statement.
        if (comparePosition == 0) {
            if (this.getType() == Type.IF_STMT) {
                return +1;
            } else if (other.getType() == Type.IF_STMT) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return comparePosition;
        }
    }

    @Override
    public String toString() {
        return "IP: " + position + "(" + type + ")";
    }

    public enum Type {
        IF_STMT,
        IS_BRANCH,
        NO_BRANCH;

        public boolean isBranchType() {
            return this == IS_BRANCH;
        }
    }
}
