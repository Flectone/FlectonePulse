package net.flectone.pulse.module.message.death.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.EntityUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Singleton
public class DeathExtractor extends Extractor {

    private final EntityUtil entityUtil;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Inject
    public DeathExtractor(EntityUtil entityUtil) {
        this.entityUtil = entityUtil;
    }

    public String extractItemName(TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().size() < 3) return null;

        Component itemComp = translatableComponent.arguments().get(2).asComponent();
        String itemName = switch (itemComp) {
            // format "chat.square_brackets"
            case TranslatableComponent transComp when transComp.key().equals("chat.square_brackets")
                    && !transComp.arguments().isEmpty() -> extractItemComponent(transComp.arguments().getFirst().asComponent());
            // legacy format extra
            case TextComponent textComp when textComp.content().equals("[")
                    && !textComp.children().isEmpty() -> extractItemComponent(textComp.children().getFirst().asComponent());
            default -> null;
        };

        if (StringUtils.isEmpty(itemName)) return null;

        return itemName;
    }

    public Death extractDeath(TranslatableComponent translatableComponent, int index) {
        if (translatableComponent.arguments().size() < index + 1) return null;

        return switch (translatableComponent.arguments().get(index).asComponent()) {
            case TranslatableComponent targetComponent -> {
                Death death = new Death(translatableComponent.key());
                death.setTargetName(targetComponent.key());
                death.setPlayer(false);

                extractUUID(targetComponent.insertion()).ifPresent(death::setTargetUUID);

                HoverEvent<?> hoverEvent = targetComponent.hoverEvent();
                if (hoverEvent != null && hoverEvent.value() instanceof HoverEvent.ShowEntity showEntity) {
                    death.setTargetUUID(showEntity.id());
                    death.setTargetType(entityUtil.resolveEntityTranslationKey(showEntity.type().key().value()));
                    if (showEntity.type().value().equalsIgnoreCase("player")) {
                        death.setPlayer(true);
                    }
                } else {
                    death.setTargetType(targetComponent.key());
                }

                yield death;
            }
            case TextComponent targetComponent -> {
                String target = targetComponent.content();
                String insertion = targetComponent.insertion();

                Death death = null;

                if (!target.isEmpty()) {
                    death = new Death(translatableComponent.key());
                    death.setTargetName(target);
                    death.setPlayer(true);

                    Optional<UUID> optionalUUID = extractUUID(insertion);
                    if (optionalUUID.isPresent()) {
                        death.setTargetUUID(optionalUUID.get());
                        death.setPlayer(false);
                        death.setTargetType(target);
                        yield death;
                    }
                } else if (!targetComponent.children().isEmpty()
                        && targetComponent.children().getFirst() instanceof TextComponent extraText
                        && !extraText.content().isEmpty()) {
                    death = new Death(translatableComponent.key());
                    death.setTargetName(extraText.content());
                    death.setPlayer(true);
                }

                HoverEvent<?> hoverEvent = targetComponent.hoverEvent();
                if (hoverEvent == null) yield death;

                if (hoverEvent.value() instanceof HoverEvent.ShowEntity showEntity) {
                    if (!(showEntity.name() instanceof TextComponent hoverComponent)) yield death;

                    if (death != null && !showEntity.type().value().equalsIgnoreCase("player")) {
                        death.setPlayer(false);
                        death.setTargetUUID(showEntity.id());
                        death.setTargetType(entityUtil.resolveEntityTranslationKey(showEntity.type().key().value()));
                    }

                    if (!hoverComponent.children().isEmpty() && hoverComponent.children().getFirst().asComponent() instanceof TextComponent) {
                        death = new Death(translatableComponent.key());
                        StringBuilder targetNameBuilder = new StringBuilder();
                        for (int i = 0; i < hoverComponent.children().size(); i++) {
                            if (hoverComponent.children().get(i) instanceof TextComponent textComponent) {
                                targetNameBuilder.append(textComponent.content());
                            }
                        }

                        death.setTargetName(targetNameBuilder.toString());
                        death.setTargetUUID(showEntity.id());
                        death.setTargetType(entityUtil.resolveEntityTranslationKey(showEntity.type().key().value()));
                        death.setPlayer(showEntity.type().value().equalsIgnoreCase("player"));
                    }
                }

                yield  death;
            }
            default -> null;
        };
    }

    private String extractItemComponent(Component component) {
        try {
            return miniMessage.serialize(component);
        } catch (Exception ignored) {
            if (component instanceof TextComponent extraText && !extraText.children().isEmpty()) {
                Component itemText = extraText.children().getFirst();
                if (itemText instanceof TextComponent itemTextComp) {
                    return itemTextComp.content();
                }
            }

            return null;
        }
    }

}
