package net.flectone.pulse.module.command.mail.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a mail being sent or delivered contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param mail the mail
 * @param target the player it is addressed to
 */
@With
@Builder
record MailMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        @NonNull Mail mail,
        @NonNull FPlayer target
) implements MailMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new MailCacheKey(base().createCacheKey(), string, mail, target);
    }

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param mail the mail
     * @param target the player it is addressed to
     */
    public record MailCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull Mail mail,
            @NonNull FPlayer target
    ) implements CacheKey {
    }

}