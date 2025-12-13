package net.flectone.pulse.module.integration.tab.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.tab.TABModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
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
    public void onModuleEnableEvent(ModuleEnableEvent event) {
        if (!tabModule.isHooked()) return;

        AbstractModule eventModule = event.getModule();
        Integration.TAB config = tabModule.config();

        if ((eventModule instanceof HeaderModule && config.isDisableFlectonepulseHeader())
                || (eventModule instanceof FooterModule && config.isDisableFlectonepulseFooter())
                || (eventModule instanceof PlayerlistnameModule && config.isDisableFlectonepulsePlayerlistname())
                || ((eventModule instanceof ScoreboardModule || eventModule instanceof BelownameModule || eventModule instanceof TabnameModule) && config.isDisableFlectonepulseScoreboard())) {
            event.setCancelled(true);
        }
    }

}
