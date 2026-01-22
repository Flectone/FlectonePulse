package net.flectone.pulse.module.integration.vault;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.file.FileFacade;

import java.util.Collections;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VaultModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final VaultIntegration vaultIntegration;

    @Override
    public void onEnable() {
        super.onEnable();

        vaultIntegration.hook();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        vaultIntegration.unhook();
    }

    @Override
    public Integration.Vault config() {
        return fileFacade.integration().vault();
    }

    @Override
    public Permission.Integration.Vault permission() {
        return fileFacade.permission().integration().vault();
    }

    public boolean hasVaultPermission(FPlayer fPlayer, String permission) {
        if (!isEnable()) return false;

        return vaultIntegration.hasPermission(fPlayer, permission);
    }

    public String getPrefix(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return vaultIntegration.getPrefix(fPlayer);
    }

    public String getSuffix(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return vaultIntegration.getSuffix(fPlayer);
    }

    public Set<String> getGroups() {
        if (!isEnable()) return Collections.emptySet();

        return vaultIntegration.getGroups();
    }
}
