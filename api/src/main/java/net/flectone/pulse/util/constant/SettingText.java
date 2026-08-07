package net.flectone.pulse.util.constant;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The per-player settings stored as text, such as the chosen locale or chat.
 * @author TheFaser
 */
public enum SettingText {

    CHAT_NAME,
    LOCALE,
    WORLD_PREFIX,
    STREAM_PREFIX,
    SERVER,
    SPY_STATUS,
    VANISH_STATUS,
    AFK_SUFFIX,
    NICKNAME;

    private static final Map<String, SettingText> ENUM_BY_KEY = Arrays.stream(SettingText.values())
            .collect(Collectors.toUnmodifiableMap(
                    Enum::name,
                    settingText -> settingText
            ));


    /**
     * Maps a stored setting name back to its constant, ignoring case.
     *
     * @param string the stored name
     * @return the matching setting, or null if the name is unknown or empty
     */
    public static @Nullable SettingText fromString(String string) {
        if (string == null || string.isEmpty()) return null;

        return ENUM_BY_KEY.get(string.toUpperCase());
    }

}
