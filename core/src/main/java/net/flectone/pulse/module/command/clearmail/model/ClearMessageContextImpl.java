package net.flectone.pulse.module.command.clearmail.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.mail.model.Mail;
import org.jspecify.annotations.NonNull;

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

    public record ClearCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull Mail mail
    ) implements CacheKey {
    }

}