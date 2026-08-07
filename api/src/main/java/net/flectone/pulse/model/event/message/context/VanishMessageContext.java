package net.flectone.pulse.model.event.message.context;

/**
 * A message context that also records whether the sender is hidden, so join and quit
 * messages can be faked or suppressed for vanished players.
 * @author TheFaser
 */
public interface VanishMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static VanishMessageContextImpl.VanishMessageContextImplBuilder builder() {
        return VanishMessageContextImpl.builder();
    }

    /**
     * Whether this message is a stand-in produced by vanishing rather than a real event.
     *
     * @return true if the message is faked
     */
    boolean fakeMessage();

    /**
     * Whether the sender is currently hidden.
     *
     * @return true if the sender is vanished
     */
    boolean vanished();

    /**
     * Returns a copy with the faked flag set.
     *
     * @param fakeMessage the new state
     * @return the copy
     */
    VanishMessageContext withFakeMessage(boolean fakeMessage);

    /**
     * Returns a copy with the vanished flag set.
     *
     * @param vanished the new state
     * @return the copy
     */
    VanishMessageContext withVanished(boolean vanished);

}
