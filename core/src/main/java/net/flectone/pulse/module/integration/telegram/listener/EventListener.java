package net.flectone.pulse.module.integration.telegram.listener;

import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.module.AbstractModuleMessage;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;

public abstract class EventListener extends AbstractModuleMessage<Localization.Integration.Telegram> implements LongPollingSingleThreadUpdateConsumer {

    public EventListener() {
        super(localization -> localization.getIntegration().getTelegram());
    }
}
