package net.flectone.pulse.model.value;

import java.util.EnumSet;

/**
 * A boss bar shown above the hotbar for a limited time.
 *
 * @param duration how long the bar stays on screen, in ticks
 * @param health the filled fraction of the bar, from 0.0 to 1.0
 * @param overlay the segment style drawn over the bar
 * @param color the bar color
 * @param flags extra client effects such as fog or boss music
 * @author TheFaser
 */
public record BossBar(
        long duration,
        float health,
        net.kyori.adventure.bossbar.BossBar.Overlay overlay,
        net.kyori.adventure.bossbar.BossBar.Color color,
        EnumSet<net.kyori.adventure.bossbar.BossBar.Flag> flags
) {

    /**
     * Creates a boss bar with no extra flags.
     *
     * @param duration how long the bar stays on screen, in ticks
     * @param health the filled fraction of the bar, from 0.0 to 1.0
     * @param overlay the segment style drawn over the bar
     * @param color the bar color
     */
    public BossBar(long duration,
                   float health,
                   net.kyori.adventure.bossbar.BossBar.Overlay overlay,
                   net.kyori.adventure.bossbar.BossBar.Color color) {
        this(duration, health, overlay, color, EnumSet.noneOf(net.kyori.adventure.bossbar.BossBar.Flag.class));
    }

    /**
     * Returns a copy of this bar with an extra flag enabled.
     *
     * @param flag the flag to add
     * @return a new boss bar carrying the flag
     */
    public BossBar withFlag(net.kyori.adventure.bossbar.BossBar.Flag flag) {
        EnumSet<net.kyori.adventure.bossbar.BossBar.Flag> newFlags = EnumSet.copyOf(this.flags);
        newFlags.add(flag);
        return new BossBar(duration, health, overlay, color, newFlags);
    }

}