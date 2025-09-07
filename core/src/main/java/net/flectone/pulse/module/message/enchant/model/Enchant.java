package net.flectone.pulse.module.message.enchant.model;

import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

public record Enchant(String name, String level, String count, @Nullable FEntity entity) {
}
