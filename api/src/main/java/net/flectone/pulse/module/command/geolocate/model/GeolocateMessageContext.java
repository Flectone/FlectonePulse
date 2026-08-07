package net.flectone.pulse.module.command.geolocate.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * The location looked up for a player's address.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface GeolocateMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static GeolocateMessageContextImpl.GeolocateMessageContextImplBuilder builder() {
        return GeolocateMessageContextImpl.builder();
    }

    /**
     * The answer from the geolocation service.
     *
     * @return the answer from the geolocation service
     */
    @NonNull IpResponse response();

    /**
     * Returns a copy carrying a different value.
     *
     * @param response the answer from the geolocation service
     * @return the copy
     */
    GeolocateMessageContext withResponse(@NonNull IpResponse response);

}