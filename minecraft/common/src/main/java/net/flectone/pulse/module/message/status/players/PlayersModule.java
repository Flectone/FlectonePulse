package net.flectone.pulse.module.message.status.players;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.status.players.listener.PlayersPulseListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayersModule implements AbstractModuleLocalization<Localization.Message.Status.Players> {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final PlatformServerAdapter platformServerAdapter;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        listenerRegistry.register(PlayersPulseListener.class);
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return AbstractModuleLocalization.super.permissionBuilder().add(permission().bypass());
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_STATUS_PLAYERS;
    }

    @Override
    public Message.Status.Players config() {
        return fileFacade.message().status().players();
    }

    @Override
    public Permission.Message.Status.Players permission() {
        return fileFacade.permission().message().status().players();
    }

    @Override
    public Localization.Message.Status.Players localization(FEntity sender) {
        return fileFacade.localization(sender).message().status().players();
    }

    public boolean isAllowed(FPlayer fPlayer) {
        if (!config().control()) return true;
        if (!moduleController.isEnable(this)) return true;
        if (moduleController.isDisabledFor(this, fPlayer)) return true;
        if (permissionChecker.check(fPlayer, permission().bypass())) return true;

        int online = platformServerAdapter.getOnlinePlayerCount();
        return online < config().max();
    }

    public List<Localization.Message.Status.Players.Sample> getSamples(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        return localization(fPlayer).samples();
    }
}
