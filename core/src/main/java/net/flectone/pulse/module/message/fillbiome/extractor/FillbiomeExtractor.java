package net.flectone.pulse.module.message.fillbiome.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.fillbiome.model.Fillbiome;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class FillbiomeExtractor extends Extractor {

    @Inject
    public FillbiomeExtractor() {
    }

    public Optional<Fillbiome> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        Optional<String> blocks = Optional.empty();
        int i = 0;
        if (translationKey == MinecraftTranslationKey.COMMANDS_FILLBIOME_SUCCESS_COUNT) {
            blocks = extractTextContent(translatableComponent, 0);
            i = 1;
        }

        Optional<String> x1 = extractTextContent(translatableComponent, i);
        if (x1.isEmpty()) return Optional.empty();

        Optional<String> y1 = extractTextContent(translatableComponent, i + 1);
        if (y1.isEmpty()) return Optional.empty();

        Optional<String> z1 = extractTextContent(translatableComponent, i + 2);
        if (z1.isEmpty()) return Optional.empty();

        Optional<String> x2 = extractTextContent(translatableComponent, i + 3);
        if (x2.isEmpty()) return Optional.empty();

        Optional<String> y2 = extractTextContent(translatableComponent, i + 4);
        if (y2.isEmpty()) return Optional.empty();

        Optional<String> z2 = extractTextContent(translatableComponent, i + 5);
        if (z2.isEmpty()) return Optional.empty();

        Fillbiome fillbiome = new Fillbiome(blocks.orElse(null), x1.get(), y1.get(), z1.get(), x2.get(), y2.get(), z2.get());
        return Optional.of(fillbiome);
    }

}