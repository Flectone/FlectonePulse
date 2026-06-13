package net.flectone.pulse.module.message.bossbar;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BossbarModule implements ModuleLocalization<Localization.Message.Bossbar> {

    private final FileFacade fileFacade;
    private final SocialService socialService;

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder().addAll(permission().types().values());
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_BOSSBAR;
    }

    @Override
    public Message.Bossbar config() {
        return fileFacade.message().bossbar();
    }

    @Override
    public Permission.Message.Bossbar permission() {
        return fileFacade.permission().message().bossbar();
    }

    @Override
    public Localization.Message.Bossbar localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().bossbar();
    }

}
