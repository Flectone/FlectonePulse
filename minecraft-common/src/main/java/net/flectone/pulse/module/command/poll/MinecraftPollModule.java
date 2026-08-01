package net.flectone.pulse.module.command.poll;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.command.poll.builder.MinecraftDialogPollBuilder;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class MinecraftPollModule extends PollModule {

    private final ModuleCommandController commandModuleController;
    private final MinecraftPacketProvider packetProvider;
    private final Provider<MinecraftDialogPollBuilder> dialogPollBuilderProvider;

    @Inject
    public MinecraftPollModule(FileFacade fileFacade,
                               FPlayerService fPlayerService,
                               ProxySender proxySender,
                               TaskScheduler taskScheduler,
                               CommandParserProvider commandParserProvider,
                               MessagePipeline messagePipeline,
                               MessageDispatcher messageDispatcher,
                               ModuleController moduleController,
                               ModuleCommandController commandModuleController,
                               FLogger fLogger,
                               ComponentSerializer componentSerializer,
                               MinecraftPacketProvider packetProvider,
                               Provider<MinecraftDialogPollBuilder> dialogPollBuilderProvider,
                               ListenerRegistry listenerRegistry,
                               ProxyRegistry proxyRegistry,
                               SocialService socialService) {
        super(fileFacade, fPlayerService, proxySender, taskScheduler, commandParserProvider, messagePipeline, messageDispatcher, moduleController, commandModuleController, componentSerializer, fLogger, proxyRegistry, listenerRegistry, socialService);

        this.commandModuleController = commandModuleController;
        this.packetProvider = packetProvider;
        this.dialogPollBuilderProvider = dialogPollBuilderProvider;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (config().enableGui() && packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_6)) {
            commandModuleController.registerSubCommand(this, config().subCommandGui(), commandBuilder -> commandBuilder
                    .permission(permission().create().name())
                    .handler(commandContext -> dialogPollBuilderProvider.get().openDialog(commandContext.sender()))
            );
        }

    }

}
