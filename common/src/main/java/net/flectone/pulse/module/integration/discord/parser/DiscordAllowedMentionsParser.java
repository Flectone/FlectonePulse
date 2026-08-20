package net.flectone.pulse.module.integration.discord.parser;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.rest.util.AllowedMentions;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordAllowedMentionsParser {

    private static final AllowedMentions.Type[] TYPES = AllowedMentions.Type.values();

    private final DiscordSnowflakeParser discordSnowflakeParser;

    @NonNull
    public AllowedMentions parse(Localization.Integration.Discord.@Nullable AllowedMentions allowedMentions) {
        if (allowedMentions == null) return AllowedMentions.suppressAll();

        AllowedMentions.Builder builder = AllowedMentions.builder();

        for (String type : defaultList(allowedMentions.types())) {
            parseType(type).ifPresent(builder::parseType);
        }

        for (String user : defaultList(allowedMentions.users())) {
            discordSnowflakeParser.parse(user).ifPresent(builder::allowUser);
        }

        for (String role : defaultList(allowedMentions.roles())) {
            discordSnowflakeParser.parse(role).ifPresent(builder::allowRole);
        }

        return builder.build();
    }

    public Optional<AllowedMentions.Type> parseType(@NonNull String string) {
        for (AllowedMentions.Type type : TYPES) {
            if (type.name().equalsIgnoreCase(string) || type.getRaw().equalsIgnoreCase(string)) {
                return Optional.of(type);
            }
        }

        return Optional.empty();
    }

    private List<String> defaultList(@Nullable List<String> list) {
        return list == null ? List.of() : list;
    }

}
