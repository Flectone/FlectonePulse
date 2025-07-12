package net.flectone.pulse.module.integration.advancedban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.ExternalModeration;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
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
public class AdvancedBanModule extends AbstractModule {

    private final Integration.Advancedban integration;
    private final Permission.Integration.Advancedban permission;

    private final AdvancedBanIntegration advancedBanIntegration;

    @Inject
    public AdvancedBanModule(FileResolver fileResolver,
                             AdvancedBanIntegration advancedBanIntegration,
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
        this.advancedBanIntegration = advancedBanIntegration;

        integration = fileResolver.getIntegration().getAdvancedban();
        permission = fileResolver.getPermission().getIntegration().getAdvancedban();

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
    public void onEnable() {
        registerModulePermission(permission);

        advancedBanIntegration.hook();
    @Override
    public void onDisable() {
        advancedBanIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean isMuted(FEntity fEntity) {
        if (checkModulePredicates(fEntity)) return false;

        return advancedBanIntegration.isMuted(fEntity);
    }

    public ExternalModeration getMute(FEntity fEntity) {
        if (checkModulePredicates(fEntity)) return null;

        return advancedBanIntegration.getMute(fEntity);
    }

    public boolean isHooked() {
        return advancedBanIntegration.isHooked();
    }
}
