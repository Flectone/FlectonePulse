package net.flectone.pulse.module.command.whitelist.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.Nullable;

public interface WhitelistMessageContext extends MessageContext {

    static WhitelistMessageContextImpl.WhitelistMessageContextImplBuilder builder() {
        return WhitelistMessageContextImpl.builder();
    }

    @Nullable Moderation moderation();

    boolean turnedOn();

    WhitelistMessageContext withModeration(@Nullable Moderation moderation);

    WhitelistMessageContext withTurnedOn(boolean turnedOn);

}