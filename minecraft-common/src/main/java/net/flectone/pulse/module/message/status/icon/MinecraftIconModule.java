package net.flectone.pulse.module.message.status.icon;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.converter.IconConverter;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.status.icon.listener.MinecraftPacketIconListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.MinecraftServerStatusFormatter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.random.RandomGenerator;

import java.nio.file.Path;

@Singleton
public class MinecraftIconModule extends IconModuleImpl {

    private final FPlayerService fPlayerService;
    private final ModuleController moduleController;
    private final ListenerRegistry listenerRegistry;
    private final MinecraftServerStatusFormatter serverStatusFormatter;

    @Inject
    public MinecraftIconModule(FileFacade fileFacade,
                               PlatformServerAdapter platformServerAdapter,
                               ModuleController moduleController,
                               RandomGenerator randomGenerator,
                               IconConverter iconConverter,
                               FLogger fLogger,
                               @Named("projectPath") Path projectPath,
                               @Named("imagePath") Path iconPath,
                               FPlayerService fPlayerService,
                               ListenerRegistry listenerRegistry,
                               MinecraftServerStatusFormatter serverStatusFormatter) {
        super(fileFacade, platformServerAdapter, moduleController, randomGenerator, iconConverter, fLogger, projectPath, iconPath);

        this.fPlayerService = fPlayerService;
        this.moduleController = moduleController;
        this.listenerRegistry = listenerRegistry;
        this.serverStatusFormatter = serverStatusFormatter;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftPacketIconListener.class);
    }

    public void update(PacketSendEvent event) {
        User user = event.getUser();

        FPlayer fPlayer = fPlayerService.getFPlayer(user.getAddress().getAddress());
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        event.markForReEncode(true);

        WrapperPlayServerServerData wrapperPlayServerServerData = new WrapperPlayServerServerData(event);
        wrapperPlayServerServerData.setIcon(serverStatusFormatter.formatIcon(next(fPlayer)));
    }

}
