package net.flectone.pulse.module.message.vanilla;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VanillaModule extends AbstractModuleLocalization<Localization.Message.Vanilla> {

    private final FileFacade fileFacade;

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public MessageType messageType() {
        return MessageType.VANILLA;
    }

    @Override
    public Message.Vanilla config() {
        return fileFacade.message().vanilla();
    }

    @Override
    public Permission.Message.Vanilla permission() {
        return fileFacade.permission().message().vanilla();
    }

    @Override
    public Localization.Message.Vanilla localization(FEntity sender) {
        return fileFacade.localization(sender).message().vanilla();
    }

}
