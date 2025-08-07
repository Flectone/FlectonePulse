package net.flectone.pulse.module.message.sleep.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.sleep.model.Sleep;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class SleepExtractor {

    @Inject
    public SleepExtractor() {
    }

    public Optional<Sleep> extract(MessageReceiveEvent event) {
        String sleepCount = "";
        String allCount = "";

        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (event.getTranslationKey() == MinecraftTranslationKey.SLEEP_PLAYERS_SLEEPING && translatableComponent.args().size() == 2) {
            if ((translatableComponent.args().get(0) instanceof TextComponent sleepComponent)) {
                sleepCount = sleepComponent.content();
            }
            if ((translatableComponent.args().get(1) instanceof TextComponent allComponent)) {
                allCount = allComponent.content();
            }
        }

        Sleep sleep = new Sleep(sleepCount, allCount);
        return Optional.of(sleep);
    }

}
