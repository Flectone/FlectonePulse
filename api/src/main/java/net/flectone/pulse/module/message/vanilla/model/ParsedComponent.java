package net.flectone.pulse.module.message.vanilla.model;

import net.flectone.pulse.config.Message;

import java.util.Map;

/**
 * A vanilla message broken into the pieces the plugin needs to re-render it.
 *
 * @param translationKey the vanilla translation key
 * @param vanillaMessage the matching config entry
 * @param arguments the translation arguments, keyed by position
 * @author TheFaser
 */
public record ParsedComponent(
        String translationKey,
        Message.Vanilla.VanillaMessage vanillaMessage,
        Map<Integer, Object> arguments
) {
}
