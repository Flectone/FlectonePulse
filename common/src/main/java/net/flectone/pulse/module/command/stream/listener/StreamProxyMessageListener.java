package net.flectone.pulse.module.command.stream.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.command.stream.model.StreamMessageContext;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StreamProxyMessageListener implements PulseListener {

    private final StreamModule streamModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_STREAM) return event;
        if (moduleController.isDisabledFor(streamModule, event.sender())) return event.withProcessed(true);
        if (!streamModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(streamModule, EventMetadata.builder()
                    .range(Range.get(Range.Type.SERVER))
                    .destination(streamModule.config().destination())
                    .sound(streamModule.soundOrThrow())
                    .messageContext(fResolver -> StreamMessageContext.builder()
                            .base(MessageContext.builder()
                                    .sender(event.sender())
                                    .receiver(fResolver)
                                    .message(streamModule.localization(fResolver).formatStart())
                                    .tagResolver(streamModule.urlTag(event.sender(), fResolver, message))
                                    .build()
                            )
                            .turned(true)
                            .urls(message)
                            .build()
                    )
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
