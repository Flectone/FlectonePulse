package net.flectone.pulse.module.integration.luckperms;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;

import java.util.Collections;
import java.util.Set;

@Singleton
public class LuckPermsModule extends AbstractModule {

    private final Integration.Luckperms integration;
    private final Permission.Integration.Luckperms permission;
    private final LuckPermsIntegration luckPermsIntegration;
    private final PlatformServerAdapter platformServerAdapter;

    @Inject
    public LuckPermsModule(FileResolver fileResolver,
                           LuckPermsIntegration luckPermsIntegration,
                           PlatformServerAdapter platformServerAdapter) {
        this.integration = fileResolver.getIntegration().getLuckperms();
        this.permission = fileResolver.getPermission().getIntegration().getLuckperms();
        this.luckPermsIntegration = luckPermsIntegration;
        this.platformServerAdapter = platformServerAdapter;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            // delay for init
            luckPermsIntegration.hookLater();
        } else {
            luckPermsIntegration.hook();
        }
    }

    @Override
    public void onDisable() {
        luckPermsIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean hasLuckPermission(FPlayer fPlayer, String permission) {
        if (!isEnable()) return false;

        return luckPermsIntegration.hasPermission(fPlayer, permission);
    }

    public int getGroupWeight(FPlayer fPlayer) {
        if (!isEnable()) return 0;
        if (!integration.isTabSort()) return 0;

        return luckPermsIntegration.getGroupWeight(fPlayer);
    }

    public String getPrefix(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return luckPermsIntegration.getPrefix(fPlayer);
    }

    public String getSuffix(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return luckPermsIntegration.getSuffix(fPlayer);
    }

    public Set<String> getGroups() {
        if (!isEnable()) return Collections.emptySet();

        return luckPermsIntegration.getGroups();
    }
}
