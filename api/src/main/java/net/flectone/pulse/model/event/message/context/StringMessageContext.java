package net.flectone.pulse.model.event.message.context;

import org.jspecify.annotations.NonNull;

/**
 * A message context that also carries an extra string, such as the original player input.
 * @author TheFaser
 */
public interface StringMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static StringMessageContextImpl.StringMessageContextImplBuilder builder() {
        return StringMessageContextImpl.builder();
    }

    /**
     * The extra value carried alongside the message.
     *
     * @return the string
     */
    @NonNull String string();

    /**
     * Returns a copy carrying a different value.
     *
     * @param string the new value
     * @return the copy
     */
    StringMessageContext withString(@NonNull String string);

}
