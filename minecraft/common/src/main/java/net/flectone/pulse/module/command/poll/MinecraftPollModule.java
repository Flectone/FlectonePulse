package net.flectone.pulse.module.command.poll;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.command.poll.builder.DialogPollBuilder;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.incendo.cloud.meta.CommandMeta;

@Singleton
public class MinecraftPollModule extends PollModule {

    private final PacketProvider packetProvider;
    private final Provider<DialogPollBuilder> dialogPollBuilderProvider;

    @Inject
    public MinecraftPollModule(FileFacade fileFacade,
                               FPlayerService fPlayerService,
                               ProxySender proxySender,
                               TaskScheduler taskScheduler,
                               CommandParserProvider commandParserProvider,
                               MessagePipeline messagePipeline,
                               FLogger fLogger,
                               PacketProvider packetProvider,
                               Provider<DialogPollBuilder> dialogPollBuilderProvider) {
        super(fileFacade, fPlayerService, proxySender, taskScheduler, commandParserProvider, messagePipeline, fLogger);

        this.packetProvider = packetProvider;
        this.dialogPollBuilderProvider = dialogPollBuilderProvider;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (config().enableGui() && packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_6)) {
            registerCustomCommand(manager ->
                    manager.commandBuilder(getCommandName() + "gui", CommandMeta.empty())
                            .permission(permission().create().name())
                            .handler(commandContext -> dialogPollBuilderProvider.get().openDialog(commandContext.sender()))
            );
        }

    }

}
