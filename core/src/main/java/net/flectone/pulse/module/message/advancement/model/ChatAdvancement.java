package net.flectone.pulse.module.message.advancement.model;

import net.flectone.pulse.constant.MinecraftTranslationKey;

public record ChatAdvancement(String owner,
                              String title,
                              String description,
                              MinecraftTranslationKey type) {
}
