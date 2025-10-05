package net.flectone.pulse.module.integration.litebans.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
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
import net.flectone.pulse.module.integration.litebans.LiteBansModule;

import java.util.Set;

@Singleton
public class LiteBansPulseListener implements PulseListener {

    private final Set<Class<? extends AbstractModule>> banModules = Set.of(BanModule.class, BanlistModule.class, UnbanModule.class);
    private final Set<Class<? extends AbstractModule>> muteModules = Set.of(MuteModule.class, MutelistModule.class, UnmuteModule.class);
    private final Set<Class<? extends AbstractModule>> warnModules = Set.of(WarnModule.class, WarnlistModule.class, UnwarnModule.class);
    private final Set<Class<? extends AbstractModule>> kickModules = Set.of(KickModule.class);

    private final LiteBansModule liteBansModule;

    @Inject
    public LiteBansPulseListener(LiteBansModule liteBansModule) {
        this.liteBansModule = liteBansModule;
    }

    @Pulse
    public void onModuleEnableEvent(ModuleEnableEvent event) {
        if (!liteBansModule.isHooked()) return;

        AbstractModule eventModule = event.getModule();
        Integration.Litebans config = liteBansModule.config();

        if ((config.isDisableFlectonepulseBan() && isInstanceOfAny(eventModule, banModules)) ||
                (config.isDisableFlectonepulseMute() && isInstanceOfAny(eventModule, muteModules)) ||
                (config.isDisableFlectonepulseWarn() && isInstanceOfAny(eventModule, warnModules)) ||
                (config.isDisableFlectonepulseKick() && isInstanceOfAny(eventModule, kickModules))) {
            event.setCancelled(true);
        }
    }

    private boolean isInstanceOfAny(AbstractModule module, Set<Class<? extends AbstractModule>> classes) {
        return classes.stream().anyMatch(clazz -> clazz.isInstance(module));
    }

}
