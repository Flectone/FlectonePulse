package net.flectone.pulse.module.integration.luckperms;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.file.FileFacade;

import java.util.Collections;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LuckPermsModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final LuckPermsIntegration luckPermsIntegration;
    private final PlatformServerAdapter platformServerAdapter;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
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

    public boolean hasLuckPermission(FPlayer fPlayer, String permission) {
        if (!moduleController.isEnable(this)) return false;

        return luckPermsIntegration.hasPermission(fPlayer, permission);
    }

    public boolean isAlwaysHaveTrue() {
        return moduleController.isEnable(this) && config().alwaysHaveTrue();
    }

    public int getGroupWeight(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return 0;
        if (!config().tabSort()) return 0;

        return luckPermsIntegration.getGroupWeight(fPlayer);
    }

    public String getPrefix(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        return luckPermsIntegration.getPrefix(fPlayer);
    }

    public String getSuffix(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        return luckPermsIntegration.getSuffix(fPlayer);
    }

    public Set<String> getGroups() {
        if (!moduleController.isEnable(this)) return Collections.emptySet();

        return luckPermsIntegration.getGroups();
    }

}
