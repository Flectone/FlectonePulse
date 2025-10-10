package net.flectone.pulse.module.message.summon.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SummonExtractor extends Extractor {

    // Summoned new %s
    public Optional<FEntity> extract(TranslatableComponent translatableComponent) {
        return extractFEntity(translatableComponent, 0);
    }

}
