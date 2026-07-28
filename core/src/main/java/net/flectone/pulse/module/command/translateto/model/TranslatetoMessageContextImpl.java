package net.flectone.pulse.module.command.translateto.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

@With
@Builder
record TranslatetoMessageContextImpl(
        @NonNull MessageContext base,
        @NonNull String string,
        @NonNull String targetLanguage,
        @NonNull String messageToTranslate
) implements TranslatetoMessageContext {

    @Override
    public CacheKey createCacheKey() {
        return new TranslatetoCacheKey(base().createCacheKey(), string, targetLanguage, messageToTranslate);
    }

    public record TranslatetoCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull String targetLanguage,
            @NonNull String messageToTranslate
    ) implements CacheKey {
    }

}