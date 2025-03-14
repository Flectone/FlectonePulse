package net.flectone.pulse.module.message.death.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeathCombatEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.death.DeathModule;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.module.message.death.model.Item;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;

@Singleton
public class DeathPacketListener extends AbstractPacketListener {

    private final DeathModule deathModule;

    @Inject
    public DeathPacketListener(DeathModule deathModule) {
        this.deathModule = deathModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (!deathModule.getMessage().isEnable()) return;

        if (event.getPacketType() == PacketType.Play.Server.DEATH_COMBAT_EVENT) {

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
                death.setTargetType("entity.minecraft." + showEntity.type().key().value());
            }

            if (hoverComponent.children().isEmpty()) return death;
            if (!(hoverComponent.children().get(0) instanceof TextComponent textComponent)) return death;

            death = new Death(translatableComponent.key());
            death.setTargetName(textComponent.content());
            death.setTargetUUID(showEntity.id());
            death.setTargetType("entity.minecraft." + showEntity.type().key().value());
            return death;
        }

        return null;
    }
}
