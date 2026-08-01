package net.flectone.pulse.module.integration.supervanish;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import me.drex.vanish.api.VanishAPI;
import me.drex.vanish.api.VanishEvents;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.server.MinecraftServer;

@Singleton
public class FabricVanishIntegration implements FIntegration {

    private final FabricFlectonePulse fabricFlectonePulse;
    @Getter private final FLogger fLogger;

    @Inject
    public FabricVanishIntegration(FileFacade fileFacade,
                                   FabricFlectonePulse fabricFlectonePulse,
                                   FPlayerService fPlayerService,
                                   SocialService socialService,
                                   QuitModule quitModule,
                                   JoinModule joinModule,
                                   FLogger fLogger) {
        this.fabricFlectonePulse = fabricFlectonePulse;
        this.fLogger = fLogger;

        VanishEvents.VANISH_EVENT.register((player, vanish) -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(player.getUUID());

            if (vanish) {
                boolean sendMessage = fileFacade.integration().supervanish().showFakeQuit();

                // proxy vanish synchronization
                if (fileFacade.integration().supervanish().proxySync()) {
                    if (socialService.getSetting(fPlayer, SettingText.VANISH_STATUS) == null) {
                        socialService.saveSetting(fPlayer, SettingText.VANISH_STATUS, "1");
                    } else {
                        sendMessage = false;
                    }
                }

                if (sendMessage) {
                    quitModule.send(fPlayer, true);
                }
            } else {
                boolean sendMessage = fileFacade.integration().supervanish().showFakeJoin();

                // proxy vanish synchronization
                if (fileFacade.integration().supervanish().proxySync()) {
                    if (socialService.getSetting(fPlayer, SettingText.VANISH_STATUS) != null) {
                        socialService.saveSetting(fPlayer, SettingText.VANISH_STATUS, null);
                    } else {
                        sendMessage = false;
                    }
                }

                if (sendMessage) {
                    joinModule.send(fPlayer, true, false);
                }
            }
        });
    }

    @Override
    public String getIntegrationName() {
        return "Vanish";
    }

    public boolean isVanished(FEntity sender) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        return VanishAPI.isVanished(minecraftServer, sender.uuid());
    }

}
