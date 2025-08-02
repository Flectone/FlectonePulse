package net.flectone.pulse.module.integration.telegram.listener;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.AbstractModuleLocalization;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;

public abstract class EventListener extends AbstractModuleLocalization<Localization.Integration.Telegram> implements LongPollingSingleThreadUpdateConsumer {

    protected EventListener() {
        super(localization -> localization.getIntegration().getTelegram());
    }

}
