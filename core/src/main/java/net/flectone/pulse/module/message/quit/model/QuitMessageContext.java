package net.flectone.pulse.module.message.quit.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;

public interface QuitMessageContext extends MessageContext {

    static QuitMessageContextImpl.QuitMessageContextImplBuilder builder() {
        return QuitMessageContextImpl.builder();
    }

    boolean fakeMessage();

    boolean vanished();

    QuitMessageContext withFakeMessage(boolean fakeMessage);

    QuitMessageContext withVanished(boolean vanished);

}