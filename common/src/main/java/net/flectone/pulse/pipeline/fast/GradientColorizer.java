package net.flectone.pulse.pipeline.fast;

import net.kyori.adventure.text.format.TextColor;
import org.jspecify.annotations.NonNull;

final class GradientColorizer implements Colorizer {

    private final TextColor[] colors;

    private double multiplier;
    private double phase;
    private int index;

    GradientColorizer(TextColor @NonNull [] colors, double phase) {
        if (phase < 0) {
            this.colors = new TextColor[colors.length];
            for (int i = 0; i < colors.length; i++) {
                this.colors[i] = colors[colors.length - 1 - i];
            }
            this.phase = 1 + phase;
        } else {
            this.colors = colors;
            this.phase = phase;
        }
    }

    @Override
    public void init(int size) {
        multiplier = size == 1 ? 0 : (double) (colors.length - 1) / (size - 1);
        phase *= colors.length - 1;
        index = 0;
    }

    @Override
    public @NonNull TextColor color() {
        double position = (index * multiplier) + phase;
        int lowUnclamped = (int) Math.floor(position);
        int high = (int) Math.ceil(position) % colors.length;
        int low = lowUnclamped % colors.length;

        return TextColor.lerp((float) position - lowUnclamped, colors[low], colors[high]);
    }

    @Override
    public void advance() {
        index++;
    }

}
