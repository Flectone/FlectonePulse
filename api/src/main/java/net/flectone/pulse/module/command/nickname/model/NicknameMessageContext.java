package net.flectone.pulse.module.command.nickname.model;

import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * A change to a player's nickname.
 * Builds on {@link MessageContext}.
 * @author TheFaser
 */
public interface NicknameMessageContext extends MessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static NicknameMessageContextImpl.NicknameMessageContextImplBuilder builder() {
        return NicknameMessageContextImpl.builder();
    }

    /**
     * The new nickname.
     *
     * @return the new nickname
     */
    @NonNull String nickname();

    /**
     * Returns a copy carrying a different value.
     *
     * @param nickname the new nickname
     * @return the copy
     */
    NicknameMessageContext withNickname(@NonNull String nickname);

}