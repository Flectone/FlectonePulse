package net.flectone.pulse.module.integration.discord.parser;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordSnowflakeParser {

    public Optional<Snowflake> parse(@NonNull String string) {
        try {
            return Optional.of(Snowflake.of(string));
        } catch (Exception _) {
            return Optional.empty();
        }
    }

}
