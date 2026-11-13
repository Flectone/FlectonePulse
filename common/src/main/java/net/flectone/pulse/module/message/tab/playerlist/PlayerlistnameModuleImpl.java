package net.flectone.pulse.module.message.tab.playerlist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.SocialService;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerlistnameModuleImpl implements PlayerlistnameModule {

    private final FileFacade fileFacade;
    private final SocialService socialService;

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(PlayerlistnameModule.super.permissions());
        permissions.add(permission().hideInvisible());
        permissions.add(permission().hideSpectator());

        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public void update() {
        // player list names are a platform feature, nothing to do by default
    }

    @Override
    public void update(FPlayer fPlayer) {
        // player list names are a platform feature, nothing to do by default
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_TAB_PLAYERLISTNAME;
    }

    @Override
    public Message.Tab.Playerlistname config() {
        return fileFacade.message().tab().playerlistname();
    }

    @Override
    public Permission.Message.Tab.Playerlistname permission() {
        return fileFacade.permission().message().tab().playerlistname();
    }

    @Override
    public Localization.Message.Tab.Playerlistname localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().tab().playerlistname();
    }

}
