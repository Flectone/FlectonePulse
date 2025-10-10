package net.flectone.pulse.module.integration.telegram;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.telegram.listener.MessageListener;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.logging.FLogger;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TelegramIntegration implements FIntegration {

    private final FileResolver fileResolver;
    private final SystemVariableResolver systemVariableResolver;
    private final FLogger fLogger;
    private final MessageListener messageListener;

    private TelegramBotsLongPollingApplication botsApplication;
    private OkHttpTelegramClient telegramClient;

    @Override
    public void hook() {
        String token = systemVariableResolver.substituteEnvVars(fileResolver.getIntegration().getTelegram().getToken());
        if (token.isEmpty()) return;

        try {
            telegramClient = new OkHttpTelegramClient(token);

            botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(token, messageListener);

            fLogger.info("✔ Telegram integration enabled");

        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> telegramString) {
        if (botsApplication == null) return;

        List<String> channels = fileResolver.getIntegration().getTelegram().getMessageChannel().get(messageName);
        if (channels == null) return;
        if (channels.isEmpty()) return;

        Localization.Integration.Telegram localization = fileResolver.getLocalization().getIntegration().getTelegram();
        String message = localization.getMessageChannel().get(messageName);
        if (message == null) return;
        if (message.isEmpty()) return;

        message = telegramString.apply(message);

        for (String chat : channels) {

            var sendMessageBuilder = SendMessage.builder()
                    .chatId(chat)
                    .text(message);

            if (chat.contains("_")) {
                sendMessageBuilder
                        .messageThreadId(Integer.parseInt(chat.split("_")[1]));
            }

            executeMethod(sendMessageBuilder.build());
        }
    }

    @Override
    public void unhook() {
        if (botsApplication == null) return;

        try {
            botsApplication.close();
        } catch (Exception e) {
            fLogger.warning(e);
        }

        fLogger.info("✖ Telegram integration disabled");
    }

    public void executeMethod(BotApiMethod<?> method) {
        try {
            telegramClient.executeAsync(method);
        } catch (TelegramApiException e) {
            fLogger.warning(e);
        }
    }
}
