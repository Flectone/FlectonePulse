package net.flectone.pulse.module.message.clear.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.clear.model.Clear;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class ClearExtractor extends Extractor {

    @Inject
    public ClearExtractor() {
    }

    public Optional<Clear> extract(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent.args().size() < 2) return Optional.empty();
        if (!(translatableComponent.args().get(0) instanceof TextComponent firstArg)) return Optional.empty();
        if (!(translatableComponent.args().get(1) instanceof TextComponent secondArg)) return Optional.empty();

        String count;
        String value;
        if (event.getTranslationKey() == MinecraftTranslationKey.COMMANDS_CLEAR_SUCCESS) {
            count = secondArg.content();
            value = extractTarget(firstArg);
        } else {
            count = firstArg.content();
            value = extractTarget(secondArg);
        }

        Clear clear = new Clear(count, value);
        return Optional.of(clear);
    }

}
