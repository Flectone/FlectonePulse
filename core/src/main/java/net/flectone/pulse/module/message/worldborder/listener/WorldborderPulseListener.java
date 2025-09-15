package net.flectone.pulse.module.message.worldborder.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.worldborder.WorldborderModule;
import net.flectone.pulse.module.message.worldborder.extractor.WorldborderExtractor;
import net.flectone.pulse.module.message.worldborder.model.Worldborder;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class WorldborderPulseListener implements PulseListener {

    private final WorldborderExtractor worldborderExtractor;
    private final WorldborderModule worldborderModule;

    @Inject
    public WorldborderPulseListener(WorldborderExtractor worldborderExtractor,
                                    WorldborderModule worldborderModule) {
        this.worldborderExtractor = worldborderExtractor;
        this.worldborderModule = worldborderModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_WORLDBORDER_DAMAGE_AMOUNT_SUCCESS, COMMANDS_WORLDBORDER_DAMAGE_BUFFER_SUCCESS,
                 COMMANDS_WORLDBORDER_GET, COMMANDS_WORLDBORDER_SET_IMMEDIATE,
                 COMMANDS_WORLDBORDER_WARNING_DISTANCE_SUCCESS, COMMANDS_WORLDBORDER_WARNING_TIME_SUCCESS -> {
                Optional<Worldborder> worldborder = worldborderExtractor.extractValue(event.getTranslatableComponent());
                if (worldborder.isEmpty()) return;

                event.setCancelled(true);
                worldborderModule.send(event.getFPlayer(), translationKey, worldborder.get());
            }
            case COMMANDS_WORLDBORDER_CENTER_SUCCESS, COMMANDS_WORLDBORDER_SET_GROW,
                 COMMANDS_WORLDBORDER_SET_SHRINK -> {
                Optional<Worldborder> worldborder = worldborderExtractor.extractSecondValue(event.getTranslatableComponent());
                if (worldborder.isEmpty()) return;

                event.setCancelled(true);
                worldborderModule.send(event.getFPlayer(), translationKey, worldborder.get());
            }
        }
    }
}