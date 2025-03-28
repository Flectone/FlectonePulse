package net.flectone.pulse.module.integration.triton;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.rexcantor64.triton.api.TritonAPI;
import com.rexcantor64.triton.api.events.PlayerChangeLanguageSpigotEvent;
import com.rexcantor64.triton.api.players.LanguagePlayer;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

@Singleton
public class TritonIntegration implements Listener, FIntegration {

    private final FPlayerService fPlayerService;
    private final FLogger fLogger;

    @Inject
    public TritonIntegration(FPlayerService fPlayerService,
                             FLogger fLogger) {
        this.fPlayerService = fPlayerService;
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        fLogger.info("Triton hooked");
    }

    @EventHandler
    public void onPlayerChangeLanguageSpigotEvent(PlayerChangeLanguageSpigotEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getLanguagePlayer().getUUID());

        fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.LOCALE, event.getNewLanguage().getLanguageId());
    }

    @Nullable
    public String getLocale(FPlayer fPlayer) {
        LanguagePlayer languagePlayer = TritonAPI.getInstance().getPlayerManager().get(fPlayer.getUuid());
        if (languagePlayer == null) return null;

        return languagePlayer.getLanguageId();
    }
}
