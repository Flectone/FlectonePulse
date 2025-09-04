package net.flectone.pulse.util.constant;

import java.util.Arrays;

public enum SettingText {

    CHAT_NAME,
    LOCALE,
    WORLD_PREFIX,
    STREAM_PREFIX,
    SPY_STATUS,
    AFK_SUFFIX;

    public static SettingText fromString(String string) {
        return Arrays.stream(SettingText.values())
                .filter(value -> string.equalsIgnoreCase(value.name()))
                .findAny()
                .orElse(null);
    }

}
