package net.flectone.pulse.module.message.setblock.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.setblock.model.Setblock;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SetblockExtractor extends Extractor {

    // Changed the block at %s, %s, %s
    // Changed the block
    public Optional<Setblock> extract(TranslatableComponent translatableComponent) {
        Optional<String> x = extractTextContent(translatableComponent, 0);
        Optional<String> y = extractTextContent(translatableComponent, 1);
        Optional<String> z = extractTextContent(translatableComponent, 2);

        Setblock setblock = new Setblock(x.orElse(null), y.orElse(null), z.orElse(null));
        return Optional.of(setblock);
    }

}
