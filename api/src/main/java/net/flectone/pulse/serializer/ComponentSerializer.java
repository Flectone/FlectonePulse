package net.flectone.pulse.serializer;

import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

/**
 * Converts between components and the text forms the platform and protocol use.
 * @author TheFaser
 */
public interface ComponentSerializer {

    /**
     * Serializes a component to MiniMessage text.
     *
     * @param component the component
     * @return the serialized text
     */
    @NonNull String toStandard(@NonNull Component component);

    /**
     * Serializes a component to plain text with all formatting stripped.
     *
     * @param component the component
     * @return the serialized text
     */
    @NonNull String toPlain(@NonNull Component component);

    /**
     * Serializes a component to text with legacy section-sign color codes.
     *
     * @param component the component
     * @return the serialized text
     */
    @NonNull String toLegacy(@NonNull Component component);

    /**
     * Serializes a component to the JSON form the protocol uses.
     *
     * @param component the component
     * @return the serialized text
     */
    @NonNull String toJson(@NonNull Component component);

    /**
     * Serializes a component to a JSON tree, for callers that go on to edit it.
     *
     * @param component the component
     * @return the JSON tree
     */
    @NonNull Object toJsonTree(@NonNull Component component);

    /**
     * Serializes a refusal message into the JSON the login disconnect packet expects.
     *
     * @param playerPreLoginEventWithKickReason the refused login
     * @return the JSON text
     */
    @NonNull String toJson(@NonNull PlayerPreLoginEvent playerPreLoginEventWithKickReason);

    /**
     * Parses a component from MiniMessage text.
     *
     * @param string the text
     * @return the component
     */
    @NonNull Component fromStandard(@NonNull String string);

    /**
     * Parses a component from plain text.
     *
     * @param string the text
     * @return the component
     */
    @NonNull Component fromPlain(@NonNull String string);

    /**
     * Parses a component from text with legacy section-sign color codes.
     *
     * @param string the text
     * @return the component
     */
    @NonNull Component fromLegacy(@NonNull String string);

    /**
     * Parses a component from the JSON form the protocol uses.
     *
     * @param string the text
     * @return the component
     */
    @NonNull Component fromJson(String string);

}
