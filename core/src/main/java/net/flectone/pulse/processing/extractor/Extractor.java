package net.flectone.pulse.processing.extractor;

import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class Extractor {

    public String extractTarget(TextComponent targetComponent) {
        String target = targetComponent.content();
        if (target.isEmpty()) {
            target = targetComponent.insertion();
        }

        return target == null ? "" : target;
    }

    public Optional<UUID> parseUUID(@Nullable String uuid) {
        if (StringUtils.isEmpty(uuid)) return Optional.empty();

        try {
            return Optional.of(UUID.fromString(uuid));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

}
