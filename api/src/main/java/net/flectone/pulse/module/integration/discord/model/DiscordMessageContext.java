package net.flectone.pulse.module.integration.discord.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.model.value.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A message that came from Discord.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface DiscordMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static DiscordMessageContextImpl.DiscordMessageContextImplBuilder builder() {
        return DiscordMessageContextImpl.builder();
    }

    /**
     * The author's Discord display name.
     *
     * @return the author's Discord display name
     */
    @NonNull String globalName();

    /**
     * The author's nickname on the server.
     *
     * @return the author's nickname on the server
     */
    @NonNull String nickname();

    /**
     * The name shown for the author.
     *
     * @return the name shown for the author
     */
    @NonNull String displayName();

    /**
     * The author's Discord handle.
     *
     * @return the author's Discord handle
     */
    @NonNull String userName();

    /**
     * The author and text of the replied-to message, or null.
     *
     * @return the author and text of the replied-to message, or null
     */
    @Nullable Pair<String, String> reply();

    /**
     * Returns a copy carrying a different value.
     *
     * @param globalName the author's Discord display name
     * @return the copy
     */
    DiscordMessageContext withGlobalName(@NonNull String globalName);

    /**
     * Returns a copy carrying a different value.
     *
     * @param nickname the author's nickname on the server
     * @return the copy
     */
    DiscordMessageContext withNickname(@NonNull String nickname);

    /**
     * Returns a copy carrying a different value.
     *
     * @param displayName the name shown for the author
     * @return the copy
     */
    DiscordMessageContext withDisplayName(@NonNull String displayName);

    /**
     * Returns a copy carrying a different value.
     *
     * @param userName the author's Discord handle
     * @return the copy
     */
    DiscordMessageContext withUserName(@NonNull String userName);

    /**
     * Returns a copy carrying a different value.
     *
     * @param reply the author and text of the replied-to message, or null
     * @return the copy
     */
    DiscordMessageContext withReply(@Nullable Pair<String, String> reply);

}