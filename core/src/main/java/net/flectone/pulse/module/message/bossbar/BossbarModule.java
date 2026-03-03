package net.flectone.pulse.module.message.bossbar;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BossbarModule implements ModuleLocalization<Localization.Message.Bossbar> {

    private final FileFacade fileFacade;

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
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
    public Localization.Message.Bossbar localization(FEntity sender) {
        return fileFacade.localization(sender).message().bossbar();
    }

}
