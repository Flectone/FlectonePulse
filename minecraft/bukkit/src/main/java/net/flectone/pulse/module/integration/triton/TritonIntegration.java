package net.flectone.pulse.module.integration.triton;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.rexcantor64.triton.api.TritonAPI;
import com.rexcantor64.triton.api.events.PlayerChangeLanguageSpigotEvent;
import com.rexcantor64.triton.api.players.LanguagePlayer;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TritonIntegration implements Listener, FIntegration {

    private final FPlayerService fPlayerService;
    private final FLogger fLogger;

    @Override
    public void hook() {
        fLogger.info("✔ Triton hooked");
    }

    @Override
    public void unhook() {
        fLogger.info("✖ Triton unhooked");
    }

    @EventHandler
    public void onPlayerChangeLanguageSpigotEvent(PlayerChangeLanguageSpigotEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getLanguagePlayer().getUUID());
        String newLanguage = event.getNewLanguage().getLanguageId();

        SettingText setting = SettingText.LOCALE;
        fPlayerService.saveOrUpdateSetting(fPlayer.withSetting(setting, newLanguage), setting);
    }

    public @Nullable String getLocale(FPlayer fPlayer) {
        LanguagePlayer languagePlayer = TritonAPI.getInstance().getPlayerManager().get(fPlayer.uuid());
        if (languagePlayer == null) return null;

        return languagePlayer.getLanguageId();
    }
}
