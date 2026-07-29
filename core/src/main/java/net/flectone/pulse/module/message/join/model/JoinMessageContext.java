package net.flectone.pulse.module.message.join.model;

import net.flectone.pulse.model.event.message.context.VanishMessageContext;

public interface JoinMessageContext extends VanishMessageContext {

    static JoinMessageContextImpl.JoinMessageContextImplBuilder builder() {
        return JoinMessageContextImpl.builder();
    }

    boolean playedBefore();

    JoinMessageContext withPlayedBefore(boolean playedBefore);

}