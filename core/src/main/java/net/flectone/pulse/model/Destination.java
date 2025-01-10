package net.flectone.pulse.model;

import lombok.Getter;

@Getter
public class Destination {

    private final Type type;

    private BossBar bossBar = new BossBar(100, 1f,
            net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS,
            net.kyori.adventure.bossbar.BossBar.Color.BLUE
    );

    // TITLE, SUBTITLE
    private Title.Times times = Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1));

    public Destination() {
        this(Type.CHAT);
    }

    public Destination(Type type) {
        this.type = type;
    }

    public Destination(Type type,
                       long duration,
                       float health,
                       BossBar.Overlay overlay,
                       BossBar.Color color,
                       boolean playBossMusic,
                       boolean createWorldFog,
                       boolean darkenScreen) {
        this(type);
        this.duration = duration;
        this.health = health;
        this.color = color;
        this.overlay = overlay;

        if (playBossMusic) {
            flags.add(BossBar.Flag.PLAY_BOSS_MUSIC);
        }

        if (createWorldFog) {
            flags.add(BossBar.Flag.CREATE_WORLD_FOG);
        }

        if (darkenScreen) {
            flags.add(BossBar.Flag.DARKEN_SCREEN);
        }
    }

    public Destination(Type type, Title.Times times) {
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
