package net.flectone.pulse.module.command.deletemessage.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;

import java.util.UUID;

public interface DeletemessageMessageContext extends MessageContext {

    static DeletemessageMessageContextImpl.DeletemessageMessageContextImplBuilder builder() {
        return DeletemessageMessageContextImpl.builder();
    }

    @NonNull UUID deletedUUID();

    DeletemessageMessageContext withDeletedUUID(@NonNull UUID deletedUUID);

}