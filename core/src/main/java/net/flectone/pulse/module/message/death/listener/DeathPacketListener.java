package net.flectone.pulse.module.message.death.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeathCombatEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.death.DeathModule;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class DeathPacketListener implements PacketListener {

    private final DeathModule deathModule;

    @Inject
    public DeathPacketListener(DeathModule deathModule) {
        this.deathModule = deathModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (!deathModule.getMessage().isEnable()) return;
        if (event.getPacketType() != PacketType.Play.Server.DEATH_COMBAT_EVENT) return;

            WrapperPlayServerDeathCombatEvent wrapperPlayServerDeathCombatEvent = new WrapperPlayServerDeathCombatEvent(event);
            if (wrapperPlayServerDeathCombatEvent.getDeathMessage() instanceof TranslatableComponent) {
                event.setCancelled(true);
            }

            return;
        }

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKeys key = MinecraftTranslationKeys.fromString(translatableComponent.key());
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("death.")) return;

        Death death = getDeath(translatableComponent, 0);
        if (death == null) return;

        Death killer = getDeath(translatableComponent, 1);
        death.setKiller(killer);

        Item item = getItem(translatableComponent);
        death.setItem(item);

        event.setCancelled(true);

        deathModule.send(event.getUser().getUUID(), death);
    }

    private Item getItem(TranslatableComponent translatableComponent) {
        if (translatableComponent.args().size() < 3) return null;

        if (!(translatableComponent.args().get(2) instanceof TranslatableComponent itemComponent)) return null;

        if (itemComponent.args().isEmpty()) return null;
        if (!(itemComponent.args().get(0) instanceof TextComponent itemTextComponent)) return null;

        if (itemTextComponent.children().isEmpty()) return null;
        if (!(itemTextComponent.children().get(0) instanceof TextComponent itemTextTextComponent)) return null;

        Item item;

        if (itemTextTextComponent.content().isEmpty()) {
            if (itemTextTextComponent.children().isEmpty()) return null;
            if (!(itemTextTextComponent.children().get(0) instanceof TextComponent itemTextTextTextComponent)) return null;

            item = new Item(itemTextTextTextComponent.content());

            // wait for fix from PacketEvents
            // item.setHoverEvent(itemComponent.hoverEvent());
        } else {
            item = new Item(itemTextTextComponent.content());

            // wait for fix from PacketEvents
            // item.setHoverEvent(itemComponent.hoverEvent());
        }

        item.setHoverEvent(HoverEvent.showText(Component.text(item.getName()).decorate(TextDecoration.ITALIC)));

        return item;
    }

    private Death getDeath(TranslatableComponent translatableComponent, int index) {
        if (translatableComponent.args().size() < index + 1) return null;

        if (translatableComponent.args().get(index) instanceof TranslatableComponent targetComponent) {
            HoverEvent<?> hoverEvent = targetComponent.hoverEvent();
            if (hoverEvent == null) return null;
            if (!(hoverEvent.value() instanceof HoverEvent.ShowEntity showEntity)) return null;

            Death death = new Death(translatableComponent.key());
            death.setTargetName(targetComponent.key());
            death.setTargetUUID(showEntity.id());
            return death;
        }

        if (translatableComponent.args().get(index) instanceof TextComponent targetComponent) {
            String target = targetComponent.content();

            Death death = null;

            if (!target.isEmpty()) {
                death = new Death(translatableComponent.key());
                death.setTargetName(target);
                death.setPlayer(true);
            }

            HoverEvent<?> hoverEvent = targetComponent.hoverEvent();
            if (hoverEvent == null) return death;

            HoverEvent.ShowEntity showEntity = (HoverEvent.ShowEntity) hoverEvent.value();
            if (!(showEntity.name() instanceof TextComponent hoverComponent)) return death;
            if (death != null && !showEntity.type().value().equalsIgnoreCase("player")) {
                death.setPlayer(false);
                death.setTargetUUID(showEntity.id());
                death.setTargetType(entityUtil.resolveEntityTranslationKey(showEntity.type().key().value()));
            }

            if (hoverComponent.children().isEmpty()) return death;
            if (!(hoverComponent.children().get(0) instanceof TextComponent)) return death;

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
            return death;
        }

        return null;
    }
}
