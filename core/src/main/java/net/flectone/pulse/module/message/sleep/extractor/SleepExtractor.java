package net.flectone.pulse.module.message.sleep.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.sleep.model.Sleep;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class SleepExtractor extends Extractor {

    @Inject
    public SleepExtractor() {
    }

    public Optional<Sleep> extract(MessageReceiveEvent event) {
        String sleepCount = "";
        String allCount = "";

        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (event.getTranslationKey() == MinecraftTranslationKey.SLEEP_PLAYERS_SLEEPING && translatableComponent.arguments().size() == 2) {
            if ((translatableComponent.arguments().getFirst().asComponent() instanceof TextComponent sleepComponent)) {
                sleepCount = sleepComponent.content();
            }
            if ((translatableComponent.arguments().get(1).asComponent() instanceof TextComponent allComponent)) {
                allCount = allComponent.content();
            }
        }

        Sleep sleep = new Sleep(sleepCount, allCount);
        return Optional.of(sleep);
    }

}
