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
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.server.MinecraftServer;

@Singleton
public class VanishIntegration implements FIntegration {

    private final FabricFlectonePulse fabricFlectonePulse;
    @Getter private final FLogger fLogger;

    @Inject
    public VanishIntegration(FabricFlectonePulse fabricFlectonePulse,
                             FPlayerService fPlayerService,
                             QuitModule quitModule,
                             JoinModule joinModule,
                             FLogger fLogger) {
        this.fabricFlectonePulse = fabricFlectonePulse;
        this.fLogger = fLogger;

        VanishEvents.VANISH_EVENT.register((player, vanish) -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(player.getUuid());

            if (vanish) {
                quitModule.send(fPlayer, true);
            } else {
                joinModule.send(fPlayer, true);
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
