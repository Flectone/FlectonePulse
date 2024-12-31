package net.flectone.pulse.module.integration.skinsrestorer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.module.integration.FIntegration;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;

import java.util.Optional;

@Singleton
public class SkinsRestorerIntegration implements FIntegration {

    private final FLogger fLogger;

    private SkinsRestorer skinsRestorer;

    @Inject
    public SkinsRestorerIntegration(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        try {
            skinsRestorer = SkinsRestorerProvider.get();

            fLogger.info("SkinsRestorer hooked");
        } catch (IllegalStateException e) {
            fLogger.warning("SkinsRestorer hook is failed, check https://github.com/SkinsRestorer/SkinsRestorer/blob/dev/shared/src/main/resources/proxy_warning.txt");
        }
    }

    public String getTextureUrl(FPlayer fPlayer) {
        if (skinsRestorer == null) return null;

        PlayerStorage storage = skinsRestorer.getPlayerStorage();
        try {
            Optional<SkinProperty> skin = storage.getSkinForPlayer(fPlayer.getUuid(), fPlayer.getName());
            return skin.map(PropertyUtils::getSkinTextureUrlStripped).orElse(null);
        } catch (DataRequestException e) {
            return null;
        }
    }
}
