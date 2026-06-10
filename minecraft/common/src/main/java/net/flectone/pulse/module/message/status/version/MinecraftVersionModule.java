package net.flectone.pulse.module.message.status.version;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftVersionModule implements ModuleLocalization<Localization.Message.Status.Version> {

    private final FileFacade fileFacade;
    private final ModuleController moduleController;
    private final SocialService socialService;

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_STATUS_VERSION;
    }

    @Override
    public Message.Status.Version config() {
        return fileFacade.message().status().version();
    }

    @Override
    public Permission.Message.Status.Version permission() {
        return fileFacade.permission().message().status().version();
    }

    @Override
    public Localization.Message.Status.Version localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().status().version();
    }

    public String get(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        return localization(fPlayer).name();
    }
}
