package net.flectone.pulse.module.integration.supervanish;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Singleton
public class SuperVanishIntegration implements Listener, FIntegration {

    private final FPlayerManager fPlayerManager;
    private final QuitModule quitModule;
    private final JoinModule joinModule;
    private final FLogger fLogger;

    @Inject
    public SuperVanishIntegration(FPlayerManager fPlayerManager,
                                  QuitModule quitModule,
                                  JoinModule joinModule,
                                  FLogger fLogger) {
        this.fPlayerManager = fPlayerManager;
        this.quitModule = quitModule;
        this.joinModule = joinModule;
        this.fLogger = fLogger;
    }


    @Override
    public void hook() {
        fLogger.info("SuperVanish hooked");
    }

    @EventHandler
    public void onHide(PlayerHideEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerManager.get(event.getPlayer());

        quitModule.send(fPlayer);
        event.setSilent(true);
    }

    @EventHandler
    public void onShow(PlayerShowEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerManager.get(event.getPlayer());

        joinModule.send(fPlayer, false);
        event.setSilent(true);
    }
}
