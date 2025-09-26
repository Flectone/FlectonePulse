package net.flectone.pulse.module.integration.telegram.listener;

import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.util.constant.MessageType;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;

public abstract class EventListener extends AbstractModuleLocalization<Localization.Integration.Telegram> implements LongPollingSingleThreadUpdateConsumer {

    protected EventListener() {
        super(MessageType.FROM_TELEGRAM_TO_MINECRAFT);
    }

}
