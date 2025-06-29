package net.flectone.pulse.model;

import lombok.Getter;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class FPlayer extends FEntity {

    public static final FPlayer UNKNOWN = new FPlayer(FEntity.UNKNOWN_NAME);

    private final int id;
    private final Map<String, String> colors = new HashMap<>();
    private final Map<Setting, String> settings = new HashMap<>();
    private final List<Ignore> ignores = new ArrayList<>();

    private boolean online;
    private String ip;

    public FPlayer(int id, String name, UUID uuid, String type) {
        super(name, uuid, type);

        this.id = id;
    }

    public FPlayer(int id, String name, UUID uuid) {
        this(id, name, uuid, "player");
    }

    public FPlayer(String name) {
        this(-1, name, FEntity.UNKNOWN_UUID, "unknown");

        setDefaultSettings();
    }

    public void setIp(String ip) {
        if (isUnknown()) return;

        this.ip = ip;
    }

    public void setOnline(boolean online) {
        if (isUnknown()) return;

        this.online = online;
    }

    public boolean isIgnored(@NotNull FPlayer fPlayer) {
        if (ignores.isEmpty()) return false;

        return ignores
                .stream()
                .anyMatch(ignore -> ignore.target() == fPlayer.getId());
    }

    public void setSetting(Setting setting) {
        setSetting(setting, "");
    }

    public void setSetting(Setting setting, @Nullable String value) {
        settings.put(setting, value);
    }

    public void setDefaultSettings() {
        setSetting(Setting.ADVANCEMENT);
        setSetting(Setting.DEATH);
        setSetting(Setting.JOIN);
        setSetting(Setting.QUIT);
        setSetting(Setting.AUTO);
        setSetting(Setting.ME);
        setSetting(Setting.TRY);
        setSetting(Setting.DICE);
        setSetting(Setting.BALL);
        setSetting(Setting.MUTE);
        setSetting(Setting.BAN);
        setSetting(Setting.WARN);
        setSetting(Setting.TELL);
        setSetting(Setting.REPLY);
        setSetting(Setting.MAIL);
        setSetting(Setting.TICTACTOE);
        setSetting(Setting.KICK);
        setSetting(Setting.TRANSLATETO);
        setSetting(Setting.BROADCAST);
        setSetting(Setting.DO);
        setSetting(Setting.COIN);
        setSetting(Setting.AFK);
        setSetting(Setting.POLL);
        setSetting(Setting.GREETING);
        setSetting(Setting.ROCKPAPERSCISSORS);
        setSetting(Setting.DISCORD);
        setSetting(Setting.TELEGRAM);
        setSetting(Setting.TWITCH);
        setSetting(Setting.STYLE);
    }

    public boolean isSetting(Setting setting) {
        return settings.containsKey(setting);
    }

    @Nullable
    public String getSettingValue(Setting setting) {
        return settings.get(setting);
    }

    public void removeSetting(Setting setting) {
        settings.remove(setting);
    }

    public boolean equals(FPlayer fPlayer) {
        return this.id == fPlayer.getId();
    }

    @Override
    public boolean isUnknown() {
        return this.getId() == -1;
    }

    public enum Setting {
        ADVANCEMENT,
        AFK,
        AFK_SUFFIX,
        AUTO,
        BALL,
        BAN,
        BROADCAST,
        CHAT,
        COIN,
        COLOR,
        DEATH,
        DICE,
        DISCORD,
        DO,
        GREETING,
        JOIN,
        KICK,
        LOCALE,
        MAIL,
        ME,
        MUTE,
        POLL,
        QUIT,
        REPLY,
        ROCKPAPERSCISSORS,
        SPY,
        STREAM,
        STREAM_PREFIX,
        TELEGRAM,
        TELL,
        TICTACTOE,
        TRANSLATETO,
        TRY,
        TWITCH,
        WARN,
        WORLD_PREFIX,
        STYLE;

        @Nullable
        public static Setting fromString(String setting) {
            return Arrays.stream(Setting.values()).filter(s -> s.name().equalsIgnoreCase(setting))
                    .findFirst()
                    .orElse(null);
        }
    }
}
