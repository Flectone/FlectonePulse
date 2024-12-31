package net.flectone.pulse.module.integration.discord.ticker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.integration.discord.DiscordIntegration;
import net.flectone.pulse.ticker.AbstractTicker;

@Singleton
public class DiscordTicker extends AbstractTicker {

    @Inject
    public DiscordTicker(DiscordIntegration discordIntegration) {
        super(discordIntegration::updateChannelInfo);
    }
}
