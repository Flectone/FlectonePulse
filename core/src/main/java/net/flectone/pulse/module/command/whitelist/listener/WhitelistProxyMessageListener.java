package net.flectone.pulse.module.command.whitelist.listener;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.ModerationMetadata;
import net.flectone.pulse.model.event.UnModerationMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.whitelist.WhitelistModule;
import net.flectone.pulse.module.command.whitelist.model.WhitelistMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.io.ProxyPayload;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

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

                    messageDispatcher.dispatch(whitelistModule, WhitelistMetadata.<Localization.Command.Whitelist>builder()
                            .base(EventMetadata.<Localization.Command.Whitelist>builder()
                                    .sender(event.sender())
                                    .format(localization -> turnedOn ? localization.formatOn() : localization.formatOff())
                                    .destination(whitelistModule.config().destination())
                                    .sound(whitelistModule.soundOrThrow())
                                    .range(Range.get(Range.Type.SERVER))
                                    .build()
                            )
                            .turnedOn(turnedOn)
                            .build()
                    );
                }
                case ADD -> {
                    Moderation whitelist = gson.fromJson(proxyPayload.readString(), Moderation.class);

                    FPlayer fModerator = fPlayerService.getFPlayer(whitelist.moderator());
                    if (moduleController.isDisabledFor(whitelistModule, fModerator)) return event.withProcessed(true);

                    messageDispatcher.dispatch(whitelistModule, ModerationMetadata.<Localization.Command.Whitelist>builder()
                            .base(EventMetadata.<Localization.Command.Whitelist>builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .format((fReceiver, localization) ->
                                            moderationMessageFormatter.replacePlaceholders(localization.formatAdd(), fReceiver, whitelist)
                                    )
                                    .range(Range.get(Range.Type.SERVER))
                                    .destination(whitelistModule.config().destination())
                                    .sound(whitelistModule.soundOrThrow())
                                    .tagResolvers(fResolver -> new TagResolver[]{
                                            messagePipeline.targetTag("moderator", fResolver, fModerator)
                                    })
                                    .build()
                            )
                            .moderation(whitelist)
                            .build()
                    );
                }
                case REMOVE -> {
                    Moderation unwhitelist = gson.fromJson(proxyPayload.readString(), Moderation.class);

                    FPlayer fModerator = fPlayerService.getFPlayer(unwhitelist.moderator());
                    if (moduleController.isDisabledFor(whitelistModule, fModerator)) return event.withProcessed(true);

                    messageDispatcher.dispatch(whitelistModule, UnModerationMetadata.<Localization.Command.Whitelist>builder()
                            .base(EventMetadata.<Localization.Command.Whitelist>builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .format((fReceiver, localization) ->
                                            moderationMessageFormatter.replacePlaceholders(localization.formatRemove(), fReceiver, unwhitelist)
                                    )
                                    .destination(whitelistModule.config().destination())
                                    .range(Range.get(Range.Type.SERVER))
                                    .sound(whitelistModule.soundOrThrow())
                                    .tagResolvers(fResolver -> new TagResolver[]{
                                            messagePipeline.targetTag("moderator", fResolver, fModerator)
                                    })
                                    .build()
                            )
                            .unmoderation(unwhitelist)
                            .build()
                    );
                }
            }
        }

        return event.withProcessed(true);
    }

}
