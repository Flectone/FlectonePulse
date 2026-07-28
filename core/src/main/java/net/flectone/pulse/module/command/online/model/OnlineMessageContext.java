package net.flectone.pulse.module.command.online.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;

public interface OnlineMessageContext extends MessageContext {

    static OnlineMessageContextImpl.OnlineMessageContextImplBuilder builder() {
        return OnlineMessageContextImpl.builder();
    }

    @NonNull String type();

    OnlineMessageContext withType(@NonNull String type);

}