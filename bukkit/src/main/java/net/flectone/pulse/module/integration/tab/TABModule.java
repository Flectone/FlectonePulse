package net.flectone.pulse.module.integration.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
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
                     ScoreboardModule scoreboardModule,
                     BelownameModule belownameModule,
                     TabnameModule tabnameModule) {
        integration = fileManager.getIntegration().getTAB();
        permission = fileManager.getPermission().getIntegration().getTAB();

        this.tabIntegration = tabIntegration;

        headerModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseHeader() && isHooked());
        footerModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseFooter() && isHooked());
        playerlistnameModule.addPredicate(fPlayer ->  integration.isDisableFlectonepulsePlayerlistname() && isHooked());

        scoreboardModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseScoreboard() && isHooked());
        belownameModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseScoreboard() && isHooked());
        tabnameModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseScoreboard() && isHooked());
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
