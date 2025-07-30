package net.flectone.pulse.constant;

public enum MessageFlag {

    USER_MESSAGE(false),
    MENTION(false),
    EMOJI(true),
    FIXATION(true),
    QUESTION(true),
    SPOILER(true),
    TRANSLATE(false),
    TRANSLATE_ITEM(true),
    SWEAR(true),
    CAPS(true),
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
