package net.flectone.pulse.model.event.message.context;

import org.jspecify.annotations.NonNull;

public interface StringMessageContext extends MessageContext {

    static StringMessageContextImpl.StringMessageContextImplBuilder builder() {
        return StringMessageContextImpl.builder();
    }

    @NonNull String string();

    StringMessageContext withString(@NonNull String string);

}
