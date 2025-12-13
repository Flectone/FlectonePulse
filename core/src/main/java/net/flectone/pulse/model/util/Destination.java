package net.flectone.pulse.model.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Getter
public class Destination {

    public static final Type DEFAULT_TYPE = Type.CHAT;
    public static final String DEFAULT_SUBTEXT = "";
    public static final BossBar DEFAULT_BOSS_BAR = new BossBar(100, 1f, net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS, net.kyori.adventure.bossbar.BossBar.Color.BLUE);
    public static final Times DEFAULT_TIMES = new Times(20, 60, 20);
    public static final Toast DEFAULT_TOAST = new Toast("minecraft:diamond", Toast.Type.TASK);
    public static final TextScreen DEFAULT_TEXT_SCREEN = new TextScreen("#00000040", false, 2, 10, 100000, 0.5f, 0f, -0.3f, -0.8f);

    private final Type type;
    private final String subtext;
    private final BossBar bossBar;
    private final Times times;
    private final Toast toast;
    private final TextScreen textScreen;

    public Destination(Type type, String subtext, BossBar bossBar, Times times, Toast toast, TextScreen textScreen) {
        this.type = type == null ? DEFAULT_TYPE : type;
        this.subtext = subtext;
        this.bossBar = bossBar;
        this.times = times;
        this.toast = toast;
        this.textScreen = textScreen;
    }

    public Destination() {
        this(null, null, null, null, null, null);
    }

    public Destination(Type type) {
        this(type, null, null, null, null, null);
    }

    public Destination(Type type, BossBar bossBar) {
        this(type, null, bossBar, null, null, null);
    }

    public Destination(Type type, Times times) {
        this(type, null, null, times, null, null);
    }

    public Destination(Type type, Times times, String subtext) {
        this(type, subtext, null, times, null, null);
    }

    public Destination(Type type, Toast toast) {
        this(type, null, null, null, toast, null);
    }

    public Destination(Type type, TextScreen textScreen) {
        this(type, null, null, null, null, textScreen);
    }

    @JsonValue
    public Map<String, Object> toJson() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", this.type);

        switch (this.type) {
            case TOAST -> {
                map.put("icon", toast.icon());
                map.put("style", toast.style());
            }
            case TITLE, SUBTITLE, ACTION_BAR -> {
                Map<String, Object> timesMap = new LinkedHashMap<>();
                timesMap.put("stay", times.stayTicks());

                if (this.type != Type.ACTION_BAR) {
                    timesMap.put("fade_in", times.fadeInTicks());
                    timesMap.put("fade_out", times.fadeOutTicks());

                    map.put("subtext", this.subtext);
                }

                map.put("times", timesMap);
            }
            case BOSS_BAR -> {
                map.put("duration", bossBar.getDuration());
                map.put("health", bossBar.getHealth());
                map.put("overlay", bossBar.getOverlay());
                map.put("color", bossBar.getColor());
                map.put("play_boss_music", bossBar.getFlags().contains(net.kyori.adventure.bossbar.BossBar.Flag.PLAY_BOSS_MUSIC));
                map.put("create_world_fog", bossBar.getFlags().contains(net.kyori.adventure.bossbar.BossBar.Flag.CREATE_WORLD_FOG));
                map.put("darken_screen", bossBar.getFlags().contains(net.kyori.adventure.bossbar.BossBar.Flag.DARKEN_SCREEN));
            }
            case TEXT_SCREEN -> {
                map.put("background", textScreen.background());
                map.put("has_shadow", textScreen.hasShadow());
                map.put("animation_time", textScreen.animationTime());
                map.put("live_time", textScreen.liveTime());
                map.put("width", textScreen.width());
                map.put("scale", textScreen.scale());
                map.put("offset_x", textScreen.offsetX());
                map.put("offset_y", textScreen.offsetY());
                map.put("offset_z", textScreen.offsetZ());
            }
        }

        return map;
    }

    @JsonCreator
    public static Destination fromJson(Map<String, Object> map) {
        Type type = Type.valueOf(String.valueOf(map.get("type")));

        return switch (type) {
            case TOAST -> {
                String icon = parseOrDefault(map.get("icon"), DEFAULT_TOAST.icon(), string -> string);
                Toast.Type style = parseOrDefault(map.get("style"), DEFAULT_TOAST.style(), Toast.Type::valueOf);
                yield new Destination(Type.TOAST, new Toast(icon, style));
            }
            case TITLE, SUBTITLE, ACTION_BAR -> {
                Object mapTimes = map.get("times");

                if (mapTimes == null) {
                    yield new Destination(type);
                }

                Map<String, Object> timesMap = (Map<String, Object>) mapTimes;

                int fadeIn = parseOrDefault(timesMap.get("fade_in"), DEFAULT_TIMES.fadeInTicks(), Integer::parseInt);
                int stay = parseOrDefault(timesMap.get("stay"), DEFAULT_TIMES.stayTicks(), Integer::parseInt);
                int fadeOut = parseOrDefault(timesMap.get("fade_out"), DEFAULT_TIMES.fadeInTicks(), Integer::parseInt);

                Times times = new Times(fadeIn, stay, fadeOut);
                if (type == Type.ACTION_BAR) {
                    yield new Destination(type, times);
                }

                String subtext = parseOrDefault(map.get("subtext"), DEFAULT_SUBTEXT, string -> string);
                yield new Destination(type, times, subtext);
            }
            case BOSS_BAR -> {
                long duration = parseOrDefault(map.get("duration"), DEFAULT_BOSS_BAR.getDuration(), Long::parseLong);
                float health = parseOrDefault(map.get("health"), DEFAULT_BOSS_BAR.getHealth(), Float::parseFloat);
                net.kyori.adventure.bossbar.BossBar.Overlay overlay = parseOrDefault(map.get("overlay"), DEFAULT_BOSS_BAR.getOverlay(), net.kyori.adventure.bossbar.BossBar.Overlay::valueOf);
                net.kyori.adventure.bossbar.BossBar.Color color = parseOrDefault(map.get("color"), DEFAULT_BOSS_BAR.getColor(), net.kyori.adventure.bossbar.BossBar.Color::valueOf);

                net.flectone.pulse.model.util.BossBar bossBar = new net.flectone.pulse.model.util.BossBar(duration, health, overlay, color);

                boolean playBossMusic = parseOrDefault(map.get("play_boss_music"), false, Boolean::parseBoolean);
                if (playBossMusic) {
                    bossBar.addFlag(net.kyori.adventure.bossbar.BossBar.Flag.PLAY_BOSS_MUSIC);
                }

                boolean createWorldFog = parseOrDefault(map.get("create_world_fog"), false, Boolean::parseBoolean);
                if (createWorldFog) {
                    bossBar.addFlag(net.kyori.adventure.bossbar.BossBar.Flag.CREATE_WORLD_FOG);
                }

                boolean darkenScreen = parseOrDefault(map.get("darken_screen"), false, Boolean::parseBoolean);
                if (darkenScreen) {
                    bossBar.addFlag(net.kyori.adventure.bossbar.BossBar.Flag.DARKEN_SCREEN);
                }

                yield new Destination(type, bossBar);
            }
            case TEXT_SCREEN -> {
                String background = parseOrDefault(map.get("background"), DEFAULT_TEXT_SCREEN.background(), string -> string);
                boolean hasShadow = parseOrDefault(map.get("has_shadow"), DEFAULT_TEXT_SCREEN.hasShadow(), Boolean::parseBoolean);
                int animationTime = parseOrDefault(map.get("animation_time"), DEFAULT_TEXT_SCREEN.animationTime(), Integer::parseInt);
                int liveTime = parseOrDefault(map.get("live_time"), DEFAULT_TEXT_SCREEN.liveTime(), Integer::parseInt);
                int width = parseOrDefault(map.get("width"), DEFAULT_TEXT_SCREEN.width(), Integer::parseInt);
                float scale = parseOrDefault(map.get("scale"), DEFAULT_TEXT_SCREEN.scale(), Float::parseFloat);
                float offsetX = parseOrDefault(map.get("offset_x"), DEFAULT_TEXT_SCREEN.offsetX(), Float::parseFloat);
                float offsetY = parseOrDefault(map.get("offset_y"), DEFAULT_TEXT_SCREEN.offsetY(), Float::parseFloat);
                float offsetZ = parseOrDefault(map.get("offset_z"), DEFAULT_TEXT_SCREEN.offsetZ(), Float::parseFloat);

                TextScreen textScreen = new TextScreen(background, hasShadow, animationTime, liveTime, width, scale, offsetX, offsetY, offsetZ);
                yield new Destination(type, textScreen);
            }
            default -> new Destination(type);
        };
    }

    @NotNull
    private static <T> T parseOrDefault(Object object, T defaultObject, Function<String, T> functionParse) {
        return object == null ? defaultObject : functionParse.apply(String.valueOf(object));
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
        TEXT_SCREEN,
        TOAST;
    }
}
