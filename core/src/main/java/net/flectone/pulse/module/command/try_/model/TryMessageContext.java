package net.flectone.pulse.module.command.try_.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;

public interface TryMessageContext extends StringMessageContext {

    static TryMessageContextImpl.TryMessageContextImplBuilder builder() {
        return TryMessageContextImpl.builder();
    }

    int percent();

    TryMessageContext withPercent(int percent);

}