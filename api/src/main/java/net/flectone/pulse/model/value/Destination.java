package net.flectone.pulse.model.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Where a message is delivered on the client, such as chat, a title, a boss bar or a toast.
 * Only the fields that belong to the chosen {@link Type} are populated; the rest stay null.
 *
 * @param type the delivery target
 * @param subtext the secondary line, used by titles and toasts
 * @param bossBar the boss bar settings, used by {@link Type#BOSS_BAR}
 * @param times the fade and stay timings, used by titles and the action bar
 * @param toast the toast settings, used by {@link Type#TOAST}
 * @author TheFaser
 */
public record Destination(
        Type type,
        String subtext,
        BossBar bossBar,
        Times times,
        Toast toast,
        HeadText headText
) {

    /**
     * The delivery target used when the config leaves them out.
     */
    public static final Type DEFAULT_TYPE = Type.CHAT;
    /**
     * The secondary line used when the config leaves them out.
     */
    public static final String DEFAULT_SUBTEXT = "";
    /**
     * The boss bar settings used when the config leaves them out.
     */
    public static final BossBar DEFAULT_BOSS_BAR = new BossBar(100, 1f, net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS, net.kyori.adventure.bossbar.BossBar.Color.BLUE);
    /**
     * The title timings used when the config leaves them out.
     */
    public static final Times DEFAULT_TIMES = new Times(20, 60, 20);
    /**
     * The toast settings used when the config leaves them out.
     */
    public static final Toast DEFAULT_TOAST = new Toast("minecraft:diamond", Toast.Type.TASK);
    /**
     * The head text settings used when the config leaves them out.
     */
    public static final HeadText DEFAULT_HEAD_TEXT = HeadText.builder()
            .visibleToSelf(true)
            .hideWhenSneaking(false)
            .filterIgnores(false)
            .movable(true)
            .distance(30.0)
            .row(0)
            .maxCount(1)
            .lifeTime(HeadText.AUTO)
            .modern(HeadText.Modern.builder()
                    .enable(true)
                    .hasShadow(true)
                    .seeThrough(false)
                    .disableSeeThroughWhenSneaking(true)
                    .background("#00000040")
                    .animationTime(5)
                    .offsetX(0)
                    .offsetY(0.2f)
                    .offsetZ(0)
                    .scale(1.0f)
                    .scaleWithPlayer(false)
                    .space(0.3f)
                    .billboard(HeadText.Modern.Billboard.CENTER)
                    .build()
            )
            .build();
    /**
     * The shared all-default destination for the action bar, so equal destinations share one instance.
     */
    public static final Destination EMPTY_ACTION_BAR = new Destination(Type.ACTION_BAR, DEFAULT_TIMES);
    /**
     * The shared all-default destination for a boss bar, so equal destinations share one instance.
     */
    public static final Destination EMPTY_BOSS_BAR = new Destination(Type.BOSS_BAR, DEFAULT_BOSS_BAR);
    /**
     * The shared all-default destination for the server brand, so equal destinations share one instance.
     */
    public static final Destination EMPTY_BRAND = new Destination(Type.BRAND);
    /**
     * The shared all-default destination for chat, so equal destinations share one instance.
     */
    public static final Destination EMPTY_CHAT = new Destination(DEFAULT_TYPE);
    /**
     * The shared all-default destination for a title, so equal destinations share one instance.
     */
    public static final Destination EMPTY_TITLE = new Destination(Type.TITLE, DEFAULT_TIMES, DEFAULT_SUBTEXT);
    /**
     * The shared all-default destination for a subtitle, so equal destinations share one instance.
     */
    public static final Destination EMPTY_SUBTITLE = new Destination(Type.SUBTITLE, DEFAULT_TIMES, DEFAULT_SUBTEXT);
    /**
     * The shared all-default destination for the tab list header, so equal destinations share one instance.
     */
    public static final Destination EMPTY_TAB_HEADER = new Destination(Type.TAB_HEADER);
    /**
     * The shared all-default destination for the tab list footer, so equal destinations share one instance.
     */
    public static final Destination EMPTY_TAB_FOOTER = new Destination(Type.TAB_FOOTER);
    /**
     * The shared all-default destination for a toast, so equal destinations share one instance.
     */
    public static final Destination EMPTY_TOAST = new Destination(Type.TOAST, DEFAULT_TOAST, DEFAULT_SUBTEXT);

    /**
     * Fills in the defaults that the chosen type needs, leaving unrelated fields null.
     */
    public Destination {
        type = type != null ? type : DEFAULT_TYPE;
        subtext = (subtext == null && (type == Type.TITLE || type == Type.SUBTITLE)) ? DEFAULT_SUBTEXT : subtext;
        bossBar = (bossBar == null && type == Type.BOSS_BAR) ? DEFAULT_BOSS_BAR : bossBar;
        times = (times == null && (type == Type.TITLE || type == Type.SUBTITLE || type == Type.ACTION_BAR)) ? DEFAULT_TIMES : times;
        toast = (toast == null && type == Type.TOAST) ? DEFAULT_TOAST : toast;
        headText = (headText == null && type == Type.HEAD_TEXT) ? DEFAULT_HEAD_TEXT : headText;
    }

    /**
     * Creates a destination that needs no extra settings.
     *
     * @param type the delivery target
     */
    public Destination(Type type) {
        this(type, null, null, null, null, null);
    }

    /**
     * Creates a boss bar destination.
     *
     * @param type the delivery target
     * @param bossBar the boss bar settings
     */
    public Destination(Type type, BossBar bossBar) {
        this(type, null, bossBar, null, null, null);
    }

    /**
     * Creates a destination timed by fade in, stay and fade out.
     *
     * @param type the delivery target
     * @param times the timings
     */
    public Destination(Type type, Times times) {
        this(type, null, null, times, null, null);
    }

    /**
     * Creates a title-like destination with a secondary line.
     *
     * @param type the delivery target
     * @param times the timings
     * @param subtext the secondary line
     */
    public Destination(Type type, Times times, String subtext) {
        this(type, subtext, null, times, null, null);
    }

    /**
     * Creates a toast destination with a secondary line.
     *
     * @param type the delivery target
     * @param toast the toast settings
     * @param subtext the secondary line
     */
    public Destination(Type type, Toast toast, String subtext) {
        this(type, subtext, null, null, toast, null);
    }

    /**
     * Creates a head text destination.
     *
     * @param type the delivery target
     * @param headText the head text settings
     */
    public Destination(Type type, HeadText headText) {
        this(type, null, null, null, null, headText);
    }

    /**
     * Reads a destination from its config representation.
     * Values matching the defaults collapse to the shared EMPTY_* constants so equal destinations share one instance.
     *
     * @param map the raw config values
     * @return the parsed destination
     */
    @JsonCreator
    public static Destination fromJson(Map<String, Object> map) {
        Type type = Type.valueOf(String.valueOf(map.get("type")));

        return switch (type) {
            case HEAD_TEXT -> {
                boolean visibleToSelf = parseOrDefault(map.get("visible_to_self"), DEFAULT_HEAD_TEXT.visibleToSelf(), Boolean::parseBoolean);
                boolean hideWhenSneaking = parseOrDefault(map.get("hide_when_sneaking"), DEFAULT_HEAD_TEXT.hideWhenSneaking(), Boolean::parseBoolean);
                boolean filterIgnores = parseOrDefault(map.get("filter_ignores"), DEFAULT_HEAD_TEXT.filterIgnores(), Boolean::parseBoolean);
                boolean movable = parseOrDefault(map.get("movable"), DEFAULT_HEAD_TEXT.movable(), Boolean::parseBoolean);
                int row = parseOrDefault(map.get("row"), DEFAULT_HEAD_TEXT.row(), Integer::parseInt);
                int maxCount = parseOrDefault(map.get("max_count"), DEFAULT_HEAD_TEXT.maxCount(), Integer::parseInt);
                double distance = parseOrDefault(map.get("distance"), DEFAULT_HEAD_TEXT.distance(), Double::parseDouble);
                long lifeTime = parseOrDefault(map.get("life_time"), DEFAULT_HEAD_TEXT.lifeTime(), Long::parseLong);

                Object mapModern = map.get("modern");
                if (!(mapModern instanceof Map<?, ?>)) {
                    yield new Destination(type, HeadText.builder()
                            .visibleToSelf(visibleToSelf)
                            .hideWhenSneaking(hideWhenSneaking)
                            .filterIgnores(filterIgnores)
                            .movable(movable)
                            .row(row)
                            .maxCount(maxCount)
                            .distance(distance)
                            .lifeTime(lifeTime)
                            .modern(DEFAULT_HEAD_TEXT.modern())
                            .build()
                    );
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> modernMap = (Map<String, Object>) mapModern;

                boolean enable = parseOrDefault(modernMap.get("enable"), DEFAULT_HEAD_TEXT.modern().enable(), Boolean::parseBoolean);
                boolean hasShadow = parseOrDefault(modernMap.get("has_shadow"), DEFAULT_HEAD_TEXT.modern().hasShadow(), Boolean::parseBoolean);
                boolean seeThrough = parseOrDefault(modernMap.get("see_through"), DEFAULT_HEAD_TEXT.modern().seeThrough(), Boolean::parseBoolean);
                boolean disableSeeThroughWhenSneaking = parseOrDefault(modernMap.get("disable_see_through_when_sneaking"), DEFAULT_HEAD_TEXT.modern().enable(), Boolean::parseBoolean);
                String background = parseOrDefault(modernMap.get("background"), DEFAULT_HEAD_TEXT.modern().background(), String::valueOf);
                int animationTime = parseOrDefault(modernMap.get("animation_time"), DEFAULT_HEAD_TEXT.modern().animationTime(), Integer::parseInt);
                float offsetX = parseOrDefault(modernMap.get("offset_x"), DEFAULT_HEAD_TEXT.modern().offsetX(), Float::parseFloat);
                float offsetY = parseOrDefault(modernMap.get("offset_y"), DEFAULT_HEAD_TEXT.modern().offsetY(), Float::parseFloat);
                float offsetZ = parseOrDefault(modernMap.get("offset_z"), DEFAULT_HEAD_TEXT.modern().offsetZ(), Float::parseFloat);
                float scale = parseOrDefault(modernMap.get("scale"), DEFAULT_HEAD_TEXT.modern().scale(), Float::parseFloat);
                boolean scaleWithPlayer = parseOrDefault(modernMap.get("scale_with_player"), DEFAULT_HEAD_TEXT.modern().scaleWithPlayer(), Boolean::parseBoolean);
                float space = parseOrDefault(modernMap.get("space"), DEFAULT_HEAD_TEXT.modern().space(), Float::parseFloat);

                HeadText.Modern.Billboard billboard;
                try {
                    billboard = HeadText.Modern.Billboard.valueOf(String.valueOf(modernMap.get("billboard")));
                } catch (IllegalArgumentException _) {
                    billboard = DEFAULT_HEAD_TEXT.modern().billboard();
                }

                yield new Destination(type, HeadText.builder()
                        .visibleToSelf(visibleToSelf)
                        .hideWhenSneaking(hideWhenSneaking)
                        .filterIgnores(filterIgnores)
                        .movable(movable)
                        .row(row)
                        .maxCount(maxCount)
                        .distance(distance)
                        .lifeTime(lifeTime)
                        .modern(HeadText.Modern.builder()
                                .enable(enable)
                                .hasShadow(hasShadow)
                                .seeThrough(seeThrough)
                                .disableSeeThroughWhenSneaking(disableSeeThroughWhenSneaking)
                                .background(background)
                                .animationTime(animationTime)
                                .offsetX(offsetX)
                                .offsetY(offsetY)
                                .offsetZ(offsetZ)
                                .scale(scale)
                                .scaleWithPlayer(scaleWithPlayer)
                                .space(space)
                                .billboard(billboard)
                                .build()
                        )
                        .build()
                );
            }
            case TOAST -> {
                String icon = parseOrDefault(map.get("icon"), DEFAULT_TOAST.icon(), string -> string);
                String subtext = parseOrDefault(map.get("subtext"), DEFAULT_SUBTEXT, string -> string);
                Toast.Type style = parseOrDefault(map.get("style"), DEFAULT_TOAST.style(), Toast.Type::valueOf);
                Toast toast = new Toast(icon, style);
                yield toast.equals(DEFAULT_TOAST) ? EMPTY_TOAST : new Destination(Type.TOAST, toast, subtext);
            }
            case TITLE, SUBTITLE, ACTION_BAR -> {
                Object mapTimes = map.get("times");
                if (!(mapTimes instanceof Map<?, ?>)) {
                    yield new Destination(type);
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> timesMap = (Map<String, Object>) mapTimes;

                int fadeIn = parseOrDefault(timesMap.get("fade_in"), DEFAULT_TIMES.fadeInTicks(), Integer::parseInt);
                int stay = parseOrDefault(timesMap.get("stay"), DEFAULT_TIMES.stayTicks(), Integer::parseInt);
                int fadeOut = parseOrDefault(timesMap.get("fade_out"), DEFAULT_TIMES.fadeInTicks(), Integer::parseInt);

                Times times = new Times(fadeIn, stay, fadeOut);
                if (type == Type.ACTION_BAR) {
                    yield times.equals(DEFAULT_TIMES) ? EMPTY_ACTION_BAR : new Destination(type, times);
                }

                String subtext = parseOrDefault(map.get("subtext"), DEFAULT_SUBTEXT, string -> string);
                if (type == Type.TITLE && times.equals(DEFAULT_TIMES) && subtext.equals(DEFAULT_SUBTEXT)) yield EMPTY_TITLE;
                if (type == Type.SUBTITLE && times.equals(DEFAULT_TIMES) && subtext.equals(DEFAULT_SUBTEXT)) yield EMPTY_SUBTITLE;

                yield new Destination(type, times, subtext);
            }
            case BOSS_BAR -> {
                long duration = parseOrDefault(map.get("duration"), DEFAULT_BOSS_BAR.duration(), Long::parseLong);
                float health = parseOrDefault(map.get("health"), DEFAULT_BOSS_BAR.health(), Float::parseFloat);
                net.kyori.adventure.bossbar.BossBar.Overlay overlay = parseOrDefault(map.get("overlay"), DEFAULT_BOSS_BAR.overlay(), net.kyori.adventure.bossbar.BossBar.Overlay::valueOf);
                net.kyori.adventure.bossbar.BossBar.Color color = parseOrDefault(map.get("color"), DEFAULT_BOSS_BAR.color(), net.kyori.adventure.bossbar.BossBar.Color::valueOf);

                BossBar bossBar = new BossBar(duration, health, overlay, color);

                boolean playBossMusic = parseOrDefault(map.get("play_boss_music"), false, Boolean::parseBoolean);
                if (playBossMusic) {
                    bossBar = bossBar.withFlag(net.kyori.adventure.bossbar.BossBar.Flag.PLAY_BOSS_MUSIC);
                }

                boolean createWorldFog = parseOrDefault(map.get("create_world_fog"), false, Boolean::parseBoolean);
                if (createWorldFog) {
                    bossBar = bossBar.withFlag(net.kyori.adventure.bossbar.BossBar.Flag.CREATE_WORLD_FOG);
                }

                boolean darkenScreen = parseOrDefault(map.get("darken_screen"), false, Boolean::parseBoolean);
                if (darkenScreen) {
                    bossBar = bossBar.withFlag(net.kyori.adventure.bossbar.BossBar.Flag.DARKEN_SCREEN);
                }

                yield bossBar.equals(DEFAULT_BOSS_BAR) ? EMPTY_BOSS_BAR : new Destination(type, bossBar);
            }
            case BRAND -> EMPTY_BRAND;
            case CHAT -> EMPTY_CHAT;
            case TAB_HEADER -> EMPTY_TAB_HEADER;
            case TAB_FOOTER -> EMPTY_TAB_FOOTER;
        };
    }

    private static <T> T parseOrDefault(Object object, T defaultObject, Function<String, T> functionParse) {
        return object == null ? defaultObject : functionParse.apply(String.valueOf(object));
    }

    /**
     * Writes this destination back to its config representation, emitting only the keys its type uses.
     *
     * @return the config values
     */
    @JsonValue
    public Map<String, Object> toJson() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", this.type);

        switch (this.type) {
            case HEAD_TEXT -> {
                HeadText headText = this.headText != null ? this.headText : DEFAULT_HEAD_TEXT;
                map.put("visible_to_self", headText.visibleToSelf());
                map.put("hide_when_sneaking", headText.hideWhenSneaking());
                map.put("filter_ignores", headText.filterIgnores());
                map.put("movable", headText.movable());
                map.put("row", headText.row());
                map.put("max_count", headText.maxCount());
                map.put("distance", headText.distance());
                map.put("life_time", headText.lifeTime());

                HeadText.Modern modern = headText.modern() != null ? headText.modern() : DEFAULT_HEAD_TEXT.modern();
                Map<String, Object> modernMap = new LinkedHashMap<>();
                modernMap.put("enable", modern.enable());
                modernMap.put("has_shadow", modern.hasShadow());
                modernMap.put("see_through", modern.seeThrough());
                modernMap.put("disable_see_through_when_sneaking", modern.disableSeeThroughWhenSneaking());
                modernMap.put("background", modern.background());
                modernMap.put("animation_time", modern.animationTime());
                modernMap.put("offset_x", modern.offsetX());
                modernMap.put("offset_y", modern.offsetY());
                modernMap.put("offset_z", modern.offsetZ());
                modernMap.put("scale", modern.scale());
                modernMap.put("scale_with_player", modern.scaleWithPlayer());
                modernMap.put("space", modern.space());
                modernMap.put("billboard", modern.billboard());

                map.put("modern", modernMap);
            }
            case TOAST -> {
                Toast toast = this.toast != null ? this.toast : DEFAULT_TOAST;
                map.put("icon", toast.icon());
                map.put("subtext", this.subtext);
                map.put("style", toast.style());
            }
            case TITLE, SUBTITLE, ACTION_BAR -> {
                Times times = this.times != null ? this.times : DEFAULT_TIMES;
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
                BossBar bossBar = this.bossBar != null ? this.bossBar : DEFAULT_BOSS_BAR;
                map.put("duration", bossBar.duration());
                map.put("health", bossBar.health());
                map.put("overlay", bossBar.overlay());
                map.put("color", bossBar.color());
                map.put("play_boss_music", bossBar.flags().contains(net.kyori.adventure.bossbar.BossBar.Flag.PLAY_BOSS_MUSIC));
                map.put("create_world_fog", bossBar.flags().contains(net.kyori.adventure.bossbar.BossBar.Flag.CREATE_WORLD_FOG));
                map.put("darken_screen", bossBar.flags().contains(net.kyori.adventure.bossbar.BossBar.Flag.DARKEN_SCREEN));
            }
        }

        return map;
    }

    /**
     * The client surface a message is written to.
     */
    public enum Type {
        ACTION_BAR,
        BOSS_BAR,
        BRAND,
        CHAT,
        TITLE,
        SUBTITLE,
        TAB_HEADER,
        TAB_FOOTER,
        TOAST,
        HEAD_TEXT
    }
}