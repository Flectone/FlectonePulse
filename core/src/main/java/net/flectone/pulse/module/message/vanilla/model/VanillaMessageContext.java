package net.flectone.pulse.module.message.vanilla.model;

import net.flectone.pulse.model.event.message.context.VanishMessageContext;
import org.jspecify.annotations.Nullable;

public interface VanillaMessageContext extends VanishMessageContext {

    static VanillaMessageContextImpl.VanillaMessageContextImplBuilder builder() {
        return VanillaMessageContextImpl.builder();
    }

    @Nullable ParsedComponent parsedComponent();

    VanillaMessageContext withParsedComponent(@Nullable ParsedComponent parsedComponent);

}