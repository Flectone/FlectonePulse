package net.flectone.pulse.module.integration.twitch.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface TwitchMessageContext extends StringMessageContext {

    static TwitchMessageContextImpl.TwitchMessageContextImplBuilder builder() {
        return TwitchMessageContextImpl.builder();
    }

    @NonNull String nickname();

    @NonNull String channel();

    @Nullable Pair<String, String> reply();

    TwitchMessageContext withNickname(@NonNull String nickname);

    TwitchMessageContext withChannel(@NonNull String channel);

    TwitchMessageContext withReply(@Nullable Pair<String, String> reply);

}