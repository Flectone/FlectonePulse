package net.flectone.pulse.module.command.spy.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.jspecify.annotations.NonNull;

/**
 * A change to a moderator's spy mode, or a message it caught.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface SpyMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static SpyMessageContextImpl.SpyMessageContextImplBuilder builder() {
        return SpyMessageContextImpl.builder();
    }

    /**
     * Whether spy mode was switched on, rather than off.
     *
     * @return whether spy mode was switched on, rather than off
     */
    boolean turned();

    /**
     * What was spied on.
     *
     * @return what was spied on
     */
    @NonNull String action();

    /**
     * Returns a copy carrying a different value.
     *
     * @param turned whether spy mode was switched on, rather than off
     * @return the copy
     */
    SpyMessageContext withTurned(boolean turned);

    /**
     * Returns a copy carrying a different value.
     *
     * @param action what was spied on
     * @return the copy
     */
    SpyMessageContext withAction(@NonNull String action);

}