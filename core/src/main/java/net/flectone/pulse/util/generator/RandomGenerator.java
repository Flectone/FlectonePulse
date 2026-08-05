package net.flectone.pulse.util.generator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Random number generator
 *
 * @author TheFaser
 * @since 0.1.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RandomGenerator {

    /**
     * Returns a random integer between the specified start (inclusive) and end (exclusive).
     *
     * @param start the lower bound (inclusive)
     * @param end the upper bound (exclusive)
     * @return a random integer in range [start, end), or 0 if start > end
     */
    public int nextInt(int start, int end) {
        if (start > end) return 0;
        return start == end ? start : start + ThreadLocalRandom.current().nextInt(end - start);
    }

    /**
     * Returns a random integer between 0 (inclusive) and the specified bound (exclusive).
     *
     * @param bound the upper bound (exclusive)
     * @return a random integer in range [0, bound)
     */
    public int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    /**
     * Returns a random integer across the entire int range, including negative values.
     *
     * @return a random integer
     */
    public int nextInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    /**
     * Returns a random double between the specified start (inclusive) and end (exclusive).
     *
     * @param start the lower bound (inclusive)
     * @param end the upper bound (exclusive)
     * @return a random double in range [start, end), or 0 if start > end
     */
    public double nextDouble(double start, double end) {
        if (start > end) return 0;
        return start == end ? start : ThreadLocalRandom.current().nextDouble(start, end);
    }

    /**
     * Returns a random double between 0.0 (inclusive) and the specified bound (exclusive).
     *
     * @param bound the upper bound (exclusive)
     * @return a random double in range [0, bound)
     */
    public double nextDouble(double bound) {
        return ThreadLocalRandom.current().nextDouble(bound);
    }

    /**
     * Returns a random double between 0.0 (inclusive) and 1.0 (exclusive).
     *
     * @return a random double in range [0, 1)
     */
    public double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

}