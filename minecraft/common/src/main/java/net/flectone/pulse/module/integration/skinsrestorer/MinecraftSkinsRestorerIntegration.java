package net.flectone.pulse.module.integration.skinsrestorer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.tab.playerlist.MinecraftPlayerlistnameModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MinecraftSkinService;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import net.skinsrestorer.api.property.MojangSkinDataResult;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftSkinsRestorerIntegration implements FIntegration {

    private final FlectonePulse flectonePulse;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final Provider<MinecraftSkinService> skinServiceProvider;
    private final Provider<MinecraftPlayerlistnameModule> playerlistnameModuleProvider;
    private final TaskScheduler taskScheduler;
    @Getter private final FLogger fLogger;

    private SkinsRestorer skinsRestorer;
    private boolean skinApplyEventSubscribed;

    @Override
    public String getIntegrationName() {
        return "SkinsRestorer";
    }

    @Override
    public void hook() {
        try {
            skinsRestorer = SkinsRestorerProvider.get();

            if (!skinApplyEventSubscribed) {
                skinsRestorer.getEventBus().subscribe(flectonePulse, SkinApplyEvent.class, event -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer(platformPlayerAdapter.getPlayerClass()));
                    if (fPlayer.isUnknown()) return;

                    skinServiceProvider.get().updateProfilePropertyCache(fPlayer.uuid(), convertToProfileProperty(event.getProperty()));
                    taskScheduler.runAsyncLater(() -> playerlistnameModuleProvider.get().send(fPlayer), 2L);
                });

                skinApplyEventSubscribed = true;
            }

            logHook();
        } catch (Exception _) {
            fLogger.warning("SkinsRestorer hook is failed, check https://skinsrestorer.net/docs/installation");
        }
    }

    public void hookLater() {
        taskScheduler.runAsyncLater(this::hook);
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

        PlayerStorage playerStorage = skinsRestorer.getPlayerStorage();
        try {
            Optional<SkinProperty> skinProperty = playerStorage.getSkinForPlayer(fPlayer.uuid(), fPlayer.name());
            if (skinProperty.isPresent()) return skinProperty.get();

            SkinStorage skinStorage = skinsRestorer.getSkinStorage();
            Optional<MojangSkinDataResult> skinDataResult = skinStorage.getPlayerSkin(fPlayer.uuid().toString(), false);
            return skinDataResult.map(MojangSkinDataResult::getSkinProperty).orElse(null);
        } catch (Exception _) {
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
