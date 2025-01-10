package net.flectone.pulse.model;

import lombok.Getter;

@Getter
public class Destination {

    private final Type type;

    private BossBar bossBar = new BossBar(100, 1f,
            net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS,
            net.kyori.adventure.bossbar.BossBar.Color.BLUE
    );

    private Times times = new Times(20, 100, 20);

    public Destination() {
        this(Type.CHAT);
    }

    public Destination(Type type) {
        this.type = type;
    }

    public Destination(Type type, BossBar bossBar) {
        this(type);
        this.bossBar = bossBar;
    }

    public Destination(Type type, Times times) {
        this(type);
        this.times = times;
    }

    public enum Type {
        ACTION_BAR,
        BOSS_BAR,
        BRAND,
        CHAT,
        TITLE,
        SUBTITLE,
        TAB_HEADER,
        TAB_FOOTER
    }
}
