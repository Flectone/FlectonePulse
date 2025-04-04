package net.flectone.pulse.module.integration.skinsrestorer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;

@Singleton
public class SkinsRestorerModule extends AbstractModule {

    private final Integration.Skinsrestorer config;
    private final Permission.Integration.Skinsrestorer permission;

    private final SkinsRestorerIntegration skinsRestorerIntegration;

    @Inject
    public SkinsRestorerModule(FileManager fileManager,
                               SkinsRestorerIntegration skinsRestorerIntegration) {
        this.skinsRestorerIntegration = skinsRestorerIntegration;

        config = fileManager.getIntegration().getSkinsrestorer();
        permission = fileManager.getPermission().getIntegration().getSkinsrestorer();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        skinsRestorerIntegration.hook();
    }

    @Override
    public boolean isConfigEnable() {
        return config.isEnable();
    }

    public String getTextureUrl(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        return skinsRestorerIntegration.getTextureUrl(fPlayer);
    }

}
