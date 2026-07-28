package net.flectone.pulse.module.command.helper.listener;

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
import net.flectone.pulse.module.command.helper.HelperModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HelperProxyMessageListener implements PulseListener {

    private final HelperModule helperModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_HELPER) return event;
        if (!moduleController.isEnable(helperModule)) return event.withProcessed(true);
        if (!helperModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(helperModule, EventMetadata.builder()
                    .range(Range.get(Range.Type.SERVER))
                    .filter(helperModule.getFilterSee())
                    .destination(helperModule.config().destination())
                    .sound(helperModule.soundOrThrow())
                    .messageContext(fResolver -> MessageContext.builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .receiver(fResolver)
                            .message(helperModule.localization(fResolver).global())
                            .tagResolver(messagePipeline.messageTag(event.sender(), fResolver, message))
                            .build()
                    )
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
