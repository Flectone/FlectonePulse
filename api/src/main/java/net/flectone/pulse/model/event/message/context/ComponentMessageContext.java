package net.flectone.pulse.model.event.message.context;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

/**
 * A message context that also carries a ready-made component that should be inserted as-is.
 * @author TheFaser
 */
public interface ComponentMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static ComponentMessageContextImpl.ComponentMessageContextImplBuilder builder() {
        return ComponentMessageContextImpl.builder();
    }

    /**
     * The extra value carried alongside the message.
     *
     * @return the component
     */
    @NonNull Component component();

    /**
     * Returns a copy carrying a different value.
     *
     * @param component the new value
     * @return the copy
     */
    ComponentMessageContext withComponent(@NonNull Component component);

}