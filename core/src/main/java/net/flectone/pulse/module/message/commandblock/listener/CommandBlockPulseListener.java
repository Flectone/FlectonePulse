package net.flectone.pulse.module.message.commandblock.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.commandblock.CommandblockModule;
import net.flectone.pulse.module.message.commandblock.extractor.CommandBlockExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class CommandBlockPulseListener implements PulseListener {

    private final CommandblockModule cloneModule;
    private final CommandBlockExtractor clearExtractor;

    @Inject
    public CommandBlockPulseListener(CommandblockModule cloneModule,
                                     CommandBlockExtractor clearExtractor) {
        this.cloneModule = cloneModule;
        this.clearExtractor = clearExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey != MinecraftTranslationKey.ADV_MODE_NOT_ENABLED
                && translationKey != MinecraftTranslationKey.ADV_MODE_SET_COMMAND_SUCCESS) return;

        Optional<String> optionalAmount = clearExtractor.extract(event.getTranslatableComponent());

        event.setCancelled(true);
        cloneModule.send(event.getFPlayer(), translationKey, optionalAmount.orElse(null));
    }

}