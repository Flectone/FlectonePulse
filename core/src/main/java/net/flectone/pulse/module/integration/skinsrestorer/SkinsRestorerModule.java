package net.flectone.pulse.module.integration.skinsrestorer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.PlatformType;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SkinsRestorerModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final SkinsRestorerIntegration skinsRestorerIntegration;
    private final PlatformServerAdapter platformServerAdapter;

    @Override
    public void onEnable() {
        super.onEnable();

        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            // delay for init
            skinsRestorerIntegration.hookLater();
        } else {
            skinsRestorerIntegration.hook();
        }

    }

    @Override
    public void onDisable() {
        super.onDisable();

        skinsRestorerIntegration.unhook();
    }

    @Override
    public Integration.Skinsrestorer config() {
        return fileResolver.getIntegration().getSkinsrestorer();
    }

    @Override
    public Permission.Integration.Skinsrestorer permission() {
        return fileResolver.getPermission().getIntegration().getSkinsrestorer();
    }

    public String getTextureUrl(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return skinsRestorerIntegration.getTextureUrl(fPlayer);
    }

    public PlayerHeadObjectContents.ProfileProperty getProfileProperty(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return skinsRestorerIntegration.getProfileProperty(fPlayer);
    }

}
