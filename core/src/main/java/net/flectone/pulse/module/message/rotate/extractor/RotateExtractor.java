package net.flectone.pulse.module.message.rotate.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class RotateExtractor extends Extractor {

    @Inject
    public RotateExtractor() {
    }

    // Rotated %s
    public Optional<FEntity> extract(TranslatableComponent translatableComponent) {
        return extractFEntity(translatableComponent, 0);
    }

}