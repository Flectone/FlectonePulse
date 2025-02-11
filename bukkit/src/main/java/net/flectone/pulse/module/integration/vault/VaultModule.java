package net.flectone.pulse.module.integration.vault;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;

import java.util.Collections;
import java.util.Set;

@Singleton
public class VaultModule extends AbstractModule {

    private final Integration.Vault integration;
    private final Permission.Integration.Vault permission;

    private final VaultIntegration vaultIntegration;

    @Inject
    public VaultModule(FileManager fileManager,
                       VaultIntegration vaultIntegration) {
        this.vaultIntegration = vaultIntegration;

        integration = fileManager.getIntegration().getVault();
        permission = fileManager.getPermission().getIntegration().getVault();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        vaultIntegration.hook();
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean hasVaultPermission(FPlayer fPlayer, String permission) {
        if (!isEnable()) return false;

        return vaultIntegration.hasPermission(fPlayer, permission);
    }

    public String getPrefix(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        return vaultIntegration.getPrefix(fPlayer);
    }

    public String getSuffix(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        return vaultIntegration.getSuffix(fPlayer);
    }

    public Set<String> getGroups() {
        if (!isEnable()) return Collections.emptySet();;

        return vaultIntegration.getGroups();
    }
}
