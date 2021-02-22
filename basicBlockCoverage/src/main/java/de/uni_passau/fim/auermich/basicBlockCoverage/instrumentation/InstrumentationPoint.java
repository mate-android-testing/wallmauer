package de.uni_passau.fim.auermich.basicBlockCoverage.instrumentation;

import org.jf.dexlib2.builder.BuilderInstruction;

import java.util.Objects;

/**
 * Defines an instrumentation point, i.e. a location within a method.
 */
public final class InstrumentationPoint implements Comparable<InstrumentationPoint> {

    private final BuilderInstruction instruction;
    private final int position;
    private final Type type;

    // defines how many instructions are covered by the basic block
    private final int coveredInstructions;

    public InstrumentationPoint(BuilderInstruction instruction, Type type, int coveredInstructions) {
        this.instruction = instruction;
        this.position = instruction.getLocation().getIndex();
        this.type = type;
        this.coveredInstructions = coveredInstructions;
    }

    public int getCoveredInstructions() {
        return coveredInstructions;
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
        return Integer.compare(this.position, other.position);
    }

    @Override
    public String toString() {
        return "IP: " + position + "(" + type + ")";
    }

    public enum  Type {

        ENTRY_STMT,
        CATCH_BLOCK_WITH_MOVE_EXCEPTION,
        CATCH_BLOCK_WITHOUT_MOVE_EXCEPTION,
        EXCEPTIONAL_SUCCESSOR,
        IF_BRANCH,
        ELSE_BRANCH,
        GOTO_BRANCH;

        public boolean isBranchType() {
            return this == IF_BRANCH || this == ELSE_BRANCH;
        }
    }
}
