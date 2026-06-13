package net.flectone.pulse.module.command.me.listener;

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
import net.flectone.pulse.module.command.me.MeModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MeProxyMessageListener implements PulseListener {

    private final MeModule meModule;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_ME) return event;
        if (moduleController.isDisabledFor(meModule, event.sender())) return event.withProcessed(true);
        if (!meModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(meModule, EventMetadata.<Localization.Command.Me>builder()
                    .uuid(event.uuid())
                    .sender(event.sender())
                    .format(Localization.Command.Me::format)
                    .range(Range.get(Range.Type.SERVER))
                    .destination(meModule.config().destination())
                    .message(message)
                    .sound(meModule.soundOrThrow())
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
