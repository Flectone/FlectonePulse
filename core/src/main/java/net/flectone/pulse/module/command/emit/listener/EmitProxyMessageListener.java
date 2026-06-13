package net.flectone.pulse.module.command.emit.listener;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
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
    private final Gson gson;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_EMIT) return event;
        if (moduleController.isDisabledFor(emitModule, event.sender())) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            FPlayer fTarget = gson.fromJson(proxyPayload.readString(), FPlayer.FPlayerImpl.class);

            Map<String, Object> destinationMap = gson.fromJson(proxyPayload.readString(), new TypeToken<Map<String, Object>>(){}.getType());
            Destination destination = Destination.fromJson(destinationMap);
            String message = proxyPayload.readString();

            if (fTarget.isConsole()) {
                messageDispatcher.dispatch(emitModule, EventMetadata.<Localization.Command.Emit>builder()
                        .uuid(event.uuid())
                        .sender(event.sender())
                        .flag(MessageFlag.PLACEHOLDER_CONTEXT_SENDER, false)
                        .range(Range.get(Range.Type.SERVER))
                        .format(Localization.Command.Emit::format)
                        .message(message)
                        .destination(destination)
                        .sound(emitModule.soundOrThrow())
                        .build()
                );
            } else {
                messageDispatcher.dispatch(emitModule, EventMetadata.<Localization.Command.Emit>builder()
                        .uuid(event.uuid())
                        .sender(event.sender())
                        .receiver(fTarget)
                        .flag(MessageFlag.PLACEHOLDER_CONTEXT_SENDER, false)
                        .format(Localization.Command.Emit::format)
                        .message(message)
                        .destination(destination)
                        .sound(emitModule.soundOrThrow())
                        .build()
                );
            }
        }

        return event.withProcessed(true);
    }

}
