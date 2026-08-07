package net.flectone.pulse.module.command.stream.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.Nullable;

/**
 * A change to a player's stream status.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface StreamMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static StreamMessageContextImpl.StreamMessageContextImplBuilder builder() {
        return StreamMessageContextImpl.builder();
    }

    /**
     * Whether streaming was announced, rather than ended.
     *
     * @return whether streaming was announced, rather than ended
     */
    boolean turned();

    /**
     * The stream links, or null if none were given.
     *
     * @return the stream links, or null if none were given
     */
    @Nullable String urls();

    /**
     * Returns a copy carrying a different value.
     *
     * @param turned whether streaming was announced, rather than ended
     * @return the copy
     */
    StreamMessageContext withTurned(boolean turned);

    /**
     * Returns a copy carrying a different value.
     *
     * @param urls the stream links, or null if none were given
     * @return the copy
     */
    StreamMessageContext withUrls(@Nullable String urls);

}