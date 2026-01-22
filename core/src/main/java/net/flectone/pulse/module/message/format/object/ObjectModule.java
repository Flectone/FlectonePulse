package net.flectone.pulse.module.message.format.object;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ObjectModule extends AbstractModuleLocalization<Localization.Message.Format.Object> {

    private final FileFacade fileFacade;

    @Override
    public MessageType messageType() {
        return MessageType.OBJECT;
    }

    @Override
    public Localization.Message.Format.Object localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().object();
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().add(permission().playerHead(), permission().sprite());
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
