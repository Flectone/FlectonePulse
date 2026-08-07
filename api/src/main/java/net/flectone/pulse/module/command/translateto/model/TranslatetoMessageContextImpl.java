package net.flectone.pulse.module.command.translateto.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

/**
 * Carrier for a translation request contexts.
 *
 * @param base the plain context underneath
 * @param string the string
 * @param targetLanguage the language to translate into
 * @param messageToTranslate the text to translate
 */
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

    /**
     * Cache key that mixes the extra values into the base key.
     *
     * @param base the base key
     * @param playerMessage the playerMessage
     * @param targetLanguage the language to translate into
     * @param messageToTranslate the text to translate
     */
    public record TranslatetoCacheKey(
            @NonNull CacheKey base,
            @NonNull String playerMessage,
            @NonNull String targetLanguage,
            @NonNull String messageToTranslate
    ) implements CacheKey {
    }

}