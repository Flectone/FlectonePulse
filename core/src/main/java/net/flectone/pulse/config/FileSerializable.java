package net.flectone.pulse.config;

import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import lombok.Getter;
import net.elytrium.serializer.LoadResult;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Transient;
import net.elytrium.serializer.custom.ClassSerializer;
import net.elytrium.serializer.language.object.YamlSerializable;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.util.*;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.processing.serializer.EnumSerializer;
import net.kyori.adventure.bossbar.BossBar;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public abstract class FileSerializable extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig
            .Builder()
            .setBackupOnErrors(true)
            .registerSerializer(new EnumSerializer<>(FColor.class))
            .registerSerializer(new EnumSerializer<>(FPlayer.Setting.class))
            .registerSerializer(new EnumSerializer<>(Database.Type.class))
            .registerSerializer(new EnumSerializer<>(Permission.Type.class))
            .registerSerializer(new EnumSerializer<>(Destination.Type.class))
            .registerSerializer(new EnumSerializer<>(Toast.Type.class))
            .registerSerializer(new EnumSerializer<>(BossBar.Flag.class))
            .registerSerializer(new EnumSerializer<>(BossBar.Overlay.class))
            .registerSerializer(new EnumSerializer<>(BossBar.Color.class))
            .registerSerializer(new EnumSerializer<>(BubbleModule.Billboard.class))
            .registerSerializer(new EnumSerializer<>(WorldModule.Mode.class))
            .registerSerializer(new EnumSerializer<>(ObjectiveModule.Mode.class))
            .registerSerializer(new EnumSerializer<>(Event.Priority.class))
            .setLogMissingFields(false)
            .setSafeMode(true)
            .registerSerializer(new ClassSerializer<Range, Object>() {

                @Override
                public Object serialize(Range range) {
                    if (range.getType() == Range.Type.BLOCKS) {
                        return range.getValue();
                    }

                    return range.getType();
                }

                @Override
                public Range deserialize(Object object) {
                    String string = String.valueOf(object);

                    try {
                        int value = Integer.parseInt(string);
                        Range.Type type = Range.Type.fromInt(value);
                        if (type == Range.Type.BLOCKS) {
                            return new Range(value);
                        }

                        return new Range(type);
                    } catch (NumberFormatException e) {
                        Range.Type type = Range.Type.fromString(string);
                        return new Range(type);
                    }
                }
            })
            .registerSerializer(new ClassSerializer<Sound, Map<String, Object>>() {

                @Override
                public Map<String, Object> serialize(Sound sound) {
                    Map<String, Object> map = new LinkedHashMap<>();

                    map.put("enable", sound.isEnable());

                    if (sound.isEnable()) {
                        map.put("volume", sound.getVolume());
                        map.put("pitch", sound.getPitch());
                        map.put("category", sound.getCategory());
                        map.put("name", sound.getName());
                    }

                    return map;
                }

                @Override
                public Sound deserialize(Map<String, Object> map) {
                    boolean isEnable = Boolean.parseBoolean(String.valueOf(map.get("enable")));
                    if (!isEnable) return new Sound();

                    Object volume = map.get("volume");
                    float floatVolume = volume == null ? 1f : Float.parseFloat(String.valueOf(volume));

                    Object pitch = map.get("pitch");
                    float floatPitch = pitch == null ? 1f : Float.parseFloat(String.valueOf(pitch));

                    Object category = map.get("category");
                    String stringCategory = category == null ? SoundCategory.BLOCK.name() : String.valueOf(category);

                    Object name = map.get("name");
                    String stringName = name == null ? Sounds.BLOCK_NOTE_BLOCK_BELL.getName().toString(): String.valueOf(name);

                    return new Sound(true, floatVolume, floatPitch, stringCategory, stringName);
                }
            })
            .registerSerializer(new ClassSerializer<Cooldown, Map<String, Object>>() {

                @Override
                public Map<String, Object> serialize(Cooldown cooldown) {
                    Map<String, Object> map = new LinkedHashMap<>();

                    map.put("enable", cooldown.isEnable());

                    if (cooldown.isEnable()) {
                        map.put("duration", cooldown.getDuration());
                    }

                    return map;
                }

                @Override
                public Cooldown deserialize(Map<String, Object> map) {
                    boolean isEnable = Boolean.parseBoolean(String.valueOf(map.get("enable")));
                    if (!isEnable) return new Cooldown();

                    Object duration = map.get("duration");
                    long longDuration = duration == null ? 60L : Long.parseLong(String.valueOf(duration));

                    return new Cooldown(true, longDuration);
                }
            })
            .registerSerializer(new ClassSerializer<Ticker, Map<String, Object>>() {

                @Override
                public Map<String, Object> serialize(Ticker ticker) {
                    Map<String, Object> map = new LinkedHashMap<>();

                    map.put("enable", ticker.isEnable());

                    if (ticker.isEnable()) {
                        map.put("period", ticker.getPeriod());
                    }

                    return map;
                }

                @Override
                public Ticker deserialize(Map<String, Object> map) {
                    boolean isEnable = Boolean.parseBoolean(String.valueOf(map.get("enable")));
                    if (!isEnable) return new Ticker();

                    Object period = map.get("period");
                    long longPeriod = period == null ? 100L : Long.parseLong(String.valueOf(period));

                    return new Ticker(true, longPeriod);
                }
            })
            .registerSerializer(new ClassSerializer<Destination, Map<String, Object>>() {

                @Override
                public Map<String, Object> serialize(Destination destination) {
                    Map<String, Object> map = new LinkedHashMap<>();

                    Destination.Type type = destination.getType();

                    map.put("type", type);

                    switch (type) {
                        case TOAST -> {
                            Toast toast = destination.getToast();

                            map.put("icon", toast.icon());
                            map.put("style", toast.style());
                        }
                        case TITLE, SUBTITLE, ACTION_BAR -> {
                            Times times = destination.getTimes();

                            Map<String, Object> timesMap = new LinkedHashMap<>();
                            timesMap.put("stay", times.stayTicks());

                            if (type != Destination.Type.ACTION_BAR) {
                                timesMap.put("fade-in", times.fadeInTicks());
                                timesMap.put("fade-out", times.fadeOutTicks());

                                map.put("subtext", destination.getSubtext());
                            }

                            map.put("times",  timesMap);
                        }
                        case BOSS_BAR -> {
                            net.flectone.pulse.model.util.BossBar bossBar = destination.getBossBar();

                            map.put("duration", bossBar.getDuration());
                            map.put("health", bossBar.getHealth());
                            map.put("overlay", bossBar.getOverlay());
                            map.put("color", bossBar.getColor());
                            map.put("play-boos-music", bossBar.getFlags().contains(BossBar.Flag.PLAY_BOSS_MUSIC));
                            map.put("create-world-fog", bossBar.getFlags().contains(BossBar.Flag.CREATE_WORLD_FOG));
                            map.put("darken-screen", bossBar.getFlags().contains(BossBar.Flag.DARKEN_SCREEN));
                        }
                    }

                    return map;
                }

                @Override
                public Destination deserialize(Map<String, Object> map) {
                    Destination.Type type = Destination.Type.valueOf(String.valueOf(map.get("type")));

                    return switch (type) {
                        case TOAST -> {
                            Object icon = map.get("icon");
                            String stringIcon = icon == null ? "minecraft:diamond" : String.valueOf(icon);

                            Object style = map.get("style");
                            Toast.Type toastStyle = style == null ? Toast.Type.TASK : Toast.Type.valueOf(String.valueOf(style));

                            yield new Destination(Destination.Type.TOAST, new Toast(stringIcon, toastStyle));
                        }
                        case TITLE, SUBTITLE, ACTION_BAR -> {
                            Object times = map.get("times");

                            if (times == null) {
                                yield new Destination(type);
                            }

                            Map<String, Object> timesMap = (LinkedHashMap<String, Object>) times;

                            Object fadeIn = timesMap.get("fade-in");
                            int fadeInTicks = fadeIn == null ? 20 : Integer.parseInt(String.valueOf(fadeIn));

                            Object stay = timesMap.get("stay");
                            int stayTicks = stay == null ? 60 : Integer.parseInt(String.valueOf(stay));

                            Object fadeOut = timesMap.get("fade-out");
                            int fadeOutTicks = fadeOut == null ? 20 : Integer.parseInt(String.valueOf(fadeOut));

                            Times titleTimes = new Times(fadeInTicks, stayTicks, fadeOutTicks);

                            if (type == Destination.Type.ACTION_BAR) yield new Destination(type, titleTimes);

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
                            BossBar.Overlay bossBarOverlay = overlay == null ? BossBar.Overlay.PROGRESS : BossBar.Overlay.valueOf(String.valueOf(overlay));

                            Object color = map.get("color");
                            BossBar.Color bossBarColor = color == null ? BossBar.Color.BLUE : BossBar.Color.valueOf(String.valueOf(color));

                            net.flectone.pulse.model.util.BossBar bossBar = new net.flectone.pulse.model.util.BossBar(longDuration, floatHealth, bossBarOverlay, bossBarColor);

                            Object playBossMusic = map.get("play-boss-music");
                            if (playBossMusic != null && Boolean.parseBoolean(String.valueOf(playBossMusic))) {
                                bossBar.addFlag(BossBar.Flag.PLAY_BOSS_MUSIC);
                            }

                            Object createWorldFog = map.get("create-world-fog");
                            if (createWorldFog != null && Boolean.parseBoolean(String.valueOf(createWorldFog))) {
                                bossBar.addFlag(BossBar.Flag.CREATE_WORLD_FOG);
                            }

                            Object darkenScreen = map.get("darken-screen");
                            if (darkenScreen != null && Boolean.parseBoolean(String.valueOf(darkenScreen))) {
                                bossBar.addFlag(BossBar.Flag.DARKEN_SCREEN);
                            }

                            yield new Destination(type, new net.flectone.pulse.model.util.BossBar(
                                    longDuration,
                                    floatHealth,
                                    bossBarOverlay,
                                    bossBarColor
                            ));
                        }
                        default -> new Destination(type);
                    };
                }
            })
            .build();

    @Transient
    private final Path path;

    protected FileSerializable(Path path) {
        super(CONFIG);
        this.path = path;
    }

    @Override
    public LoadResult reload() {
        return super.reload(path);
    }

    @Override
    public void save() {
        super.save(path);
    }
}
