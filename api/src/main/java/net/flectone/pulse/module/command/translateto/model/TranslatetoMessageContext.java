package net.flectone.pulse.module.command.translateto.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.jspecify.annotations.NonNull;

/**
 * A translation request.
 * Builds on {@link StringMessageContext}.
 * @author TheFaser
 */
public interface TranslatetoMessageContext extends StringMessageContext {

    /**
     * Starts building this context.
     *
     * @return a new builder
     */
    static TranslatetoMessageContextImpl.TranslatetoMessageContextImplBuilder builder() {
        return TranslatetoMessageContextImpl.builder();
    }

    /**
     * The language to translate into.
     *
     * @return the language to translate into
     */
    @NonNull String targetLanguage();

    /**
     * The text to translate.
     *
     * @return the text to translate
     */
    @NonNull String messageToTranslate();

    /**
     * Returns a copy carrying a different value.
     *
     * @param targetLanguage the language to translate into
     * @return the copy
     */
    TranslatetoMessageContext withTargetLanguage(@NonNull String targetLanguage);

    /**
     * Returns a copy carrying a different value.
     *
     * @param messageToTranslate the text to translate
     * @return the copy
     */
    TranslatetoMessageContext withMessageToTranslate(@NonNull String messageToTranslate);

}