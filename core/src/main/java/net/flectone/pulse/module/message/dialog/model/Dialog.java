package net.flectone.pulse.module.message.dialog.model;

import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

public record Dialog(@Nullable FEntity target, @Nullable String count) {
}