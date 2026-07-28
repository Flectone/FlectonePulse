package net.flectone.pulse.module.command.try_.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.try_.TryModule;
import net.flectone.pulse.module.command.try_.model.TryMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

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

            messageDispatcher.dispatch(tryModule, TryMetadata.builder()
                    .base(EventMetadata.builder()
                            .range(Range.get(Range.Type.SERVER))
                            .destination(tryModule.config().destination())
                            .sound(tryModule.soundOrThrow())
                            .messageContext(fResolver -> MessageContext.builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .receiver(fResolver)
                                    .message(tryModule.replacePercent(fResolver, value))
                                    .tagResolver(messagePipeline.messageTag(event.sender(), fResolver, message))
                                    .build()
                            )
                            .build()
                    )
                    .percent(value)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
