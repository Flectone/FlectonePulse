package net.flectone.pulse.module.integration.supervanish;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Singleton
public class SuperVanishIntegration implements Listener, FIntegration {

    private final FPlayerService fPlayerService;
    private final QuitModule quitModule;
    private final JoinModule joinModule;
    private final FLogger fLogger;

    @Inject
    public SuperVanishIntegration(FPlayerService fPlayerService,
                                  QuitModule quitModule,
                                  JoinModule joinModule,
                                  FLogger fLogger) {
        this.fPlayerService = fPlayerService;
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

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer());
        handlePlayerQuit(fPlayer);

        event.setSilent(true);
    }

    @Async
    public void handlePlayerQuit(FPlayer fPlayer) {
        quitModule.send(fPlayer);
    }

    @EventHandler
    public void onShow(PlayerShowEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer());

        joinModule.send(fPlayer);
        event.setSilent(true);
    }
}
