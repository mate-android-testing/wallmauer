package de.uni_passau.fim.auermich.instrumentation.branchdistance.core;

import com.android.tools.smali.dexlib2.builder.BuilderInstruction;

import java.util.Objects;

/**
 * Defines an instrumentation point, i.e. a location within a method.
 */
public final class InstrumentationPoint implements Comparable<InstrumentationPoint> {

    /**
     * The instruction representing the instrumentation point.
     */
    private BuilderInstruction instruction;

    /**
     * The pseudo payload instruction; only set for switch instructions.
     */
    private BuilderInstruction payloadInstruction;

    /**
     * Whether the switch case payload contains a default branch; only set for switch instructions. By default, the
     * payload instruction doesn't list the default branch, unless a packed-switch instruction contains pseudo cases.
     * In particular, the default branch is always the direct successor of the switch instruction.
     */
    private boolean containsDefaultBranch = false;

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
     * Defines a new instrumentation point for the given instruction.
     *
     * @param instruction The instruction representing the instrumentation point.
     * @param payloadInstruction The pseudo payload instruction; only set for switch instructions.
     * @param type The statement type.
     */
    public InstrumentationPoint(BuilderInstruction instruction, BuilderInstruction payloadInstruction, Type type) {
        this.instruction = instruction;
        this.payloadInstruction = payloadInstruction;
        this.position = instruction.getLocation().getIndex();
        this.type = type;
        this.attachedToLabel = instruction.getLocation().getLabels().size() > 0;
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

        // Whenever an else branch starts with a branching statement, we end up with similar positions between a
        // basicBlock and a branching instrumentation point. In these scenarios, we put the basicBlock statement
        // before the branching instrumentation.
        if (comparePosition == 0) {
            if (this.getType() == Type.IF_STMT || this.getType() == Type.SWITCH_STMT) {
                return +1;
            } else if (other.getType() == Type.IF_STMT || other.getType() == Type.SWITCH_STMT) {
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
        NO_BRANCH,
        SWITCH_STMT;

        public boolean isBranchType() {
            return this == IS_BRANCH;
        }
    }

    /**
     * Returns the pseudo payload instruction. Only set for switch instructions.
     *
     * @return Returns the pseudo payload instruction.
     */
    public BuilderInstruction getPayloadInstruction() {
        return payloadInstruction;
    }

    /**
     * Whether the switch payload instruction explicitly lists the default branch. Only set if the instrumentation point
     * refers to a switch statement.
     *
     * @return Returns {@code true} if the switch payload instruction contains a default branch, otherwise {@code false}.
     */
    public boolean containsDefaultBranch() {
        assert this.type == Type.SWITCH_STMT;
        return containsDefaultBranch;
    }

    /**
     * Acknowledges that the switch payload instruction contains an explicit default branch.
     */
    public void setContainsDefaultBranch() {
        assert this.type == Type.SWITCH_STMT;
        this.containsDefaultBranch = true;
    }
}
