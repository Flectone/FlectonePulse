package net.flectone.pulse.model.event.message.context;

public interface VanishMessageContext extends MessageContext {

    static VanishMessageContextImpl.VanishMessageContextImplBuilder builder() {
        return VanishMessageContextImpl.builder();
    }

    boolean fakeMessage();

    boolean vanished();

    VanishMessageContext withFakeMessage(boolean fakeMessage);

    VanishMessageContext withVanished(boolean vanished);

}
