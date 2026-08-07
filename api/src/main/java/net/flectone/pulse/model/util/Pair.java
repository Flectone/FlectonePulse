package net.flectone.pulse.model.util;

/**
 * An immutable pair of values.
 *
 * @param left the first value
 * @param right the second value
 * @param <L> type of the first value
 * @param <R> type of the second value
 */
public record Pair<L, R>(L left, R right) {

    /**
     * Creates a pair.
     *
     * @param left the first value
     * @param right the second value
     * @param <L> type of the first value
     * @param <R> type of the second value
     * @return the new pair
     */
    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    /**
     * @return the first value
     */
    public L getLeft() {
        return left;
    }

    /**
     * @return the second value
     */
    public R getRight() {
        return right;
    }

    /**
     * @return the first value
     */
    public L getKey() {
        return left;
    }

    /**
     * @return the second value
     */
    public R getValue() {
        return right;
    }

}
