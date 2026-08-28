package net.flectone.pulse.module.integration.twitch;

import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import org.jspecify.annotations.NonNull;

/**
 * Mirrors chat to and from Twitch.
 * @author TheFaser
 */
public interface TwitchModule extends ModuleLocalization {

    @Override
    Integration.Twitch config();

    @Override
    Permission.Integration.Twitch permission();

    @Override
    Localization.Integration.Twitch localization(FPlayer fPlayer);

    /**
     * Mirrors a message to Twitch.
     *
     * @param moduleName the module it came from
     * @param messageContext the message
     * @param integrationMessageFormat how to rewrite it for Twitch
     */
    void sendMessage(@NonNull ModuleName moduleName, @NonNull MessageContext messageContext, @NonNull IntegrationMessageFormat integrationMessageFormat);

}
