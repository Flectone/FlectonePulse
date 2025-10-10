package net.flectone.pulse.module.message.fill.extractor;

import com.google.inject.Singleton;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class FillExtractor extends Extractor {

    // Successfully filled %s block(s)
    public Optional<String> extract(TranslatableComponent translatableComponent) {
        return extractTextContent(translatableComponent, 0);
    }

}
