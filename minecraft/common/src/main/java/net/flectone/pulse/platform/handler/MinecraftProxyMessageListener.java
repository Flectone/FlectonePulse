package net.flectone.pulse.platform.handler;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.data.repository.CooldownRepository;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.message.ProxyMessageListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.command.whitelist.WhitelistModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.tab.playerlist.MinecraftPlayerlistnameModule;
import net.flectone.pulse.service.*;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

import java.io.IOException;
import java.util.UUID;

@Singleton
public class MinecraftProxyMessageListener extends ProxyMessageListener {

    private final FPlayerService fPlayerService;
    private final MinecraftPlayerlistnameModule playerlistnameModule;
    private final MinecraftSkinService minecraftSkinService;

    @Inject
    public MinecraftProxyMessageListener(FileFacade fileFacade,
                                         FPlayerService fPlayerService,
                                         PlaytimeService playtimeService,
                                         SocialService socialService,
                                         FLogger fLogger,
                                         ModerationService moderationService,
                                         Gson gson,
                                         TaskScheduler taskScheduler,
                                         CooldownRepository cooldownRepository,
                                         EventDispatcher eventDispatcher,
                                         QuitModule quitModule,
                                         JoinModule joinModule,
                                         BanModule banModule,
                                         MuteModule muteModule,
                                         MaintenanceModule maintenanceModule,
                                         WarnModule warnModule,
                                         WhitelistModule whitelistModule,
                                         KickModule kickModule,
                                         MinecraftPlayerlistnameModule playerlistnameModule,
                                         MinecraftSkinService minecraftSkinService) {
        super(fileFacade, fPlayerService, playtimeService, socialService, fLogger, moderationService, gson, taskScheduler, cooldownRepository, eventDispatcher, quitModule, joinModule, banModule, muteModule, maintenanceModule, warnModule, whitelistModule, kickModule);

        this.fPlayerService = fPlayerService;
        this.playerlistnameModule = playerlistnameModule;
        this.minecraftSkinService = minecraftSkinService;
    }


    @Override
    public void handleSystemOnline(UUID uuid) {
        super.handleSystemOnline(uuid);

        playerlistnameModule.add(uuid);
    }

    @Override
    public void handleSystemOffline(UUID uuid, boolean connected) throws IOException {
        super.handleSystemOffline(uuid, connected);

        if (connected) {
            playerlistnameModule.remove(uuid);
        }
    }

    @Override
    public void handleSystemSkin(UUID uuid) {
        super.handleSystemSkin(uuid);

        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
        minecraftSkinService.updateProfilePropertyCache(fPlayer);
    }

}
