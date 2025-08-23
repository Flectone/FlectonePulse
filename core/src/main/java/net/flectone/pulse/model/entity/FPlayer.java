package net.flectone.pulse.model.entity;

import lombok.Getter;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.util.Ignore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.flectone.pulse.service.FPlayerService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This is a platform-dynamic, Flectone player. All actions done through Flectone involving a player most likely are done through FPlayer.
 * <hr>
 * <p>
 *     For example, plugins using the Bukkit API can get an instance of the {@link FPlayer} object by simply using
 *     <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Entity.html#getUniqueId()"><code>Entity.getUniqueId()</code></a>
 *     and using {@link FPlayerService}'s <code>{@link UUID} getFPlayer</code> method.
 * </p>
 *
 * @see FPlayerService
 */
@Getter
public class FPlayer extends FEntity {

    public static final FPlayer UNKNOWN = new FPlayer(FEntity.unknownName);

    private final int id;
    private final Map<FColor.Type, Set<FColor>> fColors = new EnumMap<>(FColor.Type.class);
    private final Map<Setting, String> settings = new EnumMap<>(Setting.class);
    private final List<Ignore> ignores = new ArrayList<>();

    private boolean online;
    private String ip;
    private String constantName;

    public FPlayer(int id, String name, UUID uuid, String type) {
        super(name, uuid, type);

        this.id = id;
    }

    public FPlayer(int id, String name, UUID uuid) {
        this(id, name, uuid, "player");
    }

    public FPlayer(String name) {
        this(-1, name, FEntity.unknownUUID, "unknown");

        setDefaultSettings();
    }

    public void setOnline(boolean online) {
        if (isUnknown()) return;

        this.online = online;
    }

    public void setIp(String ip) {
        if (isUnknown()) return;

        this.ip = ip;
    }

    public void setConstantName(String constantName) {
        if (isUnknown()) return;

        this.constantName = constantName;
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
        setSetting(Setting.ANON);
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

    public Map<Integer, String> getFColors(FColor.Type type) {
        return fColors.getOrDefault(type, Collections.emptySet())
                .stream()
                .collect(Collectors.toMap(FColor::number, FColor::name));
    }

    public enum Setting {
        @Deprecated
        COLOR,

        @Deprecated
        STYLE,

        ADVANCEMENT,
        AFK,
        AFK_SUFFIX,
        AUTO,
        BALL,
        BAN,
        BROADCAST,
        CHAT,
        COIN,
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
        ANON;

        @Nullable
        public static Setting fromString(String setting) {
            return Arrays.stream(Setting.values()).filter(s -> s.name().equalsIgnoreCase(setting))
                    .findFirst()
                    .orElse(null);
        }
    }
}
