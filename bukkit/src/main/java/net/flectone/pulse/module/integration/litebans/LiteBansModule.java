package net.flectone.pulse.module.integration.litebans;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.model.ExternalModeration;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.banlist.BanlistModule;
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.mutelist.MutelistModule;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.command.warnlist.WarnlistModule;

@Singleton
public class LiteBansModule extends AbstractModule {

    private final Integration.Litebans integration;
    private final Permission.Integration.Litebans permission;

    private final LiteBansIntegration liteBansIntegration;

    @Inject
    public LiteBansModule(FileManager fileManager,
                          LiteBansIntegration liteBansIntegration,
                          BanModule banModule,
                          BanlistModule banlistModule,
                          UnbanModule unbanModule,
                          MuteModule muteModule,
                          MutelistModule mutelistModule,
                          UnmuteModule unmuteModule,
                          WarnModule warnModule,
                          WarnlistModule warnlistModule,
                          UnwarnModule unwarnModule,
                          KickModule kickModule) {
        this.liteBansIntegration = liteBansIntegration;

        integration = fileManager.getIntegration().getLitebans();
        permission = fileManager.getPermission().getIntegration().getLitebans();

        banModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseBan() && isHooked());
        banlistModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseBan() && isHooked());
        unbanModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseBan() && isHooked());

        muteModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseMute() && isHooked());
        mutelistModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseMute() && isHooked());
        unmuteModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseMute() && isHooked());

        warnModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseWarn() && isHooked());
        warnlistModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseWarn() && isHooked());
        unwarnModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseWarn() && isHooked());

        kickModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseKick() && isHooked());
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        liteBansIntegration.hook();
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean isMuted(FEntity fEntity) {
        if (checkModulePredicates(fEntity)) return false;

        return liteBansIntegration.isMuted(fEntity);
    }

    public ExternalModeration getMute(FEntity fEntity) {
        if (checkModulePredicates(fEntity)) return null;

        return liteBansIntegration.getMute(fEntity);
    }

    public boolean isHooked() {
        return liteBansIntegration.isHooked();
    }
}
