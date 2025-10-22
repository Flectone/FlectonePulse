package net.flectone.pulse.module.integration.telegram;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.telegram.listener.MessageListener;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.math.NumberUtils;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.forum.EditForumTopic;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatTitle;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TelegramIntegration implements FIntegration {

    private final FileResolver fileResolver;
    private final SystemVariableResolver systemVariableResolver;
    private final FLogger fLogger;
    private final MessageListener messageListener;
    private final MessagePipeline messagePipeline;
    private final TaskScheduler taskScheduler;

    private TelegramBotsLongPollingApplication botsApplication;
    private OkHttpTelegramClient telegramClient;

    public Integration.Telegram config() {
        return fileResolver.getIntegration().getTelegram();
    }

    @Override
    public void hook() {
        String token = systemVariableResolver.substituteEnvVars(config().getToken());
        if (token.isEmpty()) return;

        try {
            telegramClient = new OkHttpTelegramClient(token);

            botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(token, messageListener);

            Integration.ChannelInfo channelInfo = config().getChannelInfo();

            if (channelInfo.isEnable() && channelInfo.getTicker().isEnable()) {
                long period = channelInfo.getTicker().getPeriod();
                taskScheduler.runAsyncTimer(this::updateChannelInfo, period, period);
                updateChannelInfo();
            }

            fLogger.info("✔ Telegram integration enabled");

        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> telegramString) {
        if (botsApplication == null) return;

        List<String> channels = config().getMessageChannel().get(messageName);
        if (channels == null) return;
        if (channels.isEmpty()) return;

        Localization.Integration.Telegram localization = fileResolver.getLocalization().getIntegration().getTelegram();
        String message = localization.getMessageChannel().getOrDefault(messageName, "<final_message>");
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

    public void updateChannelInfo() {
        if (botsApplication == null) return;

        if (!config().getChannelInfo().isEnable()) return;

        Localization.Integration.Telegram localization = fileResolver.getLocalization().getIntegration().getTelegram();
        for (Map.Entry<String, String> entry : localization.getInfoChannel().entrySet()) {
            String chatId = entry.getKey();
            if (chatId.contains("_")) {
                String[] ids = chatId.split("_");
                if (ids.length != 2) continue;
                if (!NumberUtils.isParsable(ids[0])) continue;
                if (!NumberUtils.isParsable(ids[1])) continue;

                executeMethod(EditForumTopic.builder()
                        .chatId(ids[0])
                        .messageThreadId(Integer.parseInt(ids[1]))
                        .name(getNewChatName(entry.getValue()))
                        .build()
                );
            } else {
                executeMethod(SetChatTitle.builder()
                        .chatId(chatId)
                        .title(getNewChatName(entry.getValue()))
                        .build()
                );
            }
        }
    }

    private String getNewChatName(String value) {
        return PlainTextComponentSerializer.plainText()
                .serialize(messagePipeline.builder(value).build());
    }

    public void executeMethod(BotApiMethod<?> method) {
        try {
            telegramClient.executeAsync(method);
        } catch (TelegramApiException e) {
            fLogger.warning(e);
        }
    }
}
