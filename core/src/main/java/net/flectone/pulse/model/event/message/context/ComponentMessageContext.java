package net.flectone.pulse.model.event.message.context;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

public interface ComponentMessageContext extends MessageContext {

    static ComponentMessageContextImpl.ComponentMessageContextImplBuilder builder() {
        return ComponentMessageContextImpl.builder();
    }

    @NonNull Component component();

    ComponentMessageContext withComponent(@NonNull Component component);

}