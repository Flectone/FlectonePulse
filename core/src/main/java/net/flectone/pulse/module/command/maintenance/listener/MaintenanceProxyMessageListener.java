package net.flectone.pulse.module.command.maintenance.listener;

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
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.command.maintenance.model.MaintenanceMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.io.ProxyPayload;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.IOException;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MaintenanceProxyMessageListener implements PulseListener {

    private final FileFacade fileFacade;
    private final MaintenanceModule maintenanceModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final Gson gson;
    private final FPlayerService fPlayerService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessagePipeline messagePipeline;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_MAINTENANCE) return event;
        if (maintenanceModule.config().filterByServer() && !event.server().equals(fileFacade.config().server())) return event.withProcessed(true);
        if (!moduleController.isEnable(maintenanceModule)) return event.withProcessed(true);
        if (!maintenanceModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            Moderation maintenance = gson.fromJson(proxyPayload.readString(), Moderation.class);

            FPlayer fModerator = fPlayerService.getFPlayer(maintenance.moderator());
            if (moduleController.isDisabledFor(maintenanceModule, fModerator)) return event.withProcessed(true);

            boolean turned = proxyPayload.readBoolean();

            messageDispatcher.dispatch(maintenanceModule, MaintenanceMetadata.<Localization.Command.Maintenance>builder()
                    .base(EventMetadata.<Localization.Command.Maintenance>builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .format((fReceiver, localization) ->
                                    moderationMessageFormatter.replacePlaceholders(turned ? localization.formatTrue() : localization.formatFalse(), fReceiver, maintenance)
                            )
                            .destination(maintenanceModule.config().destination())
                            .sound(maintenanceModule.soundOrThrow())
                            .proxy(dataOutputStream -> {
                                dataOutputStream.writeAsJson(maintenance);
                                dataOutputStream.writeBoolean(turned);
                            })
                            .integration(IntegrationMetadata.builder()
                                    .messageNames(List.of(maintenanceModule.name().name() + "_" + String.valueOf(turned).toUpperCase()))
                                    .build()
                            )
                            .tagResolvers(fResolver -> new TagResolver[]{
                                    messagePipeline.targetTag("moderator", fResolver, fModerator)
                            })
                            .build()
                    )
                    .moderation(maintenance)
                    .turned(turned)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
