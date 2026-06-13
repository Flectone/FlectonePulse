package net.flectone.pulse.module.integration.telegram;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.telegram.listener.TelegramMessageListener;
import net.flectone.pulse.module.integration.telegram.model.TelegramClient;
import net.flectone.pulse.module.integration.telegram.provider.TelegramClientProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.NonNull;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.forum.EditForumTopic;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatTitle;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TelegramIntegration implements FIntegration {

    private final AtomicLong taskGeneration = new AtomicLong(0);

    private final FileFacade fileFacade;
    private final TelegramClientProvider telegramClientProvider;
    private final MessagePipeline messagePipeline;
    private final TaskScheduler taskScheduler;
    private final Injector injector;

    @Getter private final FLogger fLogger;

    public Integration.Telegram config() {
        return fileFacade.integration().telegram();
    }

    @Override
    public String getIntegrationName() {
        return "Telegram";
    }

    @Override
    public void hook() {
        long taskId = taskGeneration.incrementAndGet();

        TelegramClient telegramClient = telegramClientProvider.create();
        if (telegramClient == null) return;

        if (taskGeneration.get() != taskId) {
            try {
                telegramClient.application().close();
            } catch (Exception _) {
                // just ignore
            }
            return;
        }

        try {
            // register listener
            TelegramMessageListener telegramMessageListener = injector.getInstance(TelegramMessageListener.class);
            telegramClient.registerListener(telegramMessageListener);

            Integration.ChannelInfo channelInfo = config().channelInfo();

            if (channelInfo.enable() && channelInfo.ticker().enable()) {
                long period = channelInfo.ticker().period();
                taskScheduler.runAsyncTimer(this::updateChannelInfo, period, period);
                updateChannelInfo();
            }

            logHook();
        } catch (TelegramApiException e) {
            fLogger.warning(e);
        }
    }

    @Override
    public void unhook() {
        TelegramClient telegramClient = telegramClientProvider.get();
        if (telegramClient == null) return;

        try {
            telegramClient.application().close();
        } catch (Exception e) {
            fLogger.warning(e);
        }

        logUnhook();
    }

    public void updateChannelInfo() {
        TelegramClient telegramClient = telegramClientProvider.get();
        if (telegramClient == null) return;
        if (!config().channelInfo().enable()) return;

        Localization.Integration.Telegram localization = fileFacade.localization().integration().telegram();
        for (Map.Entry<String, String> entry : localization.infoChannel().entrySet()) {
            BotApiMethod<?> botApiMethod;

            String chatId = entry.getKey();
            if (chatId.contains("_")) {
                String[] ids = chatId.split("_");
                if (ids.length != 2) continue;
                if (!NumberUtils.isParsable(ids[0])) continue;
                if (!NumberUtils.isParsable(ids[1])) continue;

                botApiMethod = EditForumTopic.builder()
                        .chatId(ids[0])
                        .messageThreadId(Integer.parseInt(ids[1]))
                        .name(getNewChatName(entry.getValue()))
                        .build();
            } else {
                botApiMethod = SetChatTitle.builder()
                        .chatId(chatId)
                        .title(getNewChatName(entry.getValue()))
                        .build();
            }

            try {
                telegramClient.executeMethod(botApiMethod);
            } catch (TelegramApiException e) {
                fLogger.warning(e);
            }
        }
    }

    @NonNull
    private String getNewChatName(@NonNull String value) {
        return messagePipeline.buildPlain(MessageContext.builder()
                .message(value)
                .build()
        );
    }

}
