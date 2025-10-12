package net.flectone.pulse.module.message.vanilla.model;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record Mapping(Predicate<ServerVersion> predicate, String newTranslationKey, @Nullable Mapping orElse) {
}
