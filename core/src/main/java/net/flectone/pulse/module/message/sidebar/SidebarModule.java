package net.flectone.pulse.module.message.sidebar;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleListLocalization;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SidebarModule extends AbstractModuleListLocalization<Localization.Message.Sidebar> {

    private final FileFacade fileFacade;

    @Override
    public MessageType messageType() {
        return MessageType.SIDEBAR;
    }

    @Override
    public Message.Sidebar config() {
        return fileFacade.message().sidebar();
    }

    @Override
    public Permission.Message.Sidebar permission() {
        return fileFacade.permission().message().sidebar();
    }

    @Override
    public Localization.Message.Sidebar localization(FEntity sender) {
        return fileFacade.localization(sender).message().sidebar();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return joinMultiList(localization(fPlayer).values());
    }

}
