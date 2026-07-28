package net.flectone.pulse.module.integration.telegram.model;

import net.flectone.pulse.model.event.message.context.StringMessageContext;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface TelegramMessageContext extends StringMessageContext {

    static TelegramMessageContextImpl.TelegramMessageContextImplBuilder builder() {
        return TelegramMessageContextImpl.builder();
    }

    @NonNull String userName();

    @NonNull String firstName();

    @NonNull String lastName();

    @NonNull String chat();

    @Nullable Pair<String, String> reply();

    TelegramMessageContext withUserName(@NonNull String userName);

    TelegramMessageContext withFirstName(@NonNull String firstName);

    TelegramMessageContext withLastName(@NonNull String lastName);

    TelegramMessageContext withChat(@NonNull String chat);

    TelegramMessageContext withReply(@Nullable Pair<String, String> reply);

}