package net.flectone.pulse.module.command.spy.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.command.spy.model.SpyMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpyProxyMessageListener implements PulseListener {

    private final SpyModule spyModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_SPY) return event;
        if (!moduleController.isEnable(spyModule)) return event.withProcessed(true);
        if (!spyModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            String action = proxyPayload.readString();
            String message = proxyPayload.readString();

            messageDispatcher.dispatch(spyModule, SpyMetadata.<Localization.Command.Spy>builder()
                    .base(EventMetadata.<Localization.Command.Spy>builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .format(Localization.Command.Spy::formatLog)
                            .range(Range.get(Range.Type.SERVER))
                            .destination(spyModule.config().destination())
                            .message(message)
                            .filter(spyModule.createFilter(event.sender() instanceof FPlayer fPlayer ? fPlayer : FPlayer.UNKNOWN, List.of()))
                            .build()
                    )
                    .turned(true)
                    .action(action)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
