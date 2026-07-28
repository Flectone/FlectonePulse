package net.flectone.pulse.module.command.spy.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.jspecify.annotations.NonNull;

public interface SpyMessageContext extends StringMessageContext {

    static SpyMessageContextImpl.SpyMessageContextImplBuilder builder() {
        return SpyMessageContextImpl.builder();
    }

    boolean turned();

    @NonNull String action();

    SpyMessageContext withTurned(boolean turned);

    SpyMessageContext withAction(@NonNull String action);

}