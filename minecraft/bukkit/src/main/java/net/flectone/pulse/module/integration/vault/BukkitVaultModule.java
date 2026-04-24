package net.flectone.pulse.module.integration.vault;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

import java.util.Collections;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitVaultModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final BukkitVaultIntegration vaultIntegration;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        vaultIntegration.hook();
    }

    @Override
    public void onDisable() {
        vaultIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_VAULT;
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
        if (!moduleController.isEnable(this)) return false;

        return vaultIntegration.hasPermission(fPlayer, permission);
    }

    public String getPrefix(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        return vaultIntegration.getPrefix(fPlayer);
    }

    public String getSuffix(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        return vaultIntegration.getSuffix(fPlayer);
    }

    public Set<String> getGroups() {
        if (!moduleController.isEnable(this)) return Collections.emptySet();

        return vaultIntegration.getGroups();
    }
}
