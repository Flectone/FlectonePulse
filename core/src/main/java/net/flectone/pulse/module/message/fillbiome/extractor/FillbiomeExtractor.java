package net.flectone.pulse.module.message.fillbiome.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.fillbiome.model.Fillbiome;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;

import java.util.List;
import java.util.Optional;

@Singleton
public class FillbiomeExtractor extends Extractor {

    @Inject
    public FillbiomeExtractor() {
    }

    public Optional<Fillbiome> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.size() < 6) return Optional.empty();
        if (!(translationArguments.getFirst().asComponent() instanceof TextComponent amountComponent)) return Optional.empty();

        int i = 0;
        String count = null;
        if (translationKey == MinecraftTranslationKey.COMMANDS_FILLBIOME_SUCCESS_COUNT) {
            i = 1;
            count = amountComponent.content();
        }

        if (!(translationArguments.get(i).asComponent() instanceof TextComponent x1Component)) return Optional.empty();
        if (!(translationArguments.get(i + 1).asComponent() instanceof TextComponent y1Component)) return Optional.empty();
        if (!(translationArguments.get(i + 2).asComponent() instanceof TextComponent z1Component)) return Optional.empty();
        if (!(translationArguments.get(i + 3).asComponent() instanceof TextComponent x2Component)) return Optional.empty();
        if (!(translationArguments.get(i + 4).asComponent() instanceof TextComponent y2Component)) return Optional.empty();
        if (!(translationArguments.get(i + 5).asComponent() instanceof TextComponent z2Component)) return Optional.empty();

        String x1 = x1Component.content();
        String y1 = y1Component.content();
        String z1 = z1Component.content();
        String x2 = x2Component.content();
        String y2 = y2Component.content();
        String z2 = z2Component.content();

        Fillbiome fillbiome = new Fillbiome(count, x1, y1, z1, x2, y2, z2);
        return Optional.of(fillbiome);
    }

}