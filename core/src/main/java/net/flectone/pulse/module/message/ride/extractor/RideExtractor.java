package net.flectone.pulse.module.message.ride.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.ride.model.Ride;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideExtractor extends Extractor {

    // %s stopped riding %s
    // %s started riding %s
    public Optional<Ride> extract(TranslatableComponent translatableComponent) {
        Optional<FEntity> target = extractFEntity(translatableComponent, 0);
        if (target.isEmpty()) return Optional.empty();

        Optional<FEntity> secondTarget = extractFEntity(translatableComponent, 1);
        if (secondTarget.isEmpty()) return Optional.empty();

        Ride ride = new Ride(target.get(), secondTarget.get());
        return Optional.of(ride);
    }

}