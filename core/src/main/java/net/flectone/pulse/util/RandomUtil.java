package net.flectone.pulse.util;

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
public class RandomUtil {

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

}
