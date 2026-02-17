package net.flectone.pulse.util.constant;

/**
 * Message processing control flags for enabling/disabling features.
 * Used to control which modules process each message.
 *
 * @author TheFaser
 * @since 1.2.0
 */
public enum MessageFlag {

    /**
     * True for player messages, false for system messages.
     * Controls whether full user processing is applied.
     */
    USER_MESSAGE(false),

    /**
     * Controls how integration placeholders (e.g., PlaceholderAPI) are processed.
     * If enabled, placeholders are processed on behalf of the sender.
     * If disabled, placeholders are processed on behalf of the recipient.
     */
    SENDER_INTEGRATION_PLACEHOLDERS(true),

    /**
     * Controls message color processing.
     * If enabled, OUT colors are applied from the sender.
     * If disabled, OUT colors are applied from the recipient.
     */
    SENDER_COLOR_OUT(true),

    /**
     * Enables mention detection and notifications.
     */
    MENTION(true),

    /**
     * Enables FlectonePulse placeholder replacement (%item%, %skin%, etc.).
     */
    REPLACEMENT(true),

    /**
     * Replaces missing tags with empty content in messages.
     */
    REPLACE_DISABLED_TAGS(true),

    /**
     * Enables Fixation module processing.
     */
    FIXATION(true),

    /**
     * Enables QuestionAnswer module processing.
     */
    QUESTION(true),

    /**
     * Enables Translate module processing.
     */
    TRANSLATION(true),

    /**
     * Enables %item% placeholder processing.
     */
    TRANSLATE_ITEM(true),

    /**
     * Enables Swear module processing.
     */
    SWEAR(true),

    /**
     * Enables Caps module processing.
     */
    CAPS(true),

    /**
     * Enables Delete module processing.
     */
    DELETE(true),

    /**
     * Enables Flood module processing.
     */
    FLOOD(true),

    /**
     * Maintains legacy color code compatibility.
     * Handles conversion between old and new color systems.
     */
    LEGACY_COLORS(true),

    /**
     * Provides InteractiveChat plugin compatibility.
     */
    INTERACTIVE_CHAT(true),

    /**
     * Enables detection of invisible player names.
     */
    INVISIBLE_NAME(true),

    /**
     * Enables player_head placeholder processing.
     */
    OBJECT_PLAYER_HEAD(true),

    /**
     * Enables sprite module processing.
     */
    OBJECT_SPRITE(true),

    /**
     * Enables Nickname module processing.
     */
    NICKNAME(true);

    private final boolean defaultValue;

    /**
     * Creates a flag with its default enabled/disabled state.
     *
     * @param defaultValue true if enabled by default, false otherwise
     */
    MessageFlag(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the default state of this flag.
     *
     * @return true if enabled by default, false otherwise
     */
    public boolean getDefaultValue() {
        return defaultValue;
    }
}