package net.flectone.pulse.module.message.spawn.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.spawn.SpawnModule;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.List;

@Singleton
public class SpawnpointPacketListener extends AbstractPacketListener {

    private final SpawnModule spawnModule;

    @Inject
    public SpawnpointPacketListener(SpawnModule spawnModule) {
        this.spawnModule = spawnModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKeys key = MinecraftTranslationKeys.fromString(translatableComponent.key());
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("commands.spawnpoint.success")) return;

        List<Component> translationArguments = translatableComponent.args();
        if (translationArguments.size() < 6) return;
        if (!spawnModule.isEnable()) return;

        if (!(translationArguments.get(0) instanceof TextComponent xComponent)) return;
        String x = xComponent.content();

        if (!(translationArguments.get(1) instanceof TextComponent yComponent)) return;
        String y = yComponent.content();

        if (!(translationArguments.get(2) instanceof TextComponent zComponent)) return;
        String z = zComponent.content();

        if (!(translationArguments.get(3) instanceof TextComponent angleComponent)) return;
        String angle = angleComponent.content();

        if (!(translationArguments.get(4) instanceof TextComponent worldComponent)) return;
        String world = worldComponent.content();

        if (!(translationArguments.get(5) instanceof TextComponent targetComponent)) return;
        String value = targetComponent.content();

        event.setCancelled(true);
        spawnModule.send(event.getUser().getUUID(), key, x, y, z, angle, world, value);
    }
}
