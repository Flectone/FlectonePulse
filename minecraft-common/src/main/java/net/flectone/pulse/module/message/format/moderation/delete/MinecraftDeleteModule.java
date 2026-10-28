package net.flectone.pulse.module.message.format.moderation.delete;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.MessageSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.Component;

@Singleton
public class MinecraftDeleteModule extends DeleteModuleImpl {

    private final MinecraftPacketProvider packetProvider;

    @Inject
    public MinecraftDeleteModule(FileFacade fileFacade,
                                 ListenerRegistry listenerRegistry,
                                 MessagePipeline messagePipeline,
                                 FPlayerService fPlayerService,
                                 MessageSender messageSender,
                                 ModuleController moduleController,
                                 SocialService socialService,
                                 MinecraftPacketProvider packetProvider) {
        super(fileFacade, listenerRegistry, messagePipeline, fPlayerService, messageSender, moduleController, socialService);

        this.packetProvider = packetProvider;
    }

    @Override
    public Component normalize(Component component) {
        ServerVersion serverVersion = packetProvider.getServerVersion();
        AdventureSerializer serializer = AdventureSerializer.serializer(serverVersion.toClientVersion());

        return serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20_3)
                ? serializer.fromNbtTag(serializer.asNbtTag(component))
                : serializer.fromJson(serializer.asJson(component));
    }

}
