package net.flectone.pulse.model.util;

import lombok.Getter;

import java.util.EnumSet;

@Getter
public class BossBar {

    private final EnumSet<net.kyori.adventure.bossbar.BossBar.Flag> flags = EnumSet.noneOf(net.kyori.adventure.bossbar.BossBar.Flag.class);

    private final long duration;
    private final float health;

    private final net.kyori.adventure.bossbar.BossBar.Overlay overlay;
    private final net.kyori.adventure.bossbar.BossBar.Color color;

    public BossBar(long duration,
                   float health,
                   net.kyori.adventure.bossbar.BossBar.Overlay overlay,
                   net.kyori.adventure.bossbar.BossBar.Color color) {
        this.duration = duration;
        this.health = health;
        this.color = color;
        this.overlay = overlay;
    }

    public void addFlag(net.kyori.adventure.bossbar.BossBar.Flag flag) {
        flags.add(flag);
    }
}
