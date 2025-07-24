package net.flectone.pulse.module.integration.skinsrestorer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.PlatformType;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class SkinsRestorerModule extends AbstractModule {

    private final Integration.Skinsrestorer config;
    private final Permission.Integration.Skinsrestorer permission;
    private final SkinsRestorerIntegration skinsRestorerIntegration;
    private final PlatformServerAdapter platformServerAdapter;

    @Inject
    public SkinsRestorerModule(FileResolver fileResolver,
                               SkinsRestorerIntegration skinsRestorerIntegration,
                               PlatformServerAdapter platformServerAdapter) {
        this.config = fileResolver.getIntegration().getSkinsrestorer();
        this.permission = fileResolver.getPermission().getIntegration().getSkinsrestorer();
        this.skinsRestorerIntegration = skinsRestorerIntegration;
        this.platformServerAdapter = platformServerAdapter;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            // delay for init
            skinsRestorerIntegration.hookLater();
        } else {
            skinsRestorerIntegration.hook();
        }

    }

    @Override
    public void onDisable() {
        skinsRestorerIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return config.isEnable();
    }

    public String getTextureUrl(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        return skinsRestorerIntegration.getTextureUrl(fPlayer);
    }

}
