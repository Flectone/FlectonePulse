package net.flectone.pulse.module.message.update.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;

public interface UpdateMessageContext extends MessageContext {

    static UpdateMessageContextImpl.UpdateMessageContextImplBuilder builder() {
        return UpdateMessageContextImpl.builder();
    }

    @NonNull String latestVersion();

    @NonNull String currentVersion();

    UpdateMessageContext withLatestVersion(@NonNull String latestVersion);

    UpdateMessageContext withCurrentVersion(@NonNull String currentVersion);

}