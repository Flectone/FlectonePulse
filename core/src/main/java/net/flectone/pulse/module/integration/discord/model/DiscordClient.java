package net.flectone.pulse.module.integration.discord.model;

import discord4j.core.GatewayDiscordClient;
import net.flectone.pulse.model.entity.FPlayer;
import org.jspecify.annotations.NonNull;

public record DiscordClient(
        @NonNull String token,
        @NonNull FPlayer sender,
        discord4j.core.@NonNull DiscordClient client,
        @NonNull GatewayDiscordClient gateway,
        long id
) {
}
