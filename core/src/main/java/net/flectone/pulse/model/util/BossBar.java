package net.flectone.pulse.model.util;

import java.util.EnumSet;

public record BossBar(
        long duration,
        float health,
        net.kyori.adventure.bossbar.BossBar.Overlay overlay,
        net.kyori.adventure.bossbar.BossBar.Color color,
        EnumSet<net.kyori.adventure.bossbar.BossBar.Flag> flags
) {

    public BossBar(long duration,
                   float health,
                   net.kyori.adventure.bossbar.BossBar.Overlay overlay,
                   net.kyori.adventure.bossbar.BossBar.Color color) {
        this(duration, health, overlay, color, EnumSet.noneOf(net.kyori.adventure.bossbar.BossBar.Flag.class));
    }

    public BossBar withFlag(net.kyori.adventure.bossbar.BossBar.Flag flag) {
        EnumSet<net.kyori.adventure.bossbar.BossBar.Flag> newFlags = EnumSet.copyOf(this.flags);
        newFlags.add(flag);
        return new BossBar(duration, health, overlay, color, newFlags);
    }

}