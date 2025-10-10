package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TpsTracker {

    private static final int TICK_HISTORY = 20;
    private static final int MAX_SAMPLES = 2;

    private final long[] tickTimes = new long[TICK_HISTORY];
    private final double[] tpsSamples = new double[MAX_SAMPLES];

    private int tickCount = 0;
    private int sampleCount = 0;

    public void onTick() {
        long now = System.nanoTime() / 1000000;
        tickTimes[tickCount % TICK_HISTORY] = now;
        tickCount++;

        if (tickCount % TICK_HISTORY == 0) {
            updateTpsSample();
        }
    }

    private void updateTpsSample() {
        if (tickCount < TICK_HISTORY) {
            tpsSamples[sampleCount % MAX_SAMPLES] = 20.0;
            sampleCount++;
            return;
        }

        int oldestIndex = (tickCount - TICK_HISTORY) % TICK_HISTORY;
        int newestIndex = (tickCount - 1) % TICK_HISTORY;

        long oldestTick = tickTimes[oldestIndex];
        long newestTick = tickTimes[newestIndex];

        long elapsed = newestTick - oldestTick;
        double tps = (elapsed <= 0) ? 20.0 : Math.min((TICK_HISTORY * 1000.0) / elapsed, 20.0);

        tpsSamples[sampleCount % MAX_SAMPLES] = tps;
        sampleCount++;
    }

    public double getTPS() {
        if (sampleCount == 0) {
            return 20.0;
        }

        int samplesToAverage = Math.min(sampleCount, MAX_SAMPLES);
        double sum = 0.0;
        for (int i = 0; i < samplesToAverage; i++) {
            sum += tpsSamples[(sampleCount - 1 - i) % MAX_SAMPLES];
        }

        return sum / samplesToAverage;
    }

}