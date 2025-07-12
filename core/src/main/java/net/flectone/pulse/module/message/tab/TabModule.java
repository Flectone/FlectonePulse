package net.flectone.pulse.module.message.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;

@Singleton
public class TabModule extends AbstractModule {

    private final Message.Tab message;
    private final Permission.Message.Tab permission;

    @Inject
    public TabModule(FileResolver fileResolver) {
        this.message = fileResolver.getMessage().getTab();
        this.permission = fileResolver.getPermission().getMessage().getTab();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        addChildren(FooterModule.class);
        addChildren(HeaderModule.class);
        addChildren(PlayerlistnameModule.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }
}
