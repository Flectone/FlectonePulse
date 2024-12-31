package net.flectone.pulse.module.message.tab.footer.ticker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.ticker.AbstractTicker;

@Singleton
public class FooterTicker extends AbstractTicker {

    @Inject
    public FooterTicker(FooterModule footerModule) {
        super(footerModule::send);
    }

}
