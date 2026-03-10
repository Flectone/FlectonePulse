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
     * Enables Caps module processing.
     */
    CAPS_MODULE(true),

    /**
     * Controls message color processing.
     * If enabled, OUT colors are applied from the sender.
     * If disabled, OUT colors are applied from the recipient.
     */
    COLOR_CONTEXT_SENDER(true),

    /**
     * Enables Delete module processing.
     */
    DELETE_MODULE(true),

    /**
     * Enables Fixation module processing.
     */
    FIXATION_MODULE(true),

    /**
     * Enables Flood module processing.
     */
    FLOOD_MODULE(true),

    /**
     * Provides InteractiveChat plugin compatibility.
     */
    INTERACTIVE_CHAT_COMPAT(true),

    /**
     * Enables detection of invisible player names.
     */
    INVISIBLE_NAME_DETECTION(true),

    /**
     * Enables item placeholder detection and processing.
     */
    ITEM_DETECTION(true),

    /**
     * Maintains legacy color code compatibility.
     * Handles conversion between old and new color systems.
     */
    LEGACY_COLOR_CONVERSION(true),

    /**
     * Enables mention detection and notifications.
     */
    MENTION_MODULE(true),

    /**
     * Enables Nickname module processing.
     */
    NICKNAME_MODULE(true),

    /**
     * Object will be replaced with default.
     */
    OBJECT_DEFAULT_VALUE(false),

    /**
     * Enables player_head placeholder processing.
     */
    OBJECT_PLAYER_HEAD_PROCESSING(true),

    /**
     * Enables object incompatible receiver detection.
     * (player_head, sprite and texture)
     */
    OBJECT_RECEIVER_VALIDATION(true),

    /**
     * Enables sprite placeholder processing.
     */
    OBJECT_SPRITE_PROCESSING(true),

    /**
     * Enables texture placeholder processing.
     */
    OBJECT_TEXTURE_PROCESSING(true),

    /**
     * Controls how integration placeholders (e.g., PlaceholderAPI) are processed.
     * If enabled, placeholders are processed on behalf of the sender.
     * If disabled, placeholders are processed on behalf of the recipient.
     */
    PLACEHOLDER_CONTEXT_SENDER(true),

    /**
     * True for player messages, false for system messages.
     * Controls whether full user processing is applied.
     */
    PLAYER_MESSAGE(false),

    /**
     * Enables QuestionAnswer module processing.
     */
    QUESTIONANSWER_MODULE(true),

    /**
     * Replaces missing tags with empty content in messages.
     */
    REMOVE_DISABLED_TAGS(true),

    /**
     * Enables FlectonePulse placeholder replacement (%item%, %skin%, etc.).
     */
    REPLACEMENT_MODULE(true),

    /**
     * Enables Swear module processing.
     */
    SWEAR_MODULE(true),

    /**
     * Enables Translate module processing.
     */
    TRANSLATE_MODULE(true),

    /**
     * Enables URL detection and processing.
     */
    URL_PROCESSING(true);

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