package net.flectone.pulse.module.message.serverlink;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.SocialService;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ServerlinkModuleImpl implements ServerlinkModule {

    private final FileFacade fileFacade;
    private final SocialService socialService;

    @Override
    public void sendLinks(FPlayer fPlayer) {
        // server links are a platform feature, nothing to do by default
    }

    @Override
    public void remove(UUID uuid) {
        // server links are a platform feature, nothing to do by default
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_SERVERLINK;
    }

    @Override
    public Message.Serverlink config() {
        return fileFacade.message().serverlink();
    }

    @Override
    public Permission.Message.Serverlink permission() {
        return fileFacade.permission().message().serverlink();
    }

    @Override
    public Localization.Message.Serverlink localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().serverlink();
    }

}
