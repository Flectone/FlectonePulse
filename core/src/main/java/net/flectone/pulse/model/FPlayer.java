package net.flectone.pulse.model;

import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class FPlayer extends FEntity {

    public static final FPlayer UNKNOWN = new FPlayer(FEntity.UNKNOWN_NAME);

    private final int id;
    private final Map<String, String> colors = new HashMap<>();
    private final List<Moderation> mutes = new ArrayList<>();
    private final List<Ignore> ignores = new ArrayList<>();
    private final boolean[] settings = new boolean[Setting.values().length - 2];

    private boolean online;
    private String ip;
    private String chat;
    private String locale;
    private String worldPrefix;
    private String afkSuffix;
    private String streamPrefix;

    @Setter
    private String currentName;

    public FPlayer(int id, String name, UUID uuid, String type) {
        super(name, uuid, type);

        this.id = id;

        Arrays.fill(settings, true);

        set(Setting.STREAM, false);
        set(Setting.SPY, false);
    }

    public FPlayer(int id, String name, UUID uuid) {
        this(id, name, uuid, "player");
    }

    public FPlayer(String name) {
        this(-1, name, FEntity.UNKNOWN_UUID, "unknown");
    }

    public void updateMutes(List<Moderation> mutes) {
        this.mutes.clear();
        this.mutes.addAll(mutes);
    }

    public void setOnline(boolean online) {
        if (isUnknown()) return;
        this.online = online;
    }

    public void setIp(String ip) {
        if (isUnknown()) return;
        this.ip = ip;
    }

    public void setChat(String chat) {
        if (isUnknown()) return;
        this.chat = chat;
    }

    public void setLocale(String locale) {
        if (isUnknown()) return;
        this.locale = locale;
    }

    public void setWorldPrefix(String worldPrefix) {
        if (isUnknown()) return;
        this.worldPrefix = worldPrefix;
    }

    public void setAfkSuffix(String afkSuffix) {
        if (isUnknown()) return;
        this.afkSuffix = afkSuffix;
    }

    public void setStreamPrefix(String streamPrefix) {
        if (isUnknown()) return;
        this.streamPrefix = streamPrefix;
    }

    public boolean isIgnored(@NotNull FPlayer fPlayer) {
        if (ignores.isEmpty()) return false;

        return ignores
                .stream()
                .anyMatch(ignore -> ignore.target() == fPlayer.getId());
    }

    public Optional<Moderation> getMute() {
        if (mutes.isEmpty()) return Optional.empty();

        return mutes
                .stream()
                .filter(mute -> mute.isValid() && !mute.isExpired())
                .findAny();
    }

    public boolean equals(FPlayer fPlayer) {
        return this.id == fPlayer.getId();
    }

    public boolean isUnknown() {
        return this.getId() == -1;
    }

    public boolean is(Setting setting) {
        return settings[setting.ordinal()];
    }

    public void set(Setting setting, boolean value) {
        if (isUnknown()) return;
        settings[setting.ordinal()] = value;
    }

    public static boolean[] toBooleanArray(String input) {
        if (input == null) return null;

        input = input.substring(1, input.length() - 1);

        String[] stringValues = input.split(", ");

        boolean[] result = new boolean[stringValues.length];
        for (int i = 0; i < stringValues.length; i++) {
            result[i] = Boolean.parseBoolean(stringValues[i]);
        }

        return result;
    }

    public enum Setting {
        STREAM,
        SPY,
        ADVANCEMENT,
        DEATH,
        JOIN,
        QUIT,
        AUTO,
        ME,
        TRY,
        DICE,
        BALL,
        MUTE,
        BAN,
        WARN,
        TELL,
        REPLY,
        MAIL,
        TICTACTOE,
        KICK,
        TRANSLATETO,
        BROADCAST,
        DO,
        COIN,
        AFK,
        POLL,
        SPIT,
        GREETING,
        ROCKPAPERSCISSORS,
        DISCORD,
        TELEGRAM,
        TWITCH,
        CHAT,
        COLOR
    }
}
