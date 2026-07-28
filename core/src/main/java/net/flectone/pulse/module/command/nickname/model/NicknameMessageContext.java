package net.flectone.pulse.module.command.nickname.model;

import org.jspecify.annotations.NonNull;
import net.flectone.pulse.model.event.message.context.MessageContext;

public interface NicknameMessageContext extends MessageContext {

    static NicknameMessageContextImpl.NicknameMessageContextImplBuilder builder() {
        return NicknameMessageContextImpl.builder();
    }

    @NonNull String nickname();

    NicknameMessageContext withNickname(@NonNull String nickname);

}