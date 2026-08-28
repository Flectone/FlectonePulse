package net.flectone.pulse.module.integration.discord;

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
 * Mirrors chat to and from Discord.
 * @author TheFaser
 */
public interface DiscordModule extends ModuleLocalization {

    @Override
    Integration.Discord config();

    @Override
    Permission.Integration.Discord permission();

    @Override
    Localization.Integration.Discord localization(FPlayer fPlayer);

    /**
     * Mirrors a message to Discord.
     *
     * @param moduleName the module it came from
     * @param messageContext the message
     * @param integrationMessageFormat how to rewrite it for Discord
     */
    void sendMessage(@NonNull ModuleName moduleName, @NonNull MessageContext messageContext, @NonNull IntegrationMessageFormat integrationMessageFormat);

}
