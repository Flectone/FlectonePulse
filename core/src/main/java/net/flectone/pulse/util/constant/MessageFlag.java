package net.flectone.pulse.util.constant;

public enum MessageFlag {

    USER_MESSAGE(false),
    MENTION(false),
    REPLACEMENT(true),
    FIXATION(true),
    QUESTION(true),
    SPOILER(true),
    TRANSLATE(false),
    TRANSLATE_ITEM(true),
    SWEAR(true),
    CAPS(true),
    DELETE(true),
    FLOOD(true),
    FORMATTING(true),
    URL(true),
    IMAGE(true),
    COLORS(true),
    INTERACTIVE_CHAT(true);

    private final boolean defaultValue;

    MessageFlag(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }
}
