package net.flectone.pulse.module.message.tab.header.ticker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.ticker.AbstractTicker;

@Singleton
public class HeaderTicker extends AbstractTicker {

    @Inject
    public HeaderTicker(HeaderModule headerModule) {
        super(headerModule::send);
    }

}
