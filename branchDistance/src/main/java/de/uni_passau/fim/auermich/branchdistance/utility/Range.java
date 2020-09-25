package de.uni_passau.fim.auermich.branchdistance.utility;

import java.util.Objects;

/**
 * Describes a range or interval for integer values.
 * Note that this class is tailored for the description of try blocks.
 * In particular, the contains() method might not suit all needs.
 */
public class Range implements Comparable<Range> {

    private final int start;
    private final int end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Checks whether a value falls into the given range.
     *
     * Note that is method is tailored for usage with instrumentation points (branches) as input.
     * However, since an instrumentation point (position) refers actually to the first instruction
     * within a branch, we need to use a left-sided strict order here. To illustrate this:
     *
     * L1: if-eqz v0 : cond_0
     *     :try_start_0
     * L2: the first instruction within the branch (instrumentation point)
     *     :try_end_0
     *
     * The try block is described by the range [L2] and the instrumentation point position
     * also refers to L2, although the actual instrumentation happens between L1 and L2. Thus,
     * the strict order (left side) is necessary to avoid false positives.
     *
     * @param value The value to check.
     * @return Returns {@code true} if the value falls into the range, otherwise
     *      {@code false} is returned.
     */
    public boolean contains(int value) {
        return start < value && value <= end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "[" + start + "," + end + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (this.getClass() != o.getClass()) {
            return false;
        }

        Range other = (Range) o;
        return start == other.start && end == other.end;
    }

    @Override
    public int compareTo(Range o) {

        if (start < o.start) {
            return -1;
        } else if (start > o.start) {
            return 1;
        } else {
            return Integer.compare(end, o.end);
        }
    }
}
