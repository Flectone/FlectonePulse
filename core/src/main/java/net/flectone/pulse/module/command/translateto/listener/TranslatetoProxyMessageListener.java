package net.flectone.pulse.module.command.translateto.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.translateto.TranslatetoModule;
import net.flectone.pulse.module.command.translateto.model.TranslatetoMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TranslatetoProxyMessageListener implements PulseListener {

    private final TranslatetoModule translatetoModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;

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

            messageDispatcher.dispatch(translatetoModule, TranslatetoMetadata.<Localization.Command.Translateto>builder()
                    .base(EventMetadata.<Localization.Command.Translateto>builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .format(translatetoModule.replaceLanguage(targetLang))
                            .range(Range.get(Range.Type.SERVER))
                            .destination(translatetoModule.config().destination())
                            .message(message)
                            .sound(translatetoModule.soundOrThrow())
                            .build()
                    )
                    .targetLanguage(targetLang)
                    .messageToTranslate(messageToTranslate)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
