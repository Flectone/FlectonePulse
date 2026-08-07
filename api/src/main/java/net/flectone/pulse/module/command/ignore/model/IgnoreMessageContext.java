package net.flectone.pulse.module.command.ignore.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * A change to a player's ignore list.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface IgnoreMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static IgnoreMessageContextImpl.IgnoreMessageContextImplBuilder builder() {
        return IgnoreMessageContextImpl.builder();
    }

    /**
     * The ignore entry.
     *
     * @return the ignore entry
     */
    @NonNull Ignore ignore();

    /**
     * Whether the target is now ignored, rather than un-ignored.
     *
     * @return whether the target is now ignored, rather than un-ignored
     */
    boolean ignored();

    /**
     * Returns a copy carrying a different value.
     *
     * @param ignore the ignore entry
     * @return the copy
     */
    IgnoreMessageContext withIgnore(@NonNull Ignore ignore);

    /**
     * Returns a copy carrying a different value.
     *
     * @param ignored whether the target is now ignored, rather than un-ignored
     * @return the copy
     */
    IgnoreMessageContext withIgnored(boolean ignored);

}