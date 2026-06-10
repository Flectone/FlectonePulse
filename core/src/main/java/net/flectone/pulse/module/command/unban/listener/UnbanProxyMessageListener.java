package net.flectone.pulse.module.command.unban.listener;

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
import net.flectone.pulse.model.event.UnModerationMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.unban.UnbanModule;
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
public class UnbanProxyMessageListener implements PulseListener {

    private final FileFacade fileFacade;
    private final BanModule banModule;
    private final UnbanModule unbanModule;
    private final FPlayerService fPlayerService;
    private final ModuleController moduleController;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;
    private final Gson gson;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_UNBAN) return event;
        if (banModule.config().filterByServer() && !event.server().equals(fileFacade.config().server())) return event.withProcessed(true);
        if (!banModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            Moderation unban = gson.fromJson(proxyPayload.readString(), Moderation.class);

            FPlayer fModerator = fPlayerService.getFPlayer(unban.moderator());
            if (moduleController.isDisabledFor(unbanModule, fModerator)) return event.withProcessed(true);

            messageDispatcher.dispatch(unbanModule, UnModerationMetadata.<Localization.Command.Unban>builder()
                    .base(EventMetadata.<Localization.Command.Unban>builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .format((fReceiver, localization) ->
                                    moderationMessageFormatter.replacePlaceholders(localization.format(), fReceiver, unban)
                            )
                            .destination(unbanModule.config().destination())
                            .range(Range.get(Range.Type.SERVER))
                            .sound(unbanModule.soundOrThrow())
                            .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.targetTag("moderator", fResolver, fModerator)})
                            .build()
                    )
                    .unmoderation(unban)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
