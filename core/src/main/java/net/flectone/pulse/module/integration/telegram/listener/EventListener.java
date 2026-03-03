package net.flectone.pulse.module.integration.telegram.listener;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.util.constant.ModuleName;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;

public interface EventListener extends ModuleLocalization<Localization.Integration.Telegram>, LongPollingSingleThreadUpdateConsumer {

    @Override
    default ModuleName name() {
        return ModuleName.INTEGRATION_TELEGRAM;
    }

}
