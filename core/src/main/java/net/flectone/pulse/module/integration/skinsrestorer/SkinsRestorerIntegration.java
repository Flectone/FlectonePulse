package net.flectone.pulse.module.integration.skinsrestorer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Singleton
public class SkinsRestorerIntegration implements FIntegration {

    private final Cache<UUID, SkinProperty> skinPropertyCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    private final FlectonePulse flectonePulse;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final FLogger fLogger;

    private SkinsRestorer skinsRestorer;
    private boolean skinApplyEventSubscribed;

    @Inject
    public SkinsRestorerIntegration(FlectonePulse flectonePulse,
                                    PlatformPlayerAdapter platformPlayerAdapter,
                                    FLogger fLogger) {
        this.flectonePulse = flectonePulse;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        try {
            skinsRestorer = SkinsRestorerProvider.get();

            if (!skinApplyEventSubscribed) {
                skinsRestorer.getEventBus().subscribe(flectonePulse, SkinApplyEvent.class, event -> {
                    UUID uuid = platformPlayerAdapter.getUUID(event.getPlayer(platformPlayerAdapter.getPlayerClass()));
                    if (uuid == null) return;

                    skinPropertyCache.put(uuid, event.getProperty());
                });

                skinApplyEventSubscribed = true;
            }

            fLogger.info("✔ SkinsRestorer hooked");
        } catch (Exception e) {
            fLogger.warning("SkinsRestorer hook is failed, check https://skinsrestorer.net/docs/installation");
        }
    }

    @Async(delay = 20)
    public void hookLater() {
        hook();
    }

    @Override
    public void unhook() {
        fLogger.info("✖ SkinsRestorer unhooked");
    }

    private SkinProperty getSkinPropertyFromCache(FPlayer fPlayer) {
        if (skinsRestorer == null) return null;

        try {
            return skinPropertyCache.get(fPlayer.getUuid(), () -> getSkinProperty(fPlayer));
        } catch (ExecutionException e) {
            fLogger.warning(e);
            return getSkinProperty(fPlayer);
        }
    }

    private SkinProperty getSkinProperty(FPlayer fPlayer) {
        if (skinsRestorer == null) return null;

        PlayerStorage storage = skinsRestorer.getPlayerStorage();
        try {
            Optional<SkinProperty> skin = storage.getSkinForPlayer(fPlayer.getUuid(), fPlayer.getName());
            return skin.orElse(null);
        } catch (DataRequestException e) {
            return null;
        }
    }

    public String getTextureUrl(FPlayer fPlayer) {
        SkinProperty skinProperty = getSkinPropertyFromCache(fPlayer);
        if (skinProperty == null) return null;

        return PropertyUtils.getSkinTextureHash(skinProperty);
    }

    public PlayerHeadObjectContents.ProfileProperty getProfileProperty(FPlayer fPlayer) {
        SkinProperty skinProperty = getSkinPropertyFromCache(fPlayer);
        if (skinProperty == null) return null;

        return PlayerHeadObjectContents.property("textures", skinProperty.getValue(), skinProperty.getSignature());
    }
}
