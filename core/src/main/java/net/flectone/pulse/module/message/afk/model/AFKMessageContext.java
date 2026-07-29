package net.flectone.pulse.module.message.afk.model;

import net.flectone.pulse.model.event.message.context.VanishMessageContext;

public interface AFKMessageContext extends VanishMessageContext {

    static AFKMessageContextImpl.AFKMessageContextImplBuilder builder() {
        return AFKMessageContextImpl.builder();
    }

    boolean newStatus();

    AFKMessageContext withNewStatus(boolean newStatus);

}