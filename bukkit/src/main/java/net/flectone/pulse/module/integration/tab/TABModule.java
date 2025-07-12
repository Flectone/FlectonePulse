package net.flectone.pulse.module.integration.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class TABModule extends AbstractModule {

    private final Integration.TAB integration;
    private final Permission.Integration.TAB permission;
    private final TABIntegration tabIntegration;
    private final HeaderModule headerModule;
    private final FooterModule footerModule;
    private final PlayerlistnameModule playerlistnameModule;
    private final ScoreboardModule scoreboardModule;
    private final BelownameModule belownameModule;
    private final TabnameModule tabnameModule;

    @Inject
    public TABModule(FileResolver fileResolver,
                     TABIntegration tabIntegration,
                     HeaderModule headerModule,
                     FooterModule footerModule,
                     PlayerlistnameModule playerlistnameModule,
                     ScoreboardModule scoreboardModule,
                     BelownameModule belownameModule,
                     TabnameModule tabnameModule) {
        this.integration = fileResolver.getIntegration().getTAB();
        this.permission = fileResolver.getPermission().getIntegration().getTAB();
        this.tabIntegration = tabIntegration;
        this.headerModule = headerModule;
        this.footerModule = footerModule;
        this.playerlistnameModule = playerlistnameModule;
        this.scoreboardModule = scoreboardModule;
        this.belownameModule = belownameModule;
        this.tabnameModule = tabnameModule;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        tabIntegration.hook();

        headerModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseHeader() && isHooked());
        footerModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseFooter() && isHooked());
        playerlistnameModule.addPredicate(fPlayer ->  integration.isDisableFlectonepulsePlayerlistname() && isHooked());

        scoreboardModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseScoreboard() && isHooked());
        belownameModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseScoreboard() && isHooked());
        tabnameModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseScoreboard() && isHooked());
    }

    @Override
    public void onDisable() {
        tabIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean isHooked() {
        return tabIntegration.isHooked();
    }
}
