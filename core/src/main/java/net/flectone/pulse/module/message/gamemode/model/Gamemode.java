package net.flectone.pulse.module.message.gamemode.model;

import net.flectone.pulse.util.constant.MinecraftTranslationKey;

public record Gamemode(MinecraftTranslationKey key, String type, String target) {
}
