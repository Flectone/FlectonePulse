package net.flectone.pulse.module.integration.telegram.listener;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.util.constant.ModuleName;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;

public abstract class EventListener implements AbstractModuleLocalization<Localization.Integration.Telegram>, LongPollingSingleThreadUpdateConsumer {

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_TELEGRAM;
    }

}
