package net.flectone.pulse.module.command.try_.listener;

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

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_TRY) return event;
        if (moduleController.isDisabledFor(tryModule, event.sender())) return event.withProcessed(true);
        if (!tryModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            int value = proxyPayload.readInt();
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(tryModule, TryMetadata.<Localization.Command.CommandTry>builder()
                    .base(EventMetadata.<Localization.Command.CommandTry>builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .format(tryModule.replacePercent(value))
                            .range(Range.get(Range.Type.SERVER))
                            .destination(tryModule.config().destination())
                            .message(message)
                            .sound(tryModule.soundOrThrow())
                            .build()
                    )
                    .percent(value)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
