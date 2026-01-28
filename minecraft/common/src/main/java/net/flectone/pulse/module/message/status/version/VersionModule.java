package net.flectone.pulse.module.message.status.version;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VersionModule extends AbstractModuleLocalization<Localization.Message.Status.Version> {

    private final FileFacade fileFacade;

    @Override
    public MessageType messageType() {
        return MessageType.VERSION;
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
    public Localization.Message.Status.Version localization(FEntity sender) {
        return fileFacade.localization(sender).message().status().version();
    }

    public String get(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return localization(fPlayer).name();
    }
}
