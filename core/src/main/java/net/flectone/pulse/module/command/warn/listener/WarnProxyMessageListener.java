package net.flectone.pulse.module.command.warn.listener;

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
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.warn.WarnModule;
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
public class WarnProxyMessageListener implements PulseListener {

    private final FileFacade fileFacade;
    private final WarnModule warnModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final Gson gson;
    private final FPlayerService fPlayerService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessagePipeline messagePipeline;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_WARN) return event;
        if (warnModule.config().filterByServer() && !event.server().equals(fileFacade.config().server())) return event.withProcessed(true);
        if (!warnModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            Moderation warn = gson.fromJson(proxyPayload.readString(), Moderation.class);

            FPlayer fModerator = fPlayerService.getFPlayer(warn.moderator());
            if (moduleController.isDisabledFor(warnModule, fModerator)) return event.withProcessed(true);

            messageDispatcher.dispatch(warnModule, ModerationMetadata.<Localization.Command.Warn>builder()
                    .base(EventMetadata.<Localization.Command.Warn>builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .format((fReceiver, localization) ->
                                    moderationMessageFormatter.replacePlaceholders(localization.server(), fReceiver, warn)
                            )
                            .range(Range.get(Range.Type.SERVER))
                            .destination(warnModule.config().destination())
                            .sound(warnModule.soundOrThrow())
                            .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.targetTag("moderator", fResolver, fModerator)})
                            .build()
                    )
                    .moderation(warn)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
