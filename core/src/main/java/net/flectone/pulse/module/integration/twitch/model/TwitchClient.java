package net.flectone.pulse.module.integration.twitch.model;

import net.flectone.pulse.model.entity.FPlayer;
import org.jspecify.annotations.NonNull;

public record TwitchClient(
        @NonNull String token,
        @NonNull String identityProvider,
        @NonNull FPlayer sender,
        com.github.twitch4j.@NonNull TwitchClient client
) {
}
