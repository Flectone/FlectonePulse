package net.flectone.pulse.processing.extractor;

import com.google.inject.Inject;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.EntityUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Triplet;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class Extractor {

    @Inject private EntityUtil entityUtil;
    @Inject private FPlayerService fPlayerService;

    public String extractTarget(TextComponent targetComponent) {
        String target = targetComponent.content();
        if (target.isEmpty()) {
            target = targetComponent.insertion();
        }

        return target == null ? "" : target;
    }

    public Optional<UUID> extractUUID(@Nullable String uuid) {
        if (StringUtils.isEmpty(uuid)) return Optional.empty();

        try {
            return Optional.of(UUID.fromString(uuid));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public Optional<FEntity> extractFEntity(Component component) {
        UUID uuid = null;
        String content;

        if (component instanceof TranslatableComponent translatableComponent) {
            Optional<UUID> optionalUUID = extractUUID(translatableComponent.insertion());
            if (optionalUUID.isPresent()) {
                uuid = optionalUUID.get();
            }

            content = translatableComponent.key();
        } else if (component instanceof TextComponent textComponent) {
            content = textComponent.content();
        } else return Optional.empty();

        Triplet<String, String, UUID> triplet = extractHoverComponent(content, content, uuid, component.hoverEvent());
        String name = triplet.first();
        if (name.isEmpty()) return Optional.empty();

        String type = triplet.second();
        uuid = triplet.third();

        FEntity entity = new FEntity(name, uuid, type);
        return Optional.of(entity);
    }

    public Triplet<String, String, UUID> extractHoverComponent(String name, String type, @Nullable UUID uuid, HoverEvent<?> hoverEvent) {
        if (hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_ENTITY) {
            HoverEvent.ShowEntity showEntity = (HoverEvent.ShowEntity) hoverEvent.value();
            uuid = showEntity.id();
            type = entityUtil.resolveEntityTranslationKey(showEntity.type().key().value());
            if (showEntity.name() instanceof TextComponent hoverText) {
                name = hoverText.content();
            } else if (showEntity.name() instanceof TranslatableComponent hoverTranslatable) {
                name = hoverTranslatable.key();
            }
        } else if (uuid == null) {
            uuid = fPlayerService.getFPlayer(name).getUuid();
        }

        return Triplet.of(name, type, uuid);
    }

}
