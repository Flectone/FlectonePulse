package net.flectone.pulse.module.message.format.object;

import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ObjectModuleImpl implements ObjectModule {

    private final FileFacade fileFacade;
    private final SocialService socialService;

    @Override
    public Localization.Message.Format.Object localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format().object();
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(ObjectModule.super.permissions());
        permissions.add(permission().playerHeadTag());
        permissions.add(permission().spriteTag());
        permissions.add(permission().textureTag());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_OBJECT;
    }

    @Override
    public Message.Format.Object config() {
        return fileFacade.message().format().object();
    }

    @Override
    public Permission.Message.Format.Object permission() {
        return fileFacade.permission().message().format().object();
    }

}
