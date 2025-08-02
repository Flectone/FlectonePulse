package net.flectone.pulse.module.message.spawn.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.spawn.SpawnModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.List;

@Singleton
public class SpawnPulseListener implements PulseListener {

    private final SpawnModule spawnModule;

    @Inject
    public SpawnPulseListener(SpawnModule spawnModule) {
        this.spawnModule = spawnModule;
    }


    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        if (event.getKey() == MinecraftTranslationKey.BLOCK_MINECRAFT_SET_SPAWN) {
            event.cancelPacket();
            spawnModule.send(event.getFPlayer(), event.getKey());
            return;
        }

        if (!event.getKey().startsWith("commands.spawnpoint.success")) return;

        TranslatableComponent translatableComponent = event.getComponent();
        List<Component> translationArguments = translatableComponent.args();
        if (translationArguments.size() < 4) return;

        Component targetComponent;
        Component xComponent;
        Component yComponent;
        Component zComponent;
        String angle = "";
        String world = "";

        if (event.getKey() == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS) {
            // legacy format, player first
            targetComponent = translationArguments.get(0);
            xComponent = translationArguments.get(1);
            yComponent = translationArguments.get(2);
            zComponent = translationArguments.get(3);
        } else {
            // coordinates first, player last
            xComponent = translationArguments.get(0);
            yComponent = translationArguments.get(1);
            zComponent = translationArguments.get(2);
            targetComponent = translationArguments.getLast();

            // check for optional angle and world
            if (translationArguments.size() >= 5 && translationArguments.get(3) instanceof TextComponent angleComponent) {
                angle = angleComponent.content();
            }

            if (translationArguments.size() >= 6 && translationArguments.get(4) instanceof TextComponent worldComponent) {
                world = worldComponent.content();
            }
        }

        if (!(xComponent instanceof TextComponent xComp)) return;
        if (!(yComponent instanceof TextComponent yComp)) return;
        if (!(zComponent instanceof TextComponent zComp)) return;
        if (!(targetComponent instanceof TextComponent tgtComp)) return;

        String x = xComp.content();
        String y = yComp.content();
        String z = zComp.content();
        String value = tgtComp.content();

        event.cancelPacket();
        spawnModule.send(event.getFPlayer(), event.getKey(), x, y, z, angle, world, value);
    }

}
