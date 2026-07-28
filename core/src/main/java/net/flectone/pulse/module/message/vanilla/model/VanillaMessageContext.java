package net.flectone.pulse.module.message.vanilla.model;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import net.flectone.pulse.model.event.message.context.MessageContext;

public interface VanillaMessageContext extends MessageContext {

    static VanillaMessageContextImpl.VanillaMessageContextImplBuilder builder() {
        return VanillaMessageContextImpl.builder();
    }

    @Nullable ParsedComponent parsedComponent();

    boolean fakeMessage();

    boolean vanished();

    VanillaMessageContext withParsedComponent(@Nullable ParsedComponent parsedComponent);

    VanillaMessageContext withFakeMessage(boolean fakeMessage);

    VanillaMessageContext withVanished(boolean vanished);

}