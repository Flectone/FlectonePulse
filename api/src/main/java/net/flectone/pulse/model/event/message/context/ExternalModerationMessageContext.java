package net.flectone.pulse.model.event.message.context;

import net.flectone.pulse.model.util.ExternalModeration;
import org.jspecify.annotations.NonNull;

/**
 * A message context that also carries a punishment owned by another plugin.
 * @author TheFaser
 */
public interface ExternalModerationMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static ExternalModerationMessageContextImpl.ExternalModerationMessageContextImplBuilder builder() {
        return ExternalModerationMessageContextImpl.builder();
    }

    /**
     * The extra value carried alongside the message.
     *
     * @return the externalModeration
     */
    @NonNull ExternalModeration externalModeration();

    /**
     * Returns a copy carrying a different value.
     *
     * @param externalModeration the new value
     * @return the copy
     */
    ExternalModerationMessageContext withExternalModeration(@NonNull ExternalModeration externalModeration);

}