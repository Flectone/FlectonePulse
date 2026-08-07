package net.flectone.pulse.module.command.whitelist.listener;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.whitelist.WhitelistModule;
import net.flectone.pulse.module.command.whitelist.model.WhitelistMessageContext;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WhitelistProxyMessageListener implements PulseListener {

    private final FileFacade fileFacade;
    private final WhitelistModule whitelistModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final Gson gson;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_WHITELIST) return event;
        if (whitelistModule.config().filterByServer()  && !event.server().equals(fileFacade.config().server())) return event.withProcessed(true);
        if (!whitelistModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            WhitelistModule.Action action = WhitelistModule.Action.values()[proxyPayload.readInt()];
            switch (action) {
                case ON, OFF -> {
                    if (moduleController.isDisabledFor(whitelistModule, event.sender())) return event.withProcessed(true);

                    boolean turnedOn = action == WhitelistModule.Action.ON;

                    messageDispatcher.dispatch(whitelistModule, EventMetadata.builder()
                            .range(Range.Type.SERVER)
                            .destination(whitelistModule.config().destination())
                            .sound(whitelistModule.soundOrThrow())
                            .messageContext(fResolver -> WhitelistMessageContext.builder()
                                    .base(MessageContext.builder()
                                            .sender(event.sender())
                                            .receiver(fResolver)
                                            .message(turnedOn ? whitelistModule.localization(fResolver).formatOn() : whitelistModule.localization(fResolver).formatOff())
                                            .build()
                                    )
                                    .turnedOn(turnedOn)
                                    .build()
                            )
                            .build()
                    );
                }
                case ADD -> {
                    Moderation whitelist = gson.fromJson(proxyPayload.readString(), Moderation.class);

                    FPlayer fModerator = fPlayerService.getFPlayer(whitelist.moderator());
                    if (moduleController.isDisabledFor(whitelistModule, fModerator)) return event.withProcessed(true);

                    messageDispatcher.dispatch(whitelistModule, EventMetadata.builder()
                            .range(Range.Type.SERVER)
                            .destination(whitelistModule.config().destination())
                            .sound(whitelistModule.soundOrThrow())
                            .messageContext(fResolver -> WhitelistMessageContext.builder()
                                    .base(MessageContext.builder()
                                            .uuid(event.uuid())
                                            .sender(event.sender())
                                            .receiver(fResolver)
                                            .message(moderationMessageFormatter.replacePlaceholders(whitelistModule.localization(fResolver).formatAdd(), fResolver, whitelist))
                                            .tagResolver(messagePipeline.targetTag("moderator", fResolver, fModerator))
                                            .build()
                                    )
                                    .moderation(whitelist)
                                    .build()
                            )
                            .build()
                    );
                }
                case REMOVE -> {
                    Moderation unwhitelist = gson.fromJson(proxyPayload.readString(), Moderation.class);

                    FPlayer fModerator = fPlayerService.getFPlayer(unwhitelist.moderator());
                    if (moduleController.isDisabledFor(whitelistModule, fModerator)) return event.withProcessed(true);

                    messageDispatcher.dispatch(whitelistModule, EventMetadata.builder()
                            .range(Range.Type.SERVER)
                            .destination(whitelistModule.config().destination())
                            .sound(whitelistModule.soundOrThrow())
                            .messageContext(fResolver -> WhitelistMessageContext.builder()
                                    .base(MessageContext.builder()
                                            .uuid(event.uuid())
                                            .sender(event.sender())
                                            .receiver(fResolver)
                                            .message(moderationMessageFormatter.replacePlaceholders(whitelistModule.localization(fResolver).formatRemove(), fResolver, unwhitelist))
                                            .tagResolver(messagePipeline.targetTag("moderator", fResolver, fModerator))
                                            .build()
                                    )
                                    .moderation(unwhitelist)
                                    .build()
                            )
                            .build()
                    );
                }
            }
        }

        return event.withProcessed(true);
    }

}
