package net.flectone.pulse.module.integration.triton;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.rexcantor64.triton.api.TritonAPI;
import com.rexcantor64.triton.api.events.PlayerChangeLanguageSpigotEvent;
import com.rexcantor64.triton.api.players.LanguagePlayer;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

@Singleton
public class TritonIntegration implements Listener, FIntegration {

    private final FPlayerManager fPlayerManager;
    private final ThreadManager threadManager;
    private final FLogger fLogger;

    @Inject
    public TritonIntegration(FPlayerManager fPlayerManager,
                             ThreadManager threadManager,
                             FLogger fLogger) {
        this.fPlayerManager = fPlayerManager;
        this.threadManager = threadManager;
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        fLogger.info("Triton hooked");
    }

    @EventHandler
    public void onPlayerChangeLanguageSpigotEvent(PlayerChangeLanguageSpigotEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerManager.get(event.getLanguagePlayer().getUUID());
        fPlayer.setLocale(event.getNewLanguage().getLanguageId());
        threadManager.runDatabase(database -> database.updateFPlayer(fPlayer));
    }

    @Nullable
    public String getLocale(FPlayer fPlayer) {
        LanguagePlayer languagePlayer = TritonAPI.getInstance().getPlayerManager().get(fPlayer.getUuid());
        if (languagePlayer == null) return null;

        return languagePlayer.getLanguageId();
    }
}
