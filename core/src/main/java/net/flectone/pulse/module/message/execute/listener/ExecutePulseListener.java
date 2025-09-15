package net.flectone.pulse.module.message.execute.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.execute.ExecuteModule;
import net.flectone.pulse.module.message.execute.extractor.ExecuteExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class ExecutePulseListener implements PulseListener {

    private final ExecuteModule executeModule;
    private final ExecuteExtractor executeExtractor;

    @Inject
    public ExecutePulseListener(ExecuteModule effectModule,
                                ExecuteExtractor effectExtractor) {
        this.executeModule = effectModule;
        this.executeExtractor = effectExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("commands.execute.conditional.pass")) return;

        Optional<String> optionalCount = executeExtractor.extract(event.getTranslatableComponent());

        event.setCancelled(true);
        executeModule.send(event.getFPlayer(), translationKey, optionalCount.orElse(null));
    }

}