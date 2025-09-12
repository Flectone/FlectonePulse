package net.flectone.pulse.module.message.attribute.model;

import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

public record Attribute(FEntity target, String name, @Nullable String modifier, @Nullable String value) {
}