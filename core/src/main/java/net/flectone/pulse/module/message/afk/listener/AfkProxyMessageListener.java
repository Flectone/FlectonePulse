package net.flectone.pulse.module.message.afk.listener;

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
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.afk.model.AFKMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AfkProxyMessageListener implements PulseListener {

    private final AfkModule afkModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final SocialService socialService;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.MESSAGE_AFK) return event;
        if (moduleController.isDisabledFor(afkModule, event.sender())) return event.withProcessed(true);
        if (!afkModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = new ProxyPayload(event.payload())) {
            boolean isAfk = proxyPayload.readBoolean();
            boolean vanished = proxyPayload.readBoolean();

            messageDispatcher.dispatch(afkModule, AFKMetadata.<Localization.Message.Afk>builder()
                    .base(EventMetadata.<Localization.Message.Afk>builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .format(localization -> isAfk
                                    ? localization.formatTrue().global()
                                    : localization.formatFalse().global()
                            )
                            .range(Range.get(Range.Type.SERVER))
                            .destination(afkModule.config().destination())
                            .sound(afkModule.soundOrThrow())
                            .filter(fReceiver -> socialService.canSeeVanished(event.sender(), fReceiver, vanished))
                            .build()
                    )
                    .newStatus(isAfk)
                    .fakeMessage(false)
                    .vanished(vanished)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
