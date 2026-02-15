package net.flectone.pulse.module.integration.supervanish;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.VanishAPI;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SuperVanishIntegration implements Listener, FIntegration {

    private final FPlayerService fPlayerService;
    private final QuitModule quitModule;
    private final JoinModule joinModule;
    private final FLogger fLogger;

    @Override
    public void hook() {
        fLogger.info("✔ SuperVanish hooked");
    }

    @Override
    public void unhook() {
        fLogger.info("✖ SuperVanish unhooked");
    }

    @EventHandler
    public void onHide(PlayerHideEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());
        quitModule.send(fPlayer, true);

        event.setSilent(true);
    }

    @EventHandler
    public void onShow(PlayerShowEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());
        joinModule.send(fPlayer, true);

        event.setSilent(true);
    }

    public boolean isVanished(FEntity sender) {
        return VanishAPI.isInvisibleOffline(sender.uuid());
    }
}
