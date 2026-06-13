package net.flectone.pulse.module.command.do_.listener;

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
import net.flectone.pulse.module.command.do_.DoModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DoProxyMessageListener implements PulseListener {

    private final DoModule doModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_DO) return event;
        if (moduleController.isDisabledFor(doModule, event.sender())) return event.withProcessed(true);
        if (!doModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(doModule, EventMetadata.<Localization.Command.CommandDo>builder()
                    .uuid(event.uuid())
                    .sender(event.sender())
                    .format(Localization.Command.CommandDo::format)
                    .message(message)
                    .range(Range.get(Range.Type.SERVER))
                    .destination(doModule.config().destination())
                    .sound(doModule.soundOrThrow())
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
