package net.flectone.pulse.module.integration.telegram;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.telegram.listener.MessageListener;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.resolver.SystemVariableResolver;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.logging.FLogger;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.function.UnaryOperator;

@Singleton
public class TelegramIntegration implements FIntegration {

    private final Integration.Telegram integration;
    private final FileResolver fileResolver;
    private final SystemVariableResolver systemVariableResolver;
    private final FLogger fLogger;
    private final MessageListener messageListener;

    private TelegramBotsLongPollingApplication botsApplication;
    private OkHttpTelegramClient telegramClient;

    @Inject
    public TelegramIntegration(FileResolver fileResolver,
                               SystemVariableResolver systemVariableResolver,
                               FLogger fLogger,
                               MessageListener messageListener) {
        this.integration = fileResolver.getIntegration().getTelegram();
        this.fileResolver = fileResolver;
        this.systemVariableResolver = systemVariableResolver;
        this.fLogger = fLogger;
        this.messageListener = messageListener;
    }

    @Override
    public void hook() {
        String token = systemVariableResolver.substituteEnvVars(integration.getToken());
        if (token.isEmpty()) return;

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            this.telegramClient = new OkHttpTelegramClient(token);
            this.botsApplication = botsApplication;
            botsApplication.registerBot(token, messageListener);

            fLogger.info("✔ Telegram integration enabled");

        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

    public void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> telegramString) {
        if (botsApplication == null) return;

        List<String> channels = integration.getMessageChannel().get(messageTag);
        if (channels == null) return;
        if (channels.isEmpty()) return;

        Localization.Integration.Telegram localization = fileResolver.getLocalization().getIntegration().getTelegram();
        String message = localization.getMessageChannel().get(messageTag);
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
