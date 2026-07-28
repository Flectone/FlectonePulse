package net.flectone.pulse.module.command.mail.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

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

    public record MailCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull Mail mail,
            @NonNull FPlayer target
    ) implements CacheKey {
    }

}