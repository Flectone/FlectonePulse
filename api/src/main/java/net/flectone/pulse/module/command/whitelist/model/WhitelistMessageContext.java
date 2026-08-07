package net.flectone.pulse.module.command.whitelist.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.Nullable;

/**
 * A change to the whitelist.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface WhitelistMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static WhitelistMessageContextImpl.WhitelistMessageContextImplBuilder builder() {
        return WhitelistMessageContextImpl.builder();
    }

    /**
     * The whitelist entry, or null when the whitelist itself was toggled.
     *
     * @return the whitelist entry, or null when the whitelist itself was toggled
     */
    @Nullable Moderation moderation();

    /**
     * Whether the whitelist was switched on, rather than off.
     *
     * @return whether the whitelist was switched on, rather than off
     */
    boolean turnedOn();

    /**
     * Returns a copy carrying a different value.
     *
     * @param moderation the whitelist entry, or null when the whitelist itself was toggled
     * @return the copy
     */
    WhitelistMessageContext withModeration(@Nullable Moderation moderation);

    /**
     * Returns a copy carrying a different value.
     *
     * @param turnedOn whether the whitelist was switched on, rather than off
     * @return the copy
     */
    WhitelistMessageContext withTurnedOn(boolean turnedOn);

}