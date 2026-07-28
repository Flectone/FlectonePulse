package net.flectone.pulse.module.command.geolocate.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;

public interface GeolocateMessageContext extends MessageContext {

    static GeolocateMessageContextImpl.GeolocateMessageContextImplBuilder builder() {
        return GeolocateMessageContextImpl.builder();
    }

    @NonNull IpResponse response();

    GeolocateMessageContext withResponse(@NonNull IpResponse response);

}