package net.flectone.pulse.module.message.dialog.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.dialog.model.Dialog;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class DialogExtractor extends Extractor {

    @Inject
    public DialogExtractor() {
    }

    // Cleared dialog for %s
    // Displayed dialog to %s
    public Optional<Dialog> extractSingle(TranslatableComponent translatableComponent) {
        Optional<FEntity> target = extractFEntity(translatableComponent, 0);
        if (target.isEmpty()) return Optional.empty();

        Dialog dialog = Dialog.builder()
                .target(target.get())
                .build();

        return Optional.of(dialog);
    }

    // Cleared dialog for %s players
    // Displayed dialog to %s players
    public Optional<Dialog> extractMultiple(TranslatableComponent translatableComponent) {
        Optional<String> players = extractTextContent(translatableComponent, 0);
        if (players.isEmpty()) return Optional.empty();

        Dialog dialog = Dialog.builder()
                .players(players.get())
                .build();

        return Optional.of(dialog);
    }
}