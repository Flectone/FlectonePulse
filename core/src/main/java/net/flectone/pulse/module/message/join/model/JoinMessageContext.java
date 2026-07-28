package net.flectone.pulse.module.message.join.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;

public interface JoinMessageContext extends MessageContext {

    static JoinMessageContextImpl.JoinMessageContextImplBuilder builder() {
        return JoinMessageContextImpl.builder();
    }

    boolean playedBefore();

    boolean fakeMessage();

    boolean vanished();

    JoinMessageContext withPlayedBefore(boolean playedBefore);

    JoinMessageContext withFakeMessage(boolean fakeMessage);

    JoinMessageContext withVanished(boolean vanished);

}