package net.flectone.pulse.module.message.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TabModule extends AbstractModule {

    private final FileFacade fileFacade;

    @Override
    public void configureChildren() {
        super.configureChildren();

        addChild(FooterModule.class);
        addChild(HeaderModule.class);
        addChild(PlayerlistnameModule.class);
    }

    @Override
    public Message.Tab config() {
        return fileFacade.message().tab();
    }

    @Override
    public Permission.Message.Tab permission() {
        return fileFacade.permission().message().tab();
    }
}
