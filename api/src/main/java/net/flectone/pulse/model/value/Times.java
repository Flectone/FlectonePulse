package net.flectone.pulse.model.value;

/**
 * Fade and hold timings for a title, subtitle or action bar.
 *
 * @param fadeInTicks how long the text fades in, in ticks
 * @param stayTicks how long it stays fully visible, in ticks
 * @param fadeOutTicks how long it fades out, in ticks
 * @author TheFaser
 */
public record Times(int fadeInTicks, int stayTicks, int fadeOutTicks) {
}
