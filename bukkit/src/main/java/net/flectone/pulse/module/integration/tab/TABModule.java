package net.flectone.pulse.module.integration.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
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
                     PlayerlistnameModule playerlistnameModule,
                     ScoreboardModule scoreboardModule) {
        integration = fileManager.getIntegration().getTAB();
        permission = fileManager.getPermission().getIntegration().getTAB();

        this.tabIntegration = tabIntegration;

        Message.Tab messageTab = fileManager.getMessage().getTab();

        headerModule.addPredicate(fPlayer -> messageTab.getHeader().isDisableOnOtherTab() && isHooked());
        footerModule.addPredicate(fPlayer -> messageTab.getFooter().isDisableOnOtherTab() && isHooked());
        playerlistnameModule.addPredicate(fPlayer -> messageTab.getPlayerlistname().isDisableOnOtherTab() && isHooked());
        scoreboardModule.addPredicate(fPlayer -> fileManager.getMessage().getFormat().getScoreboard().isDisableOnOtherScoreboard() && isHooked());
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
