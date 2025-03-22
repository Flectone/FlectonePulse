package net.flectone.pulse.module.integration.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;

@Singleton
public class TABModule extends AbstractModule {

    private final Integration.TAB integration;
    private final Permission.Integration.TAB permission;

    private final TABIntegration tabIntegration;

    @Inject
    public TABModule(FileManager fileManager,
                     TABIntegration tabIntegration,
                     HeaderModule headerModule,
                     FooterModule footerModule,
                     PlayerlistnameModule playerlistnameModule) {
        integration = fileManager.getIntegration().getTAB();
        permission = fileManager.getPermission().getIntegration().getTAB();

        this.tabIntegration = tabIntegration;

        Message.Tab messageTab = fileManager.getMessage().getTab();

        headerModule.addPredicate(fPlayer -> messageTab.getHeader().isDisableOnOtherTab() && isHooked());
        footerModule.addPredicate(fPlayer -> messageTab.getFooter().isDisableOnOtherTab() && isHooked());
        playerlistnameModule.addPredicate(fPlayer -> messageTab.getPlayerlistname().isDisableOnOtherTab() && isHooked());
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        tabIntegration.hook();
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean isHooked() {
        return tabIntegration.isHooked();
    }
}
