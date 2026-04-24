package net.flectone.pulse.module.integration.tab.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.tab.BukkitTABModule;
import net.flectone.pulse.module.message.format.scoreboard.MinecraftScoreboardModule;
import net.flectone.pulse.module.message.objective.belowname.MinecraftBelownameModule;
import net.flectone.pulse.module.message.objective.tabname.MinecraftTabnameModule;
import net.flectone.pulse.module.message.tab.footer.MinecraftFooterModule;
import net.flectone.pulse.module.message.tab.header.MinecraftHeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.MinecraftPlayerlistnameModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitPulseTABListener implements PulseListener {

    private final BukkitTABModule tabModule;

    @Pulse
    public Event onModuleEnableEvent(ModuleEnableEvent event) {
        if (!tabModule.isHooked()) return event;

        ModuleSimple eventModule = event.module();
        Integration.Tab config = tabModule.config();

        if ((eventModule instanceof MinecraftHeaderModule && config.disableFlectonepulseHeader())
                || (eventModule instanceof MinecraftFooterModule && config.disableFlectonepulseFooter())
                || (eventModule instanceof MinecraftPlayerlistnameModule && config.disableFlectonepulsePlayerlistname())
                || ((eventModule instanceof MinecraftScoreboardModule || eventModule instanceof MinecraftBelownameModule || eventModule instanceof MinecraftTabnameModule) && config.disableFlectonepulseScoreboard())) {
            return event.withCancelled(true);
        }

        return event;
    }

}
