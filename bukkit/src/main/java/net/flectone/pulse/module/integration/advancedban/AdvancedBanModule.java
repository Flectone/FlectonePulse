package net.flectone.pulse.module.integration.advancedban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.model.entity.FEntity;
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
import net.flectone.pulse.processing.resolver.FileResolver;

import java.util.function.Predicate;

@Singleton
public class AdvancedBanModule extends AbstractModule {

    private final Integration.Advancedban integration;
    private final Permission.Integration.Advancedban permission;
    private final AdvancedBanIntegration advancedBanIntegration;
    private final BanModule banModule;
    private final BanlistModule banlistModule;
    private final UnbanModule unbanModule;
    private final MuteModule muteModule;
    private final MutelistModule mutelistModule;
    private final UnmuteModule unmuteModule;
    private final WarnModule warnModule;
    private final WarnlistModule warnlistModule;
    private final UnwarnModule unwarnModule;
    private final KickModule kickModule;

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
        this.integration = fileResolver.getIntegration().getAdvancedban();
        this.permission = fileResolver.getPermission().getIntegration().getAdvancedban();
        this.advancedBanIntegration = advancedBanIntegration;
        this.banModule = banModule;
        this.banlistModule = banlistModule;
        this.unbanModule = unbanModule;
        this.muteModule = muteModule;
        this.mutelistModule = mutelistModule;
        this.unmuteModule = unmuteModule;
        this.warnModule = warnModule;
        this.warnlistModule = warnlistModule;
        this.unwarnModule = unwarnModule;
        this.kickModule = kickModule;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        advancedBanIntegration.hook();

        Predicate<FEntity> disablePredicateBan = fPlayer -> integration.isDisableFlectonepulseBan() && isHooked();
        banModule.addPredicate(disablePredicateBan);
        banlistModule.addPredicate(disablePredicateBan);
        unbanModule.addPredicate(disablePredicateBan);

        Predicate<FEntity> disablePredicateMute = fPlayer -> integration.isDisableFlectonepulseMute() && isHooked();
        muteModule.addPredicate(disablePredicateMute);
        mutelistModule.addPredicate(disablePredicateMute);
        unmuteModule.addPredicate(disablePredicateMute);

        Predicate<FEntity> disablePredicateWarn = fPlayer -> integration.isDisableFlectonepulseWarn() && isHooked();
        warnModule.addPredicate(disablePredicateWarn);
        warnlistModule.addPredicate(disablePredicateWarn);
        unwarnModule.addPredicate(disablePredicateWarn);

        kickModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseKick() && isHooked());
    }

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
