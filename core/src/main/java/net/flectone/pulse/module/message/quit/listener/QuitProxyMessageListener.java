package net.flectone.pulse.module.message.quit.listener;

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
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.quit.model.QuitMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class QuitProxyMessageListener implements PulseListener {

    private final QuitModule quitModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final SocialService socialService;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.MESSAGE_QUIT) return event;
        if (moduleController.isDisabledFor(quitModule, event.sender())) return event.withProcessed(true);
        if (!quitModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = new ProxyPayload(event.payload())) {
            boolean fakeMessage = proxyPayload.readBoolean();
            boolean vanished = proxyPayload.readBoolean();

            messageDispatcher.dispatch(quitModule, QuitMetadata.<Localization.Message.Quit>builder()
                    .base(EventMetadata.<Localization.Message.Quit>builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .format(Localization.Message.Quit::format)
                            .destination(quitModule.config().destination())
                            .range(Range.get(Range.Type.SERVER))
                            .sound(quitModule.soundOrThrow())
                            .filter(fReceiver -> fakeMessage || socialService.canSeeVanished(event.sender(), fReceiver, vanished))
                            .build()
                    )
                    .fakeMessage(fakeMessage)
                    .vanished(vanished)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
