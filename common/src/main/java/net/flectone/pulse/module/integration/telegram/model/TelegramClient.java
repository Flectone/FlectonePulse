package net.flectone.pulse.module.integration.telegram.model;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.telegram.listener.TelegramEventListener;
import org.jspecify.annotations.NonNull;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public record TelegramClient(
        @NonNull String token,
        @NonNull FPlayer sender,
        @NonNull OkHttpTelegramClient client,
        @NonNull TelegramBotsLongPollingApplication application,
        long id
) {

    public void registerListener(@NonNull TelegramEventListener telegramEventListener) throws TelegramApiException {
        application.registerBot(token, telegramEventListener);
    }

    public void executeMethod(@NonNull BotApiMethod<?> method) throws TelegramApiException {
        client.executeAsync(method);
    }

}
