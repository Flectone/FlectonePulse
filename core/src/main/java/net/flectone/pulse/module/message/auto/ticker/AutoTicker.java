package net.flectone.pulse.module.message.auto.ticker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.auto.AutoModule;
import net.flectone.pulse.ticker.AbstractTicker;

@Singleton
public class AutoTicker extends AbstractTicker {

    @Inject
    public AutoTicker(AutoModule autoModule) {
        super(autoModule::send);
    }

}
