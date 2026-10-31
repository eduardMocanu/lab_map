package model;

import java.util.Objects;

/**
 * An immutable pair of two values.
 * Two tuples are equal if both of their values are equal, so a tuple can be used as a map key.
 *
 * @param <A> the type of the first value
 * @param <B> the type of the second value
 */
public class Tuple<A, B> {
    private final A first;
    private final B second;

    /**
     * Creates a pair of two values.
     *
     * @param first  the first value
     * @param second the second value
     */
    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /** @return the first value */
    public A getFirst() {
        return first;
    }

    /** @return the second value */
    public B getSecond() {
        return second;
    }

    /**
     * Compares two tuples value by value.
     * Returns false for null and for objects of another class (no direct cast).
     *
     * @param o the object to compare with
     * @return true if {@code o} is a tuple with equal first and second values
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(first, tuple.first) && Objects.equals(second, tuple.second);
    }

    /**
     * @return a hash code based on both values, consistent with {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    /**
     * @return the pair written as (first, second)
     */
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
