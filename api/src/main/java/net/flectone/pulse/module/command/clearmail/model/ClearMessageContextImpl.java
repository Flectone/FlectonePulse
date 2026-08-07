package net.flectone.pulse.module.command.clearmail.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.mail.model.Mail;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a mail removed by the clear-mail command contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param mail the mail that was deleted
 */
@With
@Builder
record ClearMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        @NonNull Mail mail
) implements ClearMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new ClearCacheKey(base().createCacheKey(), string, mail);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param mail the mail that was deleted
     */
    public record ClearCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull Mail mail
    ) implements CacheKey {
    }

}