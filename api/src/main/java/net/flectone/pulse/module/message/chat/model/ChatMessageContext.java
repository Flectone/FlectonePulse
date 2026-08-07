package net.flectone.pulse.module.message.chat.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.jspecify.annotations.NonNull;

/**
 * A message written in one of the configured chats.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface ChatMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static ChatMessageContextImpl.ChatMessageContextImplBuilder builder() {
        return ChatMessageContextImpl.builder();
    }

    /**
     * The chat it was written in.
     *
     * @return the chat it was written in
     */
    @NonNull Chat chat();

    /**
     * Returns a copy carrying a different value.
     *
     * @param chat the chat it was written in
     * @return the copy
     */
    ChatMessageContext withChat(@NonNull Chat chat);

}