package net.flectone.pulse.pipeline.fast;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.jspecify.annotations.NonNull;

final class RainbowColorizer implements Colorizer {

    private static final int CACHE_BITS = 8;
    private static final int CACHE_SIZE = 1 << CACHE_BITS;
    private static final int CACHE_MASK = CACHE_SIZE - 1;
    private static final int PRECISION = 16;

    private static final int[] COLOR_CACHE = new int[CACHE_SIZE];

    static {
        for (int i = 0; i < CACHE_SIZE; i++) {
            HSVLike hsv = HSVLike.hsvLike((float) i / CACHE_SIZE, 1f, 1f);
            COLOR_CACHE[i] = TextColor.color(hsv).value();
        }
    }

    private final boolean reversed;
    private final int phaseOffset;

    private int currentPos;
    private int step;

    RainbowColorizer(boolean reversed, int phase) {
        this.reversed = reversed;
        this.phaseOffset = ((int) ((phase / 10.0) % 1.0 * CACHE_SIZE)) << PRECISION;
    }

    @Override
    public void init(int size) {
        if (size <= 0) {
            step = 0;
            currentPos = phaseOffset;
            return;
        }

        int totalDist = CACHE_SIZE << PRECISION;
        step = totalDist / size;

        if (reversed) {
            step = -step;
            currentPos = phaseOffset + (totalDist + step);
        } else {
            currentPos = phaseOffset;
        }
    }

    @Override
    public @NonNull TextColor color() {
        int cacheIndex = (currentPos >> PRECISION) & CACHE_MASK;
        return TextColor.color(COLOR_CACHE[cacheIndex]);
    }

    @Override
    public void advance() {
        currentPos += step;
    }

}
