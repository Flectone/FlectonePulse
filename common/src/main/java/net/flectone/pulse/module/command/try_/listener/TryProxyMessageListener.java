package net.flectone.pulse.module.command.try_.listener;

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
import net.flectone.pulse.module.command.try_.TryModule;
import net.flectone.pulse.module.command.try_.model.TryMessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.payload.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TryProxyMessageListener implements PulseListener {

    private final TryModule tryModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_TRY) return event;
        if (moduleController.isDisabledFor(tryModule, event.sender())) return event.withProcessed(true);
        if (!tryModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            int value = proxyPayload.readInt();
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(tryModule, EventMetadata.builder()
                    .range(Range.get(Range.Type.SERVER))
                    .destination(tryModule.config().destination())
                    .sound(tryModule.soundOrThrow())
                    .messageContext(fResolver -> TryMessageContext.builder()
                            .base(MessageContext.builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .receiver(fResolver)
                                    .message(tryModule.replacePercent(fResolver, value))
                                    .tagResolver(messagePipeline.messageTag(event.sender(), fResolver, message))
                                    .build()
                            )
                            .string(message)
                            .percent(value)
                            .build()
                    )
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
