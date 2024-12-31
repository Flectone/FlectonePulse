package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Random;

@Singleton
public class RandomUtil {

    private final Random RANDOM = new Random();

    @Inject
    public RandomUtil() {}

    public int nextInt(int start, int end) {
        if (start > end) return 0;
        return start == end ? start : start + RANDOM.nextInt(end - start);
    }

    public int nextInt(int bound) {
        return RANDOM.nextInt(bound);
    }

}
