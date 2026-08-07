package net.flectone.pulse.util.generator;

/**
 * Random number generator
 *
 * @author TheFaser
 * @since 0.1.0
 */
public interface RandomGenerator {

    /**
     * Returns a random integer between the specified start (inclusive) and end (exclusive).
     *
     * @param start the lower bound (inclusive)
     * @param end the upper bound (exclusive)
     * @return a random integer in range [start, end), or 0 if start > end
     */
    int nextInt(int start, int end);

    /**
     * Returns a random integer between 0 (inclusive) and the specified bound (exclusive).
     *
     * @param bound the upper bound (exclusive)
     * @return a random integer in range [0, bound)
     */
    int nextInt(int bound);

    /**
     * Returns a random integer across the entire int range, including negative values.
     *
     * @return a random integer
     */
    int nextInt();

    /**
     * Returns a random double between the specified start (inclusive) and end (exclusive).
     *
     * @param start the lower bound (inclusive)
     * @param end the upper bound (exclusive)
     * @return a random double in range [start, end), or 0 if start > end
     */
    double nextDouble(double start, double end);

    /**
     * Returns a random double between 0.0 (inclusive) and the specified bound (exclusive).
     *
     * @param bound the upper bound (exclusive)
     * @return a random double in range [0, bound)
     */
    double nextDouble(double bound);

    /**
     * Returns a random double between 0.0 (inclusive) and 1.0 (exclusive).
     *
     * @return a random double in range [0, 1)
     */
    double nextDouble();

}
