package net.flectone.pulse.module.integration.skinsrestorer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MinecraftSkinService;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SkinsRestorerIntegration implements FIntegration {

    private final FlectonePulse flectonePulse;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final Provider<MinecraftSkinService> skinServiceProvider;
    private final Provider<PlayerlistnameModule> playerlistnameModuleProvider;
    private final TaskScheduler taskScheduler;
    private final FLogger fLogger;

    private SkinsRestorer skinsRestorer;
    private boolean skinApplyEventSubscribed;

    @Override
    public void hook() {
        try {
            skinsRestorer = SkinsRestorerProvider.get();

            if (!skinApplyEventSubscribed) {
                skinsRestorer.getEventBus().subscribe(flectonePulse, SkinApplyEvent.class, event -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer(platformPlayerAdapter.getPlayerClass()));
                    if (fPlayer.isUnknown()) return;

                    skinServiceProvider.get().updateProfilePropertyCache(fPlayer.getUuid(), convertToProfileProperty(event.getProperty()));
                    taskScheduler.runAsyncLater(() -> playerlistnameModuleProvider.get().send(fPlayer), 2L);
                });

                skinApplyEventSubscribed = true;
            }

            fLogger.info("✔ SkinsRestorer hooked");
        } catch (Exception e) {
            fLogger.warning("SkinsRestorer hook is failed, check https://skinsrestorer.net/docs/installation");
        }
    }

    public void hookLater() {
        taskScheduler.runAsyncLater(this::hook);
    }

    @Override
    public void unhook() {
        fLogger.info("✖ SkinsRestorer unhooked");
    }

    public String getTextureUrl(FPlayer fPlayer) {
        SkinProperty skinProperty = getSkinProperty(fPlayer);
        if (skinProperty == null) return null;

        return PropertyUtils.getSkinTextureHash(skinProperty);
    }

    public PlayerHeadObjectContents.ProfileProperty getProfileProperty(FPlayer fPlayer) {
        if (skinsRestorer == null) return null;

        SkinProperty skinProperty = getSkinProperty(fPlayer);
        if (skinProperty == null) return null;

        return convertToProfileProperty(skinProperty);
    }

    private SkinProperty getSkinProperty(FPlayer fPlayer) {
        if (skinsRestorer == null) return null;

        PlayerStorage storage = skinsRestorer.getPlayerStorage();
        try {
            Optional<SkinProperty> skin = storage.getSkinForPlayer(fPlayer.getUuid(), fPlayer.getName());
            return skin.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private PlayerHeadObjectContents.ProfileProperty convertToProfileProperty(SkinProperty skinProperty) {
        return PlayerHeadObjectContents.property(
                "textures",
                skinProperty.getValue(),
                skinProperty.getSignature()
        );
    }
}
