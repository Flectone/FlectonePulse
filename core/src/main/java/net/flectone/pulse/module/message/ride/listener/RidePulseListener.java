package net.flectone.pulse.module.message.ride.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.ride.RideModule;
import net.flectone.pulse.module.message.ride.extractor.RideExtractor;
import net.flectone.pulse.module.message.ride.model.Ride;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class RidePulseListener implements PulseListener {

    private final RideModule rideModule;
    private final RideExtractor rideExtractor;

    @Inject
    public RidePulseListener(RideModule rideModule,
                             RideExtractor rideExtractor) {
        this.rideModule = rideModule;
        this.rideExtractor = rideExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey != MinecraftTranslationKey.COMMANDS_RIDE_DISMOUNT_SUCCESS
                && translationKey != MinecraftTranslationKey.COMMANDS_RIDE_MOUNT_SUCCESS) return;

        Optional<Ride> optionalRide = rideExtractor.extract(event.getTranslatableComponent());
        if (optionalRide.isEmpty()) return;

        event.setCancelled(true);
        rideModule.send(event.getFPlayer(), translationKey, optionalRide.get());
    }

}