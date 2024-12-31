package net.flectone.pulse.module.message.brand.ticker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.brand.BrandModule;
import net.flectone.pulse.ticker.AbstractTicker;

@Singleton
public class BrandTicker extends AbstractTicker {

    @Inject
    public BrandTicker(BrandModule brandModule) {
        super(brandModule::send);
    }
}
