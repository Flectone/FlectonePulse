package net.flectone.pulse.module.command.helper.listener;

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

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_HELPER) return event;
        if (!moduleController.isEnable(helperModule)) return event.withProcessed(true);
        if (!helperModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(helperModule, EventMetadata.<Localization.Command.Helper>builder()
                    .uuid(event.uuid())
                    .sender(event.sender())
                    .format(Localization.Command.Helper::global)
                    .range(Range.get(Range.Type.SERVER))
                    .destination(helperModule.config().destination())
                    .message(message)
                    .sound(helperModule.soundOrThrow())
                    .filter(helperModule.getFilterSee())
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
