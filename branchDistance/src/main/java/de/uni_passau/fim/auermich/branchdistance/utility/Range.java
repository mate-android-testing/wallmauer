package de.uni_passau.fim.auermich.branchdistance.utility;

import java.util.Objects;

/**
 * Describes a range or interval for integer values.
 * The primary usage of this class is to describe the length of a try block.
 */
public class Range implements Comparable<Range> {

    private final int start;
    private final int end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean containsStrict(int value) {
        return start < value && value <= end;
    }

    public boolean contains(int value) {
        return start <= value && value <= end;
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
