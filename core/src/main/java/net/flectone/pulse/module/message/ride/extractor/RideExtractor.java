package net.flectone.pulse.module.message.ride.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.ride.model.Ride;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;

import java.util.List;
import java.util.Optional;

@Singleton
public class RideExtractor extends Extractor {

    @Inject
    public RideExtractor() {
    }

    public Optional<Ride> extract(TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translatableComponent.arguments().size() < 2) return Optional.empty();

        Optional<FEntity> optionalTarget = extractFEntity(translationArguments.get(0).asComponent());
        if (optionalTarget.isEmpty()) return Optional.empty();

        Optional<FEntity> optionalDestination = extractFEntity(translationArguments.get(1).asComponent());
        if (optionalDestination.isEmpty()) return Optional.empty();

        Ride ride = new Ride(optionalTarget.get(), optionalDestination.get());
        return Optional.of(ride);
    }

}