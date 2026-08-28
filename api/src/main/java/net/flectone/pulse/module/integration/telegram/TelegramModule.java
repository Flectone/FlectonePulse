package net.flectone.pulse.module.integration.telegram;

import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.constant.ModuleName;
import org.jspecify.annotations.NonNull;

/**
 * Mirrors chat to and from Telegram.
 * @author TheFaser
 */
public interface TelegramModule extends ModuleLocalization {

    @Override
    Integration.Telegram config();

    @Override
    Permission.Integration.Telegram permission();

    @Override
    Localization.Integration.Telegram localization(FPlayer fPlayer);

    /**
     * Mirrors a message to Telegram.
     *
     * @param moduleName the module it came from
     * @param messageContext the message
     * @param integrationMessageFormat how to rewrite it for Telegram
     */
    void sendMessage(@NonNull ModuleName moduleName, @NonNull MessageContext messageContext, @NonNull IntegrationMessageFormat integrationMessageFormat);

}
