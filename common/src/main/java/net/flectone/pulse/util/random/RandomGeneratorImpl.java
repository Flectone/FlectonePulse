package net.flectone.pulse.util.random;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RandomGeneratorImpl implements RandomGenerator {

    @Override
    public int nextInt(int start, int end) {
        if (start > end) return 0;
        return start == end ? start : start + ThreadLocalRandom.current().nextInt(end - start);
    }

    @Override
    public int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    @Override
    public int nextInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    @Override
    public double nextDouble(double start, double end) {
        if (start > end) return 0;
        return start == end ? start : ThreadLocalRandom.current().nextDouble(start, end);
    }

    @Override
    public double nextDouble(double bound) {
        return ThreadLocalRandom.current().nextDouble(bound);
    }

    @Override
    public double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

}
