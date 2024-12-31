package net.flectone.pulse.module.message.objective.belowname.ticker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.ticker.AbstractTicker;

@Singleton
public class BelownameTicker extends AbstractTicker {

    @Inject
    public BelownameTicker(BelownameModule belowNameModule) {
        super(belowNameModule::add);
    }
}
