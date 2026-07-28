package net.flectone.pulse.model.event;

public interface VanishMetadata extends EventMetadata {

    boolean fakeMessage();

    boolean vanished();

}
