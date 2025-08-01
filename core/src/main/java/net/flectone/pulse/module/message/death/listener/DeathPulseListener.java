package net.flectone.pulse.module.message.death.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.death.DeathModule;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.module.message.death.model.Item;
import net.flectone.pulse.util.EntityUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.UUID;

@Singleton
public class DeathPulseListener implements PulseListener {

    private final DeathModule deathModule;
    private final EntityUtil entityUtil;

    @Inject
    public DeathPulseListener(DeathModule deathModule,
                              EntityUtil entityUtil) {
        this.deathModule = deathModule;
        this.entityUtil = entityUtil;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        if (!event.getKey().startsWith("death.")) return;

        TranslatableComponent translatableComponent = event.getComponent();
        Death death = getDeath(translatableComponent, 0);
        if (death == null) return;

        Death killer = getDeath(translatableComponent, 1);
        death.setKiller(killer);

        Item item = getItem(translatableComponent);
        death.setItem(item);

        event.cancelPacket();
        deathModule.send(event.getFPlayer(), death);
    }

    private Item getItem(TranslatableComponent translatableComponent) {
        if (translatableComponent.args().size() < 3) return null;

        Component itemComp = translatableComponent.args().get(2);
        String itemName = switch (itemComp) {
            // format "chat.square_brackets"
            case TranslatableComponent transComp when transComp.key().equals("chat.square_brackets")
                    && !transComp.args().isEmpty() -> processItemComponent(transComp.args().get(0));
            // legacy format extra
            case TextComponent textComp when textComp.content().equals("[")
                    && !textComp.children().isEmpty() -> processItemComponent(textComp.children().get(0));
            default -> null;
        };

        if (itemName == null || itemName.isEmpty()) return null;

        Item item = new Item(itemName);
        item.setHoverEvent(HoverEvent.showText(Component.text(itemName).decorate(TextDecoration.ITALIC)));
        return item;
    }

    private String processItemComponent(Component component) {
        if (component instanceof TextComponent extraText && !extraText.children().isEmpty()) {
            Component itemText = extraText.children().get(0);
            if (itemText instanceof TextComponent itemTextComp) {
                return itemTextComp.content();
            }
        }

        return null;
    }

    private Death getDeath(TranslatableComponent translatableComponent, int index) {
        if (translatableComponent.args().size() < index + 1) return null;

        return switch (translatableComponent.args().get(index)) {
            case TranslatableComponent targetComponent -> {
                Death death = new Death(translatableComponent.key());
                death.setTargetName(targetComponent.key());
                death.setPlayer(false);

                String insertion = targetComponent.insertion();
                if (insertion != null && !insertion.isEmpty()) {
                    try {
                        death.setTargetUUID(UUID.fromString(insertion));
                    } catch (IllegalArgumentException e) {
                        // null
                    }
                }

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

                    if (insertion != null && !insertion.isEmpty()) {
                        try {
                            UUID uuid = UUID.fromString(insertion);
                            death.setTargetUUID(uuid);
                            death.setPlayer(false);
                            death.setTargetType(target);
                            yield death;
                        } catch (IllegalArgumentException e) {
                            // invalid UUID
                        }
                    }
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

                    if (!hoverComponent.children().isEmpty() && hoverComponent.children().get(0) instanceof TextComponent) {
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
}
