package net.flectone.pulse.file;

import lombok.Getter;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Transient;
import net.elytrium.serializer.custom.ClassSerializer;
import net.elytrium.serializer.language.object.YamlSerializable;
import net.flectone.pulse.model.Cooldown;
import net.flectone.pulse.model.Destination;
import net.flectone.pulse.model.Sound;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.util.TimeUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;

import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public abstract class FileSerializable extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig
            .Builder()
            .setBackupOnErrors(true)
            .registerSerializer(new ClassSerializer<Sound, Map<String, Object>>() {

                @Override
                public Map<String, Object> serialize(Sound sound) {
                    Map<String, Object> map = new LinkedHashMap<>();

                    map.put("enable", sound.isEnable());

                    if (sound.isEnable()) {
                        map.put("volume", sound.getVolume());
                        map.put("pitch", sound.getPitch());
                        map.put("type", sound.getType());
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

                    Object type = map.get("type");
                    String stringType = "BLOCK_NOTE_BLOCK_BELL";
                    if (type != null) {
                        stringType = String.valueOf(type);

                        // older version check (0.1.0 and older)
                        // type:volume:pitch
                        String[] legacySound = stringType.split(":");
                        if (legacySound.length > 1) {
                            stringType = legacySound[0];
                        }
                    }

                    return new Sound(true, floatVolume, floatPitch, stringType);
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

                    map.put("type", destination.getType());

                    switch (destination.getType()) {
                        case TITLE, SUBTITLE -> {
                            Title.Times times = destination.getTimes();

                            Map<String, Object> timesMap = new LinkedHashMap<>();
                            timesMap.put("fade-in", times.fadeIn().toMillis() / TimeUtil.MULTIPLIER);
                            timesMap.put("stay", times.stay().toMillis() / TimeUtil.MULTIPLIER);
                            timesMap.put("fade-out", times.fadeOut().toMillis() / TimeUtil.MULTIPLIER);

                            map.put("times",  timesMap);
                        }
                        case BOSS_BAR -> {
                            map.put("duration", destination.getDuration());
                            map.put("health", destination.getHealth());
                            map.put("overlay", destination.getOverlay());
                            map.put("color", destination.getColor());
                            map.put("play-boos-music", destination.getFlags().contains(BossBar.Flag.PLAY_BOSS_MUSIC));
                            map.put("create-world-fog", destination.getFlags().contains(BossBar.Flag.CREATE_WORLD_FOG));
                            map.put("darken-screen", destination.getFlags().contains(BossBar.Flag.DARKEN_SCREEN));
                        }
                    }

                    return map;
                }

                @Override
                public Destination deserialize(Map<String, Object> map) {
                    Destination.Type type = Destination.Type.valueOf(String.valueOf(map.get("type")));

                    return switch (type) {
                        case TITLE, SUBTITLE -> {
                            Object times = map.get("times");

                            if (times == null) {
                                yield new Destination(type);
                            }

                            Map<String, Object> timesMap = (LinkedHashMap<String, Object>) times;

                            Object fadeIn = timesMap.get("fade-in");
                            long longFadeIn = fadeIn == null ? 20 : Long.parseLong(String.valueOf(fadeIn));

                            Object stay = timesMap.get("stay");
                            long longStay = stay == null ? 100 : Long.parseLong(String.valueOf(stay));

                            Object fadeOut = timesMap.get("fade-out");
                            long longFadeOut = fadeOut == null ? 20 : Long.parseLong(String.valueOf(fadeOut));

                            Title.Times titleTimes = Title.Times.times(
                                    Duration.ofMillis(longFadeIn * TimeUtil.MULTIPLIER),
                                    Duration.ofMillis(longStay * TimeUtil.MULTIPLIER),
                                    Duration.ofMillis(longFadeOut * TimeUtil.MULTIPLIER)
                            );

                            yield new Destination(type, titleTimes);
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

                            Object playBossMusic = map.get("play-boss-music");
                            boolean booleanPlayBossMusic = playBossMusic != null && Boolean.parseBoolean(String.valueOf(playBossMusic));

                            Object createWorldFog = map.get("create-world-fog");
                            boolean booleanCreateWorldFog = createWorldFog != null && Boolean.parseBoolean(String.valueOf(createWorldFog));

                            Object darkenScreen = map.get("darken-screen");
                            boolean booleanDarkenScreen = darkenScreen != null && Boolean.parseBoolean(String.valueOf(darkenScreen));

                            yield new Destination(type,
                                    longDuration,
                                    floatHealth,
                                    bossBarOverlay,
                                    bossBarColor,
                                    booleanPlayBossMusic,
                                    booleanCreateWorldFog,
                                    booleanDarkenScreen
                            );
                        }
                        default -> new Destination(type);
                    };
                }
            })
            .build();

    @Transient
    private final Path path;

    public FileSerializable(Path path) {
        super(CONFIG);
        this.path = path;
    }
}
