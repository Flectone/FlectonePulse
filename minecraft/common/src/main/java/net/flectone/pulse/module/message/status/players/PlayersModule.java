package net.flectone.pulse.module.message.status.players;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.status.players.listener.PlayersPulseListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayersModule implements ModuleLocalization<Localization.Message.Status.Players> {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final PlatformServerAdapter platformServerAdapter;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;
    private final MessagePipeline messagePipeline;

    @Override
    public void onEnable() {
        listenerRegistry.register(PlayersPulseListener.class);
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder().add(permission().bypass());
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
        if (moduleController.isDisabledFor(this, fPlayer)) return true;
        if (!config().control()) return true;
        if (permissionChecker.check(fPlayer, permission().bypass())) return true;

        int online = getOnline(fPlayer);
        return online < config().max();
    }

    public int getMaxOnline(FPlayer fPlayer) {
        int maxOnline = platformServerAdapter.getMaxPlayers();
        if (moduleController.isDisabledFor(this, fPlayer)) return maxOnline;

        return config().max();
    }

    public int getOnline(FPlayer fPlayer) {
        int serverOnline = platformServerAdapter.getOnlinePlayerCount();
        if (moduleController.isDisabledFor(this, fPlayer)) return serverOnline;

        String online = config().online();
        try {
            return StringUtils.isEmpty(online) ? serverOnline : Integer.parseInt(online);
        } catch (NumberFormatException ignored) {
            // ignore exception
        }

        MessageContext onlineContext = messagePipeline.createContext(fPlayer, config().online());
        online = messagePipeline.buildPlain(onlineContext);
        try {
            return Integer.parseInt(online);
        } catch (NumberFormatException ignored) {
            return serverOnline;
        }
    }

    public List<Localization.Message.Status.Players.Sample> getSamples(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        return localization(fPlayer).samples();
    }
}
