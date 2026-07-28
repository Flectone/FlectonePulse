package net.flectone.pulse.module.message.chat.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.StringMessageContext;

public interface ChatMessageContext extends StringMessageContext {

    static ChatMessageContextImpl.ChatMessageContextImplBuilder builder() {
        return ChatMessageContextImpl.builder();
    }

    @NonNull Chat chat();

    ChatMessageContext withChat(@NonNull Chat chat);

}