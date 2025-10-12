package net.flectone.pulse.model.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class Destination {

    private final Type type;
    private final String subtext;
    private final BossBar bossBar;
    private final Times times;
    private final Toast toast;

    public Destination(Type type, String subtext, BossBar bossBar, Times times, Toast toast) {
        this.type = type != null ? type : Type.CHAT;
        this.subtext = subtext != null ? subtext : "";
        this.bossBar = bossBar != null ? bossBar : new BossBar(100, 1f,
                net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS,
                net.kyori.adventure.bossbar.BossBar.Color.BLUE);
        this.times = times != null ? times : new Times(20, 60, 20);
        this.toast = toast != null ? toast : new Toast("minecraft:diamond", Toast.Type.TASK);
    }

    public Destination() {
        this(null, null, null, null, null);
    }

    public Destination(Type type) {
        this(type, null, null, null, null);
    }

    public Destination(Type type, BossBar bossBar) {
        this(type, null, bossBar, null, null);
    }

    public Destination(Type type, Times times) {
        this(type, null, null, times, null);
    }

    public Destination(Type type, Times times, String subtext) {
        this(type, subtext, null, times, null);
    }

    public Destination(Type type, Toast toast) {
        this(type, null, null, null, toast);
    }

    @JsonValue
    public Map<String, Object> toJson() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", this.type);

        switch (this.type) {
            case TOAST -> {
                Toast toast = this.toast;
                map.put("icon", toast.icon());
                map.put("style", toast.style());
            }
            case TITLE, SUBTITLE, ACTION_BAR -> {
                Times times = this.times;
                Map<String, Object> timesMap = new LinkedHashMap<>();
                timesMap.put("stay", times.stayTicks());

                if (this.type != Type.ACTION_BAR) {
                    timesMap.put("fade-in", times.fadeInTicks());
                    timesMap.put("fade-out", times.fadeOutTicks());

                    map.put("subtext", this.subtext);
                }

                map.put("times", timesMap);
            }
            case BOSS_BAR -> {
                BossBar bossBar = this.bossBar;
                map.put("duration", bossBar.getDuration());
                map.put("health", bossBar.getHealth());
                map.put("overlay", bossBar.getOverlay());
                map.put("color", bossBar.getColor());
                map.put("play_boss_music", bossBar.getFlags().contains(net.kyori.adventure.bossbar.BossBar.Flag.PLAY_BOSS_MUSIC));
                map.put("create_world_fog", bossBar.getFlags().contains(net.kyori.adventure.bossbar.BossBar.Flag.CREATE_WORLD_FOG));
                map.put("darken_screen", bossBar.getFlags().contains(net.kyori.adventure.bossbar.BossBar.Flag.DARKEN_SCREEN));
            }
        }

        return map;
    }

    @JsonCreator
    public static Destination fromJson(Map<String, Object> map) {
        Type type = Type.valueOf(String.valueOf(map.get("type")));

        return switch (type) {
            case TOAST -> {
                Object icon = map.get("icon");
                String stringIcon = icon == null ? "minecraft:diamond" : String.valueOf(icon);

                Object style = map.get("style");
                Toast.Type toastStyle = style == null ? Toast.Type.TASK : Toast.Type.valueOf(String.valueOf(style));

                yield new Destination(Type.TOAST, new Toast(stringIcon, toastStyle));
            }
            case TITLE, SUBTITLE, ACTION_BAR -> {
                Object times = map.get("times");

                if (times == null) {
                    yield new Destination(type);
                }

                Map<String, Object> timesMap = (Map<String, Object>) times;

                Object fadeIn = timesMap.get("fade_in");
                int fadeInTicks = fadeIn == null ? 20 : Integer.parseInt(String.valueOf(fadeIn));

                Object stay = timesMap.get("stay");
                int stayTicks = stay == null ? 60 : Integer.parseInt(String.valueOf(stay));

                Object fadeOut = timesMap.get("fade_out");
                int fadeOutTicks = fadeOut == null ? 20 : Integer.parseInt(String.valueOf(fadeOut));

                Times titleTimes = new Times(fadeInTicks, stayTicks, fadeOutTicks);

                if (type == Type.ACTION_BAR) yield new Destination(type, titleTimes);

                Object subtext = map.get("subtext");
                String stringSubtext = subtext == null ? "" : String.valueOf(subtext);

                yield new Destination(type, titleTimes, stringSubtext);
            }
            case BOSS_BAR -> {
                Object duration = map.get("duration");
                long longDuration = duration == null ? 100 : Long.parseLong(String.valueOf(duration));

                Object health = map.get("health");
                float floatHealth = health == null ? 1f : Float.parseFloat(String.valueOf(health));

                Object overlay = map.get("overlay");
                net.kyori.adventure.bossbar.BossBar.Overlay bossBarOverlay = overlay == null
                        ? net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS
                        : net.kyori.adventure.bossbar.BossBar.Overlay.valueOf(String.valueOf(overlay));

                Object color = map.get("color");
                net.kyori.adventure.bossbar.BossBar.Color bossBarColor = color == null
                        ? net.kyori.adventure.bossbar.BossBar.Color.BLUE
                        : net.kyori.adventure.bossbar.BossBar.Color.valueOf(String.valueOf(color));

                net.flectone.pulse.model.util.BossBar bossBar = new net.flectone.pulse.model.util.BossBar(longDuration, floatHealth, bossBarOverlay, bossBarColor);

                Object playBossMusic = map.get("play-boss-music");
                if (playBossMusic != null && Boolean.parseBoolean(String.valueOf(playBossMusic))) {
                    bossBar.addFlag(net.kyori.adventure.bossbar.BossBar.Flag.PLAY_BOSS_MUSIC);
                }

                Object createWorldFog = map.get("create-world-fog");
                if (createWorldFog != null && Boolean.parseBoolean(String.valueOf(createWorldFog))) {
                    bossBar.addFlag(net.kyori.adventure.bossbar.BossBar.Flag.CREATE_WORLD_FOG);
                }

                Object darkenScreen = map.get("darken-screen");
                if (darkenScreen != null && Boolean.parseBoolean(String.valueOf(darkenScreen))) {
                    bossBar.addFlag(net.kyori.adventure.bossbar.BossBar.Flag.DARKEN_SCREEN);
                }

                yield new Destination(type, bossBar);
            }
            default -> new Destination(type);
        };
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
