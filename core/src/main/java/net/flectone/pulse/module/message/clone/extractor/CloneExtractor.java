package net.flectone.pulse.module.message.clone.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class CloneExtractor extends Extractor {

    @Inject
    public CloneExtractor() {
    }

    // Successfully cloned %s block(s)
    public Optional<String> extract(TranslatableComponent translatableComponent) {
        return extractTextContent(translatableComponent, 0);
    }

}
