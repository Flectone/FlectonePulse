package net.flectone.pulse.module.command.unmute.listener;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.ModerationMessageContext;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.util.payload.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UnmuteProxyMessageListener implements PulseListener {

    private final FileFacade fileFacade;
    private final MuteModule muteModule;
    private final UnmuteModule unmuteModule;
    private final FPlayerService fPlayerService;
    private final ModuleController moduleController;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;
    private final Gson gson;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_UNMUTE) return event;
        if (muteModule.config().filterByServer() && !event.server().equals(fileFacade.config().server())) return event.withProcessed(true);
        if (!muteModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            Moderation unmute = gson.fromJson(proxyPayload.readString(), Moderation.class);

            FPlayer fModerator = fPlayerService.getFPlayer(unmute.moderator());
            if (moduleController.isDisabledFor(unmuteModule, fModerator)) return event.withProcessed(true);

            messageDispatcher.dispatch(unmuteModule, EventMetadata.builder()
                    .destination(unmuteModule.config().destination())
                    .range(Range.get(Range.Type.SERVER))
                    .sound(unmuteModule.soundOrThrow())
                    .messageContext(fResolver -> ModerationMessageContext.builder()
                            .base(MessageContext.builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .receiver(fResolver)
                                    .message(moderationMessageFormatter.replacePlaceholders(unmuteModule.localization(fResolver).format(), fResolver, unmute))
                                    .tagResolver(messagePipeline.targetTag("moderator", fResolver, fModerator))
                                    .build()
                            )
                            .moderation(unmute)
                            .build()
                    )
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
