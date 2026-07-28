package net.flectone.pulse.module.message.rightclick.model;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

public interface RightClickMessageContext extends MessageContext {

    static RightClickMessageContextImpl.RightClickMessageContextImplBuilder builder() {
        return RightClickMessageContextImpl.builder();
    }

    @NonNull FPlayer target();

    RightClickMessageContext withTarget(@NonNull FPlayer target);

}