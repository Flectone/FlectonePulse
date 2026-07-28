package net.flectone.pulse.module.command.stream.model;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import net.flectone.pulse.model.event.message.context.MessageContext;

public interface StreamMessageContext extends MessageContext {

    static StreamMessageContextImpl.StreamMessageContextImplBuilder builder() {
        return StreamMessageContextImpl.builder();
    }

    boolean turned();

    @Nullable String urls();

    StreamMessageContext withTurned(boolean turned);

    StreamMessageContext withUrls(@Nullable String urls);

}