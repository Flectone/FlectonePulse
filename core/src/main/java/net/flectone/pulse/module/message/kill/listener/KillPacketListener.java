package net.flectone.pulse.module.message.kill.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.message.kill.KillModule;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.UUID;

@Singleton
public class KillPacketListener extends AbstractPacketListener {

    private final KillModule killModule;

    @Inject
    public KillPacketListener(KillModule killModule) {
        this.killModule = killModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKeys key = MinecraftTranslationKeys.fromString(translatableComponent.key());
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("commands.kill.success")) return;
        if (!killModule.isEnable()) return;

        switch (key) {
            case COMMANDS_KILL_SUCCESS_MULTIPLE -> handleKillMultiple(event, translatableComponent);
            case COMMANDS_KILL_SUCCESS_SINGLE -> handleKillSingle(event, translatableComponent);
        }
    }

    private void handleKillMultiple(PacketSendEvent event, TranslatableComponent translatableComponent) {
        if (!(translatableComponent.args().get(0) instanceof TextComponent firstArgument)) return;

        String value = firstArgument.content();

        event.setCancelled(true);
        killModule.send(event.getUser().getUUID(), MinecraftTranslationKeys.COMMANDS_KILL_SUCCESS_MULTIPLE, value, null);
    }

    private void handleKillSingle(PacketSendEvent event, TranslatableComponent translatableComponent) {
        HoverEvent<?> hoverEvent = null;
        String type = "";
        if (translatableComponent.args().get(0) instanceof TextComponent firstArgument) {
            hoverEvent = firstArgument.hoverEvent();
        } else if (translatableComponent.args().get(0) instanceof TranslatableComponent firstArgument)  {
            hoverEvent = firstArgument.hoverEvent();
            type = firstArgument.key();
        }

        if (hoverEvent == null) return;
        HoverEvent.ShowEntity showEntity = (HoverEvent.ShowEntity) hoverEvent.value();
        if (type.isEmpty()) {
            type = showEntity.type().key().value();

            ItemType itemType = ItemTypes.getByName(type);

            type = itemType == null
                    ? "entity.minecraft." + type
                    : itemType.getPlacedType() == null ? "item.minecraft." + type : "block.minecraft." + type;
        }

        String name;
        if (showEntity.name() instanceof TextComponent hoverComponent) {
            name = hoverComponent.content();
        } else if (showEntity.name() instanceof TranslatableComponent hoverComponent) {
            name = hoverComponent.key();
        } else return;

        UUID uuid = showEntity.id();
        FEntity fEntity = new FEntity(name, uuid, type);

        event.setCancelled(true);
        killModule.send(event.getUser().getUUID(), MinecraftTranslationKeys.COMMANDS_KILL_SUCCESS_SINGLE, "", fEntity);
    }
}
