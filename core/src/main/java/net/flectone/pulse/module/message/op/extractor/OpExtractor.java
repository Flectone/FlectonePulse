package net.flectone.pulse.module.message.op.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class OpExtractor extends Extractor {

    @Inject
    public OpExtractor() {
    }

    // Made %s a server operator
    public Optional<FEntity> extract(TranslatableComponent translatableComponent) {
        return extractFEntity(translatableComponent, 0);
    }

}
