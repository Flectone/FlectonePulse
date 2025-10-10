package net.flectone.pulse.module.message.difficulty.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.difficulty.DifficultyModule;
import net.flectone.pulse.module.message.difficulty.extractor.DifficultyExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DifficultyPulseListener implements PulseListener {

    private final DifficultyModule difficultyModule;
    private final DifficultyExtractor difficultyExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_DIFFICULTY_QUERY
                && event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_DIFFICULTY_SUCCESS) return;

        Optional<String> optionalTime = difficultyExtractor.extract(event.getTranslatableComponent());
        if (optionalTime.isEmpty()) return;

        event.setCancelled(true);
        difficultyModule.send(event.getFPlayer(), event.getTranslationKey(), optionalTime.get());
    }

}
