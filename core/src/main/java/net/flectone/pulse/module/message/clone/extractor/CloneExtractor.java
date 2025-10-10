package net.flectone.pulse.module.message.clone.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CloneExtractor extends Extractor {

    // Successfully cloned %s block(s)
    public Optional<String> extract(TranslatableComponent translatableComponent) {
        return extractTextContent(translatableComponent, 0);
    }

}
