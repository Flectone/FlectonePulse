package net.flectone.pulse.module.message.debugstick.model;

import org.jetbrains.annotations.Nullable;

public record DebugStick(String name, @Nullable String value) {
}