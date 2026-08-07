package net.flectone.pulse.module.integration.luckperms;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.PlatformType;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.LazyInstance;

import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LuckPermsModuleImpl implements LuckPermsModule {

    private final FileFacade fileFacade;
    private final PlatformServerAdapter platformServerAdapter;
    private final ModuleController moduleController;
    private final LazyInstance<LuckPermsIntegration> luckPermsIntegration;

    @Override
    public void onEnable() {
        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC
                || platformServerAdapter.getPlatformType() == PlatformType.NEOFORGE) {
            // delay for init
            luckPermsIntegration.get().hookLater();
        } else {
            luckPermsIntegration.get().hook();
        }
    }

    @Override
    public void onDisable() {
        luckPermsIntegration.get().unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_LUCKPERMS;
    }

    @Override
    public Integration.Luckperms config() {
        return fileFacade.integration().luckperms();
    }

    @Override
    public Permission.Integration.Luckperms permission() {
        return fileFacade.permission().integration().luckperms();
    }

    @Override
    public boolean hasLuckPermission(FPlayer fPlayer, String permission) {
        if (!moduleController.isEnable(this)) return false;

        return luckPermsIntegration.get().hasPermission(fPlayer, permission);
    }

    @Override
    public boolean isAlwaysHaveTrue() {
        return moduleController.isEnable(this) && config().alwaysHaveTrue();
    }

    @Override
    public int getGroupWeight(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return 0;
        if (!config().tabSort()) return 0;

        return luckPermsIntegration.get().getGroupWeight(fPlayer);
    }

    @Override
    public String getPrefix(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        return luckPermsIntegration.get().getPrefix(fPlayer);
    }

    @Override
    public String getSuffix(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        return luckPermsIntegration.get().getSuffix(fPlayer);
    }

    @Override
    public Set<String> getGroups() {
        if (!moduleController.isEnable(this)) return Set.of();

        return luckPermsIntegration.get().getGroups();
    }

}
