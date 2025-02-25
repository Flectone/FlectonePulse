package net.flectone.pulse.module.integration.triton;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.rexcantor64.triton.api.TritonAPI;
import com.rexcantor64.triton.api.events.PlayerChangeLanguageSpigotEvent;
import com.rexcantor64.triton.api.players.LanguagePlayer;
import net.flectone.pulse.database.dao.SettingDAO;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

@Singleton
public class TritonIntegration implements Listener, FIntegration {

    private final FPlayerManager fPlayerManager;
    private final SettingDAO settingDAO;
    private final FLogger fLogger;

    @Inject
    public TritonIntegration(FPlayerManager fPlayerManager,
                             SettingDAO settingDAO,
                             FLogger fLogger) {
        this.fPlayerManager = fPlayerManager;
        this.settingDAO = settingDAO;
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
        fPlayer.setSetting(FPlayer.Setting.LOCALE, event.getNewLanguage().getLanguageId());

        settingDAO.insertOrUpdate(fPlayer, FPlayer.Setting.LOCALE);
    }

    @Nullable
    public String getLocale(FPlayer fPlayer) {
        LanguagePlayer languagePlayer = TritonAPI.getInstance().getPlayerManager().get(fPlayer.getUuid());
        if (languagePlayer == null) return null;

        return languagePlayer.getLanguageId();
    }
}
