package net.flectone.pulse.module.integration.discord.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface DiscordMessageContext extends StringMessageContext {

    static DiscordMessageContextImpl.DiscordMessageContextImplBuilder builder() {
        return DiscordMessageContextImpl.builder();
    }

    @NonNull String globalName();

    @NonNull String nickname();

    @NonNull String displayName();

    @NonNull String userName();

    @Nullable Pair<String, String> reply();

    DiscordMessageContext withGlobalName(@NonNull String globalName);

    DiscordMessageContext withNickname(@NonNull String nickname);

    DiscordMessageContext withDisplayName(@NonNull String displayName);

    DiscordMessageContext withUserName(@NonNull String userName);

    DiscordMessageContext withReply(@Nullable Pair<String, String> reply);

}