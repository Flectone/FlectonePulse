package net.flectone.pulse.module.message.afk.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;

public interface AFKMessageContext extends MessageContext {

    static AFKMessageContextImpl.AFKMessageContextImplBuilder builder() {
        return AFKMessageContextImpl.builder();
    }

    boolean newStatus();

    boolean fakeMessage();

    boolean vanished();

    AFKMessageContext withNewStatus(boolean newStatus);

    AFKMessageContext withFakeMessage(boolean fakeMessage);

    AFKMessageContext withVanished(boolean vanished);

}