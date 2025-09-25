package net.flectone.pulse.module.integration.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class TABModule extends AbstractModule {

    private final FileResolver fileResolver;
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
        this.fileResolver = fileResolver;
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
        registerModulePermission(permission());

        tabIntegration.hook();

        headerModule.addPredicate(fPlayer -> config().isDisableFlectonepulseHeader() && isHooked());
        footerModule.addPredicate(fPlayer -> config().isDisableFlectonepulseFooter() && isHooked());
        playerlistnameModule.addPredicate(fPlayer ->  config().isDisableFlectonepulsePlayerlistname() && isHooked());

        scoreboardModule.addPredicate(fPlayer -> config().isDisableFlectonepulseScoreboard() && isHooked());
        belownameModule.addPredicate(fPlayer -> config().isDisableFlectonepulseScoreboard() && isHooked());
        tabnameModule.addPredicate(fPlayer -> config().isDisableFlectonepulseScoreboard() && isHooked());
    }

    @Override
    public void onDisable() {
        tabIntegration.unhook();
    }

    @Override
    public Integration.TAB config() {
        return fileResolver.getIntegration().getTAB();
    }

    @Override
    public Permission.Integration.TAB permission() {
        return fileResolver.getPermission().getIntegration().getTAB();
    }

    public boolean isHooked() {
        return tabIntegration.isHooked();
    }
}
