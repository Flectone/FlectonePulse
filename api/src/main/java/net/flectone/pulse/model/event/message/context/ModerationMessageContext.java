package net.flectone.pulse.model.event.message.context;

import net.flectone.pulse.model.value.Moderation;
import org.jspecify.annotations.NonNull;

/**
 * A message context that also carries a punishment issued by FlectonePulse.
 * @author TheFaser
 */
public interface ModerationMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static ModerationMessageContextImpl.ModerationMessageContextImplBuilder builder() {
        return ModerationMessageContextImpl.builder();
    }

    /**
     * The extra value carried alongside the message.
     *
     * @return the moderation
     */
    @NonNull Moderation moderation();

    /**
     * Returns a copy carrying a different value.
     *
     * @param moderation the new value
     * @return the copy
     */
    ModerationMessageContext withModeration(@NonNull Moderation moderation);

}