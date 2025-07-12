package net.flectone.pulse.module.integration.luckperms;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;

import java.util.Collections;
import java.util.Set;

@Singleton
public class LuckPermsModule extends AbstractModule {

    private final Integration.Luckperms integration;
    private final Permission.Integration.Luckperms permission;

    private final LuckPermsIntegration luckPermsIntegration;

    @Inject
    public LuckPermsModule(FileResolver fileResolver,
                           LuckPermsIntegration luckPermsIntegration) {
        this.luckPermsIntegration = luckPermsIntegration;

        integration = fileResolver.getIntegration().getLuckperms();
        permission = fileResolver.getPermission().getIntegration().getLuckperms();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        luckPermsIntegration.hook();
    }

    @Override
    public void onDisable() {
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
        if (checkModulePredicates(fPlayer)) return null;

        return luckPermsIntegration.getPrefix(fPlayer);
    }

    public String getSuffix(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        return luckPermsIntegration.getSuffix(fPlayer);
    }

    public Set<String> getGroups() {
        if (!isEnable()) return Collections.emptySet();

        return luckPermsIntegration.getGroups();
    }
}
