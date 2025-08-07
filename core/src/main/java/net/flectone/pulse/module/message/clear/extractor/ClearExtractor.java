package net.flectone.pulse.module.message.clear.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.clear.model.Clear;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class ClearExtractor {

    @Inject
    public ClearExtractor() {
    }

    public Optional<Clear> extract(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent.args().size() < 2) return Optional.empty();
        if (!(translatableComponent.args().get(0) instanceof TextComponent firstArg)) return Optional.empty();
        if (!(translatableComponent.args().get(1) instanceof TextComponent secondArg)) return Optional.empty();

        Clear clear;
        if (event.getTranslationKey() == MinecraftTranslationKey.COMMANDS_CLEAR_SUCCESS) {
            clear = new Clear(secondArg.content(), firstArg.content());
        } else {
            clear = new Clear(firstArg.content(), secondArg.content());
        }

        return Optional.of(clear);
    }

}
