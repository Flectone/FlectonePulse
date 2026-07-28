package net.flectone.pulse.module.command.ignore.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;

public interface IgnoreMessageContext extends MessageContext {

    static IgnoreMessageContextImpl.IgnoreMessageContextImplBuilder builder() {
        return IgnoreMessageContextImpl.builder();
    }

    @NonNull Ignore ignore();

    boolean ignored();

    IgnoreMessageContext withIgnore(@NonNull Ignore ignore);

    IgnoreMessageContext withIgnored(boolean ignored);

}