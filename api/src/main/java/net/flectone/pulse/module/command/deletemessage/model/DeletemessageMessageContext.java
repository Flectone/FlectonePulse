package net.flectone.pulse.module.command.deletemessage.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * A chat message withdrawn by a moderator.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface DeletemessageMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static DeletemessageMessageContextImpl.DeletemessageMessageContextImplBuilder builder() {
        return DeletemessageMessageContextImpl.builder();
    }

    /**
     * Id of the message being withdrawn.
     *
     * @return id of the message being withdrawn
     */
    @NonNull UUID deletedUUID();

    /**
     * Returns a copy carrying a different value.
     *
     * @param deletedUUID id of the message being withdrawn
     * @return the copy
     */
    DeletemessageMessageContext withDeletedUUID(@NonNull UUID deletedUUID);

}