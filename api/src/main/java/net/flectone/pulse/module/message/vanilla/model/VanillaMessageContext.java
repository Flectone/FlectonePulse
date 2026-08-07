package net.flectone.pulse.module.message.vanilla.model;

import net.flectone.pulse.model.event.message.context.VanishMessageContext;
import org.jspecify.annotations.Nullable;

/**
 * A vanilla server message the plugin re-renders.
 * Builds on {@link VanishMessageContext}.
 * @author TheFaser
 */
public interface VanillaMessageContext extends VanishMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static VanillaMessageContextImpl.VanillaMessageContextImplBuilder builder() {
        return VanillaMessageContextImpl.builder();
    }

    /**
     * The message broken into its translation key and arguments, or null if it could not be parsed.
     *
     * @return the message broken into its translation key and arguments, or null if it could not be parsed
     */
    @Nullable ParsedComponent parsedComponent();

    /**
     * Returns a copy carrying a different value.
     *
     * @param parsedComponent the message broken into its translation key and arguments, or null if it could not be parsed
     * @return the copy
     */
    VanillaMessageContext withParsedComponent(@Nullable ParsedComponent parsedComponent);

}