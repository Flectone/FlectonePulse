package net.flectone.pulse.file.model;

import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.EnumSet;

@Getter
public class Destination {

    private final Type type;

    // BOOS_BAR
    private final EnumSet<BossBar.Flag> flags = EnumSet.noneOf(BossBar.Flag.class);
    private BossBar.Overlay overlay = BossBar.Overlay.PROGRESS;
    private BossBar.Color color = BossBar.Color.BLUE;
    private long duration = 100;
    private float health = 1f;

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
        SCOREBOARD,
        TITLE,
        SUBTITLE,
        TAB_HEADER,
        TAB_FOOTER
    }
}
