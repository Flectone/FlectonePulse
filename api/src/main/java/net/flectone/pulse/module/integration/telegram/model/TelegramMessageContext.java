package net.flectone.pulse.module.integration.telegram.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.model.util.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A message that came from Telegram.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface TelegramMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static TelegramMessageContextImpl.TelegramMessageContextImplBuilder builder() {
        return TelegramMessageContextImpl.builder();
    }

    /**
     * The author's Telegram handle.
     *
     * @return the author's Telegram handle
     */
    @NonNull String userName();

    /**
     * The author's first name.
     *
     * @return the author's first name
     */
    @NonNull String firstName();

    /**
     * The author's last name.
     *
     * @return the author's last name
     */
    @NonNull String lastName();

    /**
     * The chat the message was posted in.
     *
     * @return the chat the message was posted in
     */
    @NonNull String chat();

    /**
     * The author and text of the replied-to message, or null.
     *
     * @return the author and text of the replied-to message, or null
     */
    @Nullable Pair<String, String> reply();

    /**
     * Returns a copy carrying a different value.
     *
     * @param userName the author's Telegram handle
     * @return the copy
     */
    TelegramMessageContext withUserName(@NonNull String userName);

    /**
     * Returns a copy carrying a different value.
     *
     * @param firstName the author's first name
     * @return the copy
     */
    TelegramMessageContext withFirstName(@NonNull String firstName);

    /**
     * Returns a copy carrying a different value.
     *
     * @param lastName the author's last name
     * @return the copy
     */
    TelegramMessageContext withLastName(@NonNull String lastName);

    /**
     * Returns a copy carrying a different value.
     *
     * @param chat the chat the message was posted in
     * @return the copy
     */
    TelegramMessageContext withChat(@NonNull String chat);

    /**
     * Returns a copy carrying a different value.
     *
     * @param reply the author and text of the replied-to message, or null
     * @return the copy
     */
    TelegramMessageContext withReply(@Nullable Pair<String, String> reply);

}