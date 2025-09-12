package net.flectone.pulse.module.message.dialog.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.dialog.model.Dialog;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;

import java.util.List;
import java.util.Optional;

@Singleton
public class DialogExtractor extends Extractor {

    @Inject
    public DialogExtractor() {
    }

    public Optional<Dialog> extractSingle(TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.isEmpty()) return Optional.empty();

        Optional<FEntity> optionalFEntity = extractFEntity(translationArguments.getFirst().asComponent());
        if (optionalFEntity.isEmpty()) return Optional.empty();

        Dialog dialog = new Dialog(optionalFEntity.get(), null);
        return Optional.of(dialog);
    }

    public Optional<Dialog> extractMultiple(TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.isEmpty()) return Optional.empty();
        if (!(translationArguments.getFirst().asComponent() instanceof TextComponent countComponent)) return Optional.empty();

        String count = extractTarget(countComponent);
        Dialog dialog = new Dialog(null, count);
        return Optional.of(dialog);
    }
}