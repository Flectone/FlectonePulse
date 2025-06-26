package net.flectone.pulse.util;

import java.util.Arrays;

public enum MessageTag {
    CHAT,
    COMMAND_ME,
    COMMAND_BALL,
    COMMAND_BAN,
    COMMAND_BROADCAST,
    COMMAND_CHATCOLOR,
    COMMAND_CHATSTYLE,
    COMMAND_COIN,
    COMMAND_DICE,
    COMMAND_DO,
    COMMAND_HELPER,
    COMMAND_MUTE,
    COMMAND_UNBAN,
    COMMAND_UNMUTE,
    COMMAND_UNWARN,
    COMMAND_POLL_CREATE_MESSAGE,
    COMMAND_POLL_VOTE,
    COMMAND_SPY,
    COMMAND_STREAM,
    COMMAND_TELL,
    COMMAND_TRANSLATETO,
    COMMAND_TRY,
    COMMAND_WARN,
    COMMAND_KICK,
    COMMAND_TICTACTOE_CREATE,
    COMMAND_TICTACTOE_MOVE,
    COMMAND_ROCKPAPERSCISSORS_CREATE,
    COMMAND_ROCKPAPERSCISSORS_MOVE,
    COMMAND_ROCKPAPERSCISSORS_FINAL,
    FROM_DISCORD_TO_MINECRAFT,
    FROM_TWITCH_TO_MINECRAFT,
    FROM_TELEGRAM_TO_MINECRAFT,
    AFK,
    ADVANCEMENT,
    DEATH,
    JOIN,
    QUIT,

    // only for invalidation cache
    SYSTEM_ONLINE,
    SYSTEM_OFFLINE,
    SYSTEM_BAN,
    SYSTEM_MUTE,
    SYSTEM_WARN;

    public String toProxyTag() {
        return "FlectonePulse:" + this.name();
    }

    public static MessageTag fromProxyString(String string) {
        if (string == null) return null;

        return Arrays.stream(MessageTag.values())
                .filter(tag -> string.equals("FlectonePulse:" + tag.name()))
                .findAny()
                .orElse(null);
    }
}
