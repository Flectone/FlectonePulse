package net.flectone.pulse.module.message.teleport.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.teleport.TeleportModule;
import net.flectone.pulse.module.message.teleport.extractor.TeleportExtractor;
import net.flectone.pulse.module.message.teleport.model.TeleportEntity;
import net.flectone.pulse.module.message.teleport.model.TeleportLocation;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class TeleportPulseListener implements PulseListener {

    private final TeleportModule teleportModule;
    private final TeleportExtractor teleportExtractor;

    @Inject
    public TeleportPulseListener(TeleportModule teleportModule,
                                 TeleportExtractor teleportExtractor) {
        this.teleportModule = teleportModule;
        this.teleportExtractor = teleportExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("commands.teleport.success")) return;

        switch (translationKey) {
            case COMMANDS_TELEPORT_SUCCESS_ENTITY_MULTIPLE, COMMANDS_TELEPORT_SUCCESS_ENTITY_SINGLE -> {
                Optional<TeleportEntity> optionalTeleportEntity = teleportExtractor.extractEntity(translationKey, event.getTranslatableComponent());
                if (optionalTeleportEntity.isEmpty()) return;

                event.setCancelled(true);
                teleportModule.send(event.getFPlayer(), translationKey, optionalTeleportEntity.get());
            }
            case COMMANDS_TELEPORT_SUCCESS_LOCATION_MULTIPLE, COMMANDS_TELEPORT_SUCCESS_LOCATION_SINGLE -> {
                Optional<TeleportLocation> optionalTeleportLocation = teleportExtractor.extractLocation(translationKey, event.getTranslatableComponent());
                if (optionalTeleportLocation.isEmpty()) return;

                event.setCancelled(true);
                teleportModule.send(event.getFPlayer(), translationKey, optionalTeleportLocation.get());
            }
        }
    }
}
