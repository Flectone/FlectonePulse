package net.flectone.pulse.module.message.worldborder.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.worldborder.model.Worldborder;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class WorldborderExtractor extends Extractor {

    @Inject
    public WorldborderExtractor() {
    }

    // Set the world border damage to %s per block each second
    // Set the world border damage buffer to %s block(s)
    // The world border is currently %s block(s) wide
    // Set the world border to %s block(s) wide
    // Set the world border warning distance to %s block(s)
    // Set the world border warning time to %s second(s)
    public Optional<Worldborder> extractValue(TranslatableComponent translatableComponent) {
        Optional<String> value = extractTextContent(translatableComponent, 0);
        if (value.isEmpty()) return Optional.empty();

        Worldborder worldborder = new Worldborder(value.get(), null);
        return Optional.of(worldborder);
    }

    // Set the center of the world border to %s, %s
    // Growing the world border to %s blocks wide over %s seconds
    // Shrinking the world border to %s block(s) wide over %s second(s)
    public Optional<Worldborder> extractSecondValue(TranslatableComponent translatableComponent) {
        Optional<String> value = extractTextContent(translatableComponent, 0);
        if (value.isEmpty()) return Optional.empty();

        Optional<String> secondValue = extractTextContent(translatableComponent, 1);
        if (secondValue.isEmpty()) return Optional.empty();

        Worldborder worldborder = new Worldborder(value.get(), secondValue.get());
        return Optional.of(worldborder);
    }

}