package de.uni_passau.fim.auermich.instrumentation.branchdistance.core;

import org.jf.dexlib2.builder.BuilderInstruction;
import java.util.Objects;

/**
 * Defines an instrumentation point, i.e. a location within a method.
 */
public final class InstrumentationPoint implements Comparable<InstrumentationPoint> {

    private BuilderInstruction instruction;

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
            * This can happen when a branch starts with an if stmt.
            * We need to ensure that an if stmt comes before the branch instrumentation point.
             */
            if (this.getType() == Type.IF_STMT) {
                return -1;
            } else if (other.getType() == Type.IF_STMT) {
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
        IF_STMT;
    }
}
