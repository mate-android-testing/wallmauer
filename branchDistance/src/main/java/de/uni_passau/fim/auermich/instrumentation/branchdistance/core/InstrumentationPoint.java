package de.uni_passau.fim.auermich.instrumentation.branchdistance.core;

import org.jf.dexlib2.builder.BuilderInstruction;

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

    // original position of instruction
    private final int position;

    private final Type type;
    private final boolean attachedToLabel;

    public InstrumentationPoint(BuilderInstruction instruction, Type type) {
        this.instruction = instruction;
        this.position = instruction.getLocation().getIndex();
        this.type = type;
        this.attachedToLabel = instruction.getLocation().getLabels().size() > 0;
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

    /**
     * Whether a label is attached to the instruction.
     *
     * @return Returns {@code true} if a label is attached to the instruction,
     *          otherwise {@code false} is returned.
     */
    public boolean isAttachedToLabel() {
        return attachedToLabel;
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

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o instanceof InstrumentationPoint) {

            InstrumentationPoint other = (InstrumentationPoint) o;
            // we also need to compare the type since there can be multiple IPs at the same position
            return this.position == other.position && this.type == other.type;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, type);
    }

    /**
     * Defines a natural ordering between the instrumentation points. This is only necessary
     * since the first instruction at a branch could coincide with an if statement. In such a case,
     * we want to have the trace information closer to the statement at the branch. Thus, we need
     * to rank an if statement instrumentation point naturally lower (insert prior to) than the
     * branch statement instrumentation point.
     * NOTE: We don't consider here method entry/exit instrumentation points as we handle them
     * separately from the branch instrumentation points. That means, there is no way that a
     * branch instrumentation point could coincide with a method entry/exit statement.
     *
     * @param other The other instrumentation point to compare against.
     * @return Returns a natural ordering between two instrumentation points.
     */
    @Override
    public int compareTo(InstrumentationPoint other) {
        int comparePosition = Integer.compare(this.position, other.position);

        if (comparePosition == 0) {
            /*
            * This can happen when a branch starts with an if or a switch stmt.
            * We need to ensure that an if or a switch stmt comes before the branch instrumentation point.
             */
            if (this.getType() == Type.IF_STMT || this.getType() == Type.SWITCH_STMT) {
                return -1;
            } else if (other.getType() == Type.IF_STMT || other.getType() == Type.SWITCH_STMT) {
                return +1;
            } else {
                // shared else branch -> duplicate
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
        IF_BRANCH,
        ELSE_BRANCH,
        ENTRY_STMT,
        EXIT_STMT,
        TRY_BLOCK_STMT,
        CATCH_BLOCK_STMT,
        IF_STMT,
        SWITCH_STMT;
    }
}
