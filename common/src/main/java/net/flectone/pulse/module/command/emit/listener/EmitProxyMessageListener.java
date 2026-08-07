package net.flectone.pulse.module.command.emit.listener;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.entity.FPlayerImpl;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.emit.EmitModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EmitProxyMessageListener implements PulseListener {

    private final EmitModule emitModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;
    private final Gson gson;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_EMIT) return event;
        if (moduleController.isDisabledFor(emitModule, event.sender())) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            FPlayer fTarget = gson.fromJson(proxyPayload.readString(), FPlayerImpl.class);

            Map<String, Object> destinationMap = gson.fromJson(proxyPayload.readString(), new TypeToken<Map<String, Object>>(){}.getType());
            Destination destination = Destination.fromJson(destinationMap);
            String message = proxyPayload.readString();

            if (fTarget.isConsole()) {
                messageDispatcher.dispatch(emitModule, EventMetadata.builder()
                        .range(Range.get(Range.Type.SERVER))
                        .destination(destination)
                        .sound(emitModule.soundOrThrow())
                        .messageContext(fResolver -> StringMessageContext.builder()
                                .base(MessageContext.builder()
                                        .uuid(event.uuid())
                                        .sender(event.sender())
                                        .receiver(fResolver)
                                        .flag(MessageFlag.PLACEHOLDER_CONTEXT_SENDER, false)
                                        .message(emitModule.localization(fResolver).format())
                                        .tagResolver(messagePipeline.messageTag(event.sender(), fResolver, message))
                                        .build()
                                )
                                .string(message)
                                .build()
                        )
                        .build()
                );
            } else {
                messageDispatcher.dispatch(emitModule, EventMetadata.builder()
                        .filter(fTarget)
                        .destination(destination)
                        .sound(emitModule.soundOrThrow())
                        .messageContext(fResolver -> StringMessageContext.builder()
                                .base(MessageContext.builder()
                                        .uuid(event.uuid())
                                        .sender(event.sender())
                                        .receiver(fResolver)
                                        .flag(MessageFlag.PLACEHOLDER_CONTEXT_SENDER, false)
                                        .message(emitModule.localization(fResolver).format())
                                        .tagResolver(messagePipeline.messageTag(event.sender(), fResolver, message))
                                        .build()
                                )
                                .string(message)
                                .build()
                        )
                        .build()
                );
            }
        }

        return event.withProcessed(true);
    }

}
