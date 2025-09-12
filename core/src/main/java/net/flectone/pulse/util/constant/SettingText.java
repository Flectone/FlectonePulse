package net.flectone.pulse.util.constant;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum SettingText {

    CHAT_NAME,
    LOCALE,
    WORLD_PREFIX,
    STREAM_PREFIX,
    SPY_STATUS,
    AFK_SUFFIX;

    private static final Map<String, SettingText> ENUM_BY_KEY = Arrays.stream(SettingText.values())
            .collect(Collectors.toUnmodifiableMap(
                    Enum::name,
                    settingText -> settingText
            ));

    @Nullable
    public static SettingText fromString(String string) {
        if (string == null || string.isEmpty()) return null;

        return ENUM_BY_KEY.get(string.toUpperCase());
    }

}
