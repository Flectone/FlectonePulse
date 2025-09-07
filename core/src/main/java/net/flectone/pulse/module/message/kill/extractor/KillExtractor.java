package net.flectone.pulse.module.message.kill.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.kill.model.Kill;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.EntityUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.incendo.cloud.type.tuple.Triplet;

import java.util.Optional;
import java.util.UUID;

@Singleton
public class KillExtractor extends Extractor {

    private final EntityUtil entityUtil;
    private final FPlayerService fPlayerService;

    @Inject
    public KillExtractor(EntityUtil entityUtil,
                         FPlayerService fPlayerService) {
        this.entityUtil = entityUtil;
        this.fPlayerService = fPlayerService;
    }

    public Optional<Kill> extractMultipleKill(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (!(translatableComponent.arguments().getFirst().asComponent() instanceof TextComponent firstArgument)) return Optional.empty();

        String value = firstArgument.content();
        Kill kill = new Kill(value, null);
        return Optional.of(kill);
    }

    public Optional<Kill> extractSingleKill(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent.arguments().isEmpty()) return Optional.empty();

        Component firstArgument = translatableComponent.arguments().getFirst().asComponent();
        UUID uuid = null;

        String content = switch (firstArgument) {
            case TranslatableComponent translatableArg -> {
                Optional<UUID> optionalUUID = parseUUID(translatableArg.insertion());
                if (optionalUUID.isPresent()) {
                    uuid = optionalUUID.get();
                }

                yield translatableArg.key();
            }
            case TextComponent textArg -> textArg.content();
            default -> null;
        };

        if (content == null) return Optional.empty();

        Triplet<String, String, UUID> triplet = extractHoverComponent(content, content, uuid, firstArgument.hoverEvent());
        String name = triplet.first();
        if (name.isEmpty()) return Optional.empty();

        String type = triplet.second();
        uuid = triplet.third();

        event.setCancelled(true);
        FEntity fEntity = new FEntity(name, uuid, type);
        Kill kill = new Kill("", fEntity);
        return Optional.of(kill);
    }

    private Triplet<String, String, UUID> extractHoverComponent(String name, String type, UUID uuid, HoverEvent<?> hoverEvent) {
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
