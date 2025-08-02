package net.flectone.pulse.model.util;

import lombok.Getter;

@Getter
public class Destination {

    private final Type type;
    private final String subtext;

    private BossBar bossBar = new BossBar(100, 1f,
            net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS,
            net.kyori.adventure.bossbar.BossBar.Color.BLUE
    );

    private Times times = new Times(20, 60, 20);
    private Toast toast = new Toast("minecraft:diamond", Toast.Type.TASK);

    public Destination() {
        this(Type.CHAT, "");
    }

    public Destination(Type type) {
        this(type, "");
    }

    public Destination(Type type, String subtext) {
        this.type = type;
        this.subtext = subtext;
    }

    public Destination(Type type, BossBar bossBar) {
        this(type, "");
        this.bossBar = bossBar;
    }

    public Destination(Type type, Times times) {
        this(type, "");
        this.times = times;
    }

    public Destination(Type type, Times times, String subtext) {
        this(type, subtext);
        this.times = times;
    }

    public Destination(Type type, Toast toast) {
        this(type, "");
        this.toast = toast;
    }

    public enum Type {
        ACTION_BAR,
        BOSS_BAR,
        BRAND,
        CHAT,
        TITLE,
        SUBTITLE,
        TAB_HEADER,
        TAB_FOOTER,
        TOAST
    }
}
