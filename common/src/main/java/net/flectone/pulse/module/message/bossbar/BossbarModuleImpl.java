package net.flectone.pulse.module.message.bossbar;

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
public class BossbarModuleImpl implements BossbarModule {

    private final FileFacade fileFacade;
    private final SocialService socialService;

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(BossbarModule.super.permissions());
        permissions.addAll(permission().types().values());
        return Collections.unmodifiableSet(permissions);
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
