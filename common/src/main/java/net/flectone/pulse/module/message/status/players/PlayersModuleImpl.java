package net.flectone.pulse.module.message.status.players;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.SocialService;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayersModuleImpl implements PlayersModule {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final PlatformServerAdapter platformServerAdapter;
    private final ModuleController moduleController;
    private final MessagePipeline messagePipeline;
    private final SocialService socialService;

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(PlayersModule.super.permissions());
        permissions.add(permission().bypass());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public boolean isAllowed(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return true;
        if (!config().control()) return true;
        if (permissionChecker.check(fPlayer, permission().bypass())) return true;

        int online = getOnline(fPlayer);
        return online < config().max();
    }

    @Override
    public int getMaxOnline(FPlayer fPlayer) {
        int maxOnline = platformServerAdapter.getMaxPlayers();
        if (moduleController.isDisabledFor(this, fPlayer)) return maxOnline;

        return config().max();
    }

    @Override
    public int getOnline(FPlayer fPlayer) {
        int serverOnline = platformServerAdapter.getOnlinePlayerCount();
        if (moduleController.isDisabledFor(this, fPlayer)) return serverOnline;

        String online = config().online();
        try {
            return StringUtils.isEmpty(online) ? serverOnline : Integer.parseInt(online);
        } catch (NumberFormatException _) {
            // ignore exception
        }

        online = messagePipeline.buildPlain(MessageContext.builder()
                .module(this.name())
                .sender(fPlayer)
                .message(config().online())
                .build()
        );
        try {
            return Integer.parseInt(online);
        } catch (NumberFormatException _) {
            return serverOnline;
        }
    }

    @Override
    public List<Localization.Message.Status.Players.Sample> getSamples(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        return localization(fPlayer).samples();
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
    public Localization.Message.Status.Players localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().status().players();
    }

}
