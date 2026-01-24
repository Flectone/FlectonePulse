package net.flectone.pulse.module.message.vanilla.model;

import net.flectone.pulse.config.Message;

import java.util.Map;

public record ParsedComponent(String translationKey, Message.Vanilla.VanillaMessage vanillaMessage, Map<Integer, Object> arguments) {
}
