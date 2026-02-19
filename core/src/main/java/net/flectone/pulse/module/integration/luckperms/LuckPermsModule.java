package net.flectone.pulse.module.integration.luckperms;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.file.FileFacade;

import java.util.Collections;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LuckPermsModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final LuckPermsIntegration luckPermsIntegration;
    private final PlatformServerAdapter platformServerAdapter;

    @Override
    public void onEnable() {
        super.onEnable();

        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            // delay for init
            luckPermsIntegration.hookLater();
        } else {
            luckPermsIntegration.hook();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        luckPermsIntegration.unhook();
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
        if (!isEnable()) return false;

        return luckPermsIntegration.hasPermission(fPlayer, permission);
    }

    public int getGroupWeight(FPlayer fPlayer) {
        if (!isEnable()) return 0;
        if (!config().tabSort()) return 0;

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
