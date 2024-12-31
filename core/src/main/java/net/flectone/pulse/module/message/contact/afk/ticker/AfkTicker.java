package net.flectone.pulse.module.message.contact.afk.ticker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.contact.afk.AfkModule;
import net.flectone.pulse.ticker.AbstractTicker;

@Singleton
public class AfkTicker extends AbstractTicker {

    @Inject
    public AfkTicker(AfkModule afkModule) {
        super(afkModule::check);
    }
}
