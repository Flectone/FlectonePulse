package net.flectone.pulse.module.message.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;

@Singleton
public class TabModule extends AbstractModule {

    private final Message.Tab message;
    private final Permission.Message.Tab permission;

    @Inject
    public TabModule(FileManager fileManager) {
        message = fileManager.getMessage().getTab();
        permission = fileManager.getPermission().getMessage().getTab();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        addChildren(FooterModule.class);
        addChildren(HeaderModule.class);
        addChildren(PlayerlistnameModule.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }
}
