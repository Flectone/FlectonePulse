package net.flectone.pulse.module.integration.tab.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.tab.TABModule;
import net.flectone.pulse.module.message.format.scoreboard.MinecraftScoreboardModule;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TABPulseListener implements PulseListener {

    private final TABModule tabModule;

    @Pulse
    public Event onModuleEnableEvent(ModuleEnableEvent event) {
        if (!tabModule.isHooked()) return event;

        AbstractModule eventModule = event.module();
        Integration.Tab config = tabModule.config();

        if ((eventModule instanceof HeaderModule && config.disableFlectonepulseHeader())
                || (eventModule instanceof FooterModule && config.disableFlectonepulseFooter())
                || (eventModule instanceof PlayerlistnameModule && config.disableFlectonepulsePlayerlistname())
                || ((eventModule instanceof MinecraftScoreboardModule || eventModule instanceof BelownameModule || eventModule instanceof TabnameModule) && config.disableFlectonepulseScoreboard())) {
            return event.withCancelled(true);
        }

        return event;
    }

}
