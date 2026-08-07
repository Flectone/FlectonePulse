package net.flectone.pulse.module.command.anon.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.module.command.anon.AnonModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.util.payload.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AnonProxyMessageListener implements PulseListener {

    private final AnonModule anonModule;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final MessagePipeline messagePipeline;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_ANON) return event;
        if (moduleController.isDisabledFor(anonModule, event.sender())) return event.withProcessed(true);
        if (!anonModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(anonModule, EventMetadata.builder()
                    .range(Range.get(Range.Type.SERVER))
                    .destination(anonModule.config().destination())
                    .sound(anonModule.soundOrThrow())
                    .messageContext(fResolver -> StringMessageContext.builder()
                            .base(MessageContext.builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .receiver(fResolver)
                                    .message(anonModule.localization(fResolver).format())
                                    .tagResolver(messagePipeline.messageTag(event.sender(), fResolver, message))
                                    .build()
                            )
                            .string(message)
                            .build()
                    )
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
