package net.flectone.pulse.module.message.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class TabModule extends AbstractModule {

    private final FileResolver fileResolver;

    @Inject
    public TabModule(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    @Override
    public void configureChildren() {
        super.configureChildren();

        addChildren(FooterModule.class);
        addChildren(HeaderModule.class);
        addChildren(PlayerlistnameModule.class);
    }

    @Override
    public Message.Tab config() {
        return fileResolver.getMessage().getTab();
    }

    @Override
    public Permission.Message.Tab permission() {
        return fileResolver.getPermission().getMessage().getTab();
    }
}
