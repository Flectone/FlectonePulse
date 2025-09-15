package net.flectone.pulse.module.message.execute.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class ExecuteExtractor extends Extractor {

    @Inject
    public ExecuteExtractor() {
    }

    public Optional<String> extract(TranslatableComponent translatableComponent) {
        return extractTextContent(translatableComponent, 0);
    }
}