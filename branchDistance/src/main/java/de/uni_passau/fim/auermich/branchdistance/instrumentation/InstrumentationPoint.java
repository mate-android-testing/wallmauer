package de.uni_passau.fim.auermich.branchdistance.instrumentation;

import org.jf.dexlib2.builder.BuilderInstruction;

import java.util.Objects;

/**
 * Defines an instrumentation point, i.e. a location within a method.
 */
public final class InstrumentationPoint implements Comparable<InstrumentationPoint> {

    private final BuilderInstruction instruction;
    private final int position;
    private final Type type;

    public InstrumentationPoint(BuilderInstruction instruction, Type type) {
        this.instruction = instruction;
        this.position = instruction.getLocation().getIndex();
        this.type = type;
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
        IF_STMT;
    }
}
