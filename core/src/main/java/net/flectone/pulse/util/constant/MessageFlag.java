package net.flectone.pulse.util.constant;

public enum MessageFlag {

    USER_MESSAGE(false),
    SENDER_COLOR_OUT(true),
    MENTION(true),
    REPLACEMENT(true),
    REPLACE_DISABLED_TAGS(true),
    FIXATION(true),
    QUESTION(true),
    TRANSLATE(false),
    TRANSLATE_ITEM(true),
    SWEAR(true),
    CAPS(true),
    DELETE(true),
    FLOOD(true),
    ADVENTURE_TAGS(true),
    IMAGE(true),
    LEGACY_COLORS(true),
    INTERACTIVE_CHAT(true),
    INVISIBLE_NAME(true),
    OBJECT_PLAYER_HEAD(true),
    OBJECT_SPRITE(true);

    private final boolean defaultValue;

    MessageFlag(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }
}
