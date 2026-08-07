package net.flectone.pulse.module.integration.twitch.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.model.util.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A message that came from Twitch chat.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface TwitchMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static TwitchMessageContextImpl.TwitchMessageContextImplBuilder builder() {
        return TwitchMessageContextImpl.builder();
    }

    /**
     * The author's Twitch name.
     *
     * @return the author's Twitch name
     */
    @NonNull String nickname();

    /**
     * The channel the message was posted in.
     *
     * @return the channel the message was posted in
     */
    @NonNull String channel();

    /**
     * The author and text of the replied-to message, or null.
     *
     * @return the author and text of the replied-to message, or null
     */
    @Nullable Pair<String, String> reply();

    /**
     * Returns a copy carrying a different value.
     *
     * @param nickname the author's Twitch name
     * @return the copy
     */
    TwitchMessageContext withNickname(@NonNull String nickname);

    /**
     * Returns a copy carrying a different value.
     *
     * @param channel the channel the message was posted in
     * @return the copy
     */
    TwitchMessageContext withChannel(@NonNull String channel);

    /**
     * Returns a copy carrying a different value.
     *
     * @param reply the author and text of the replied-to message, or null
     * @return the copy
     */
    TwitchMessageContext withReply(@Nullable Pair<String, String> reply);

}