package net.flectone.pulse.module.command.translateto.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.jspecify.annotations.NonNull;

public interface TranslatetoMessageContext extends StringMessageContext {

    static TranslatetoMessageContextImpl.TranslatetoMessageContextImplBuilder builder() {
        return TranslatetoMessageContextImpl.builder();
    }

    @NonNull String targetLanguage();

    @NonNull String messageToTranslate();

    TranslatetoMessageContext withTargetLanguage(@NonNull String targetLanguage);

    TranslatetoMessageContext withMessageToTranslate(@NonNull String messageToTranslate);

}