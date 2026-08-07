package net.flectone.pulse.module.command.translateto.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.module.command.translateto.TranslatetoModule;
import net.flectone.pulse.module.command.translateto.model.TranslatetoMessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.payload.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslatetoProxyMessageListener implements PulseListener {

    private final TranslatetoModule translatetoModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_TRANSLATETO) return event;
        if (moduleController.isDisabledFor(translatetoModule, event.sender())) return event.withProcessed(true);
        if (!translatetoModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String targetLang = proxyPayload.readString();
            String message = proxyPayload.readString();
            String messageToTranslate = proxyPayload.readString();

            messageDispatcher.dispatch(translatetoModule, EventMetadata.builder()
                    .range(Range.get(Range.Type.SERVER))
                    .destination(translatetoModule.config().destination())
                    .sound(translatetoModule.soundOrThrow())
                    .messageContext(fResolver -> TranslatetoMessageContext.builder()
                            .base(MessageContext.builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .receiver(fResolver)
                                    .message(translatetoModule.replaceLanguage(fResolver, targetLang))
                                    .tagResolver(messagePipeline.messageTag(event.sender(), fResolver, message))
                                    .build()
                            )
                            .string(message)
                            .targetLanguage(targetLang)
                            .messageToTranslate(messageToTranslate)
                            .build()
                    )
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
