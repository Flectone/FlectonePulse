package net.flectone.pulse.module.command.spy.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.command.spy.model.SpyMessageContext;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpyProxyMessageListener implements PulseListener {

    private final SpyModule spyModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_SPY) return event;
        if (!moduleController.isEnable(spyModule)) return event.withProcessed(true);
        if (!spyModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String action = proxyPayload.readString();
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(spyModule, EventMetadata.builder()
                    .range(Range.get(Range.Type.SERVER))
                    .filter(spyModule.createFilter(event.sender() instanceof FPlayer fPlayer ? fPlayer : FPlayer.UNKNOWN, Set.of()))
                    .destination(spyModule.config().destination())
                    .messageContext(fResolver -> SpyMessageContext.builder()
                            .base(MessageContext.builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .receiver(fResolver)
                                    .message(spyModule.localization(fResolver).formatLog())
                                    .tagResolver(messagePipeline.messageTag(event.sender(), fResolver, message))
                                    .build()
                            )
                            .string(message)
                            .turned(true)
                            .action(action)
                            .build()
                    )
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
