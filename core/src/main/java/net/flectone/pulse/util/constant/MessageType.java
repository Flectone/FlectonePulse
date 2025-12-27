package net.flectone.pulse.util.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum MessageType {

    AFK,
    ADVANCEMENT,
    AUTO,
    BOSSBAR,
    BRAND,
    CHAT,
    COMMAND_AFK,
    COMMAND_ANON,
    COMMAND_BALL,
    COMMAND_BAN,
    COMMAND_BANLIST,
    COMMAND_BROADCAST,
    COMMAND_CHATCOLOR,
    COMMAND_CHATSETTING,
    COMMAND_CLEARCHAT,
    COMMAND_CLEARMAIL,
    COMMAND_COIN,
    COMMAND_DELETE,
    COMMAND_DICE,
    COMMAND_DO,
    COMMAND_EMIT,
    COMMAND_FLECTONEPULSE,
    COMMAND_GEOLOCATE,
    COMMAND_HELPER,
    COMMAND_IGNORE,
    COMMAND_IGNORELIST,
    COMMAND_KICK,
    COMMAND_MAIL,
    COMMAND_MAINTENANCE,
    COMMAND_ME,
    COMMAND_MUTE,
    COMMAND_MUTELIST,
    COMMAND_ONLINE,
    COMMAND_PING,
    COMMAND_POLL,
    COMMAND_REPLY,
    COMMAND_ROCKPAPERSCISSORS,
    COMMAND_SPRITE,
    COMMAND_SPY,
    COMMAND_STREAM,
    COMMAND_SYMBOL,
    COMMAND_TELL,
    COMMAND_TICTACTOE,
    COMMAND_TOPONLINE,
    COMMAND_TRANSLATETO,
    COMMAND_TRY,
    COMMAND_UNBAN,
    COMMAND_UNMUTE,
    COMMAND_UNWARN,
    COMMAND_WARN,
    COMMAND_WARNLIST,
    DEATH,
    DELETE,
    ERROR,
    FOOTER,
    FORMAT,
    FROM_DISCORD_TO_MINECRAFT,
    FROM_TELEGRAM_TO_MINECRAFT,
    FROM_TWITCH_TO_MINECRAFT,
    GREETING,
    HEADER,
    JOIN,
    MENTION,
    MOTD,
    NAME,
    NEWBIE,
    OBJECTIVE,
    PLAYERS,
    PLAYERLISTNAME,
    QUESTION_ANSWER,
    QUIT,
    OBJECT,
    REPLACEMENT,
    RIGHT_CLICK,
    SIDEBAR,
    SLEEP,
    SWEAR,
    TRANSLATE,
    UPDATE,
    VANILLA,
    VERSION,

    SERVER_ENABLE,
    SERVER_DISABLE,

    // only for invalidation cache
    SYSTEM_ONLINE,
    SYSTEM_OFFLINE,
    SYSTEM_BAN,
    SYSTEM_MUTE,
    SYSTEM_WARN;

    private static final Map<String, MessageType> ENUM_BY_PROXY_KEY = Arrays.stream(MessageType.values())
            .collect(Collectors.toUnmodifiableMap(
                    MessageType::toProxyTag,
                    messageType -> messageType
            ));

    public String toProxyTag() {
        return "FlectonePulse:" + this.name();
    }

    public static MessageType fromProxyString(String string) {
        if (string == null || string.isEmpty()) return null;

        return ENUM_BY_PROXY_KEY.get(string);
    }
}
