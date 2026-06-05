package net.flectone.pulse.module.integration.discord;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ChannelModifyRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.discord.listener.DiscordMessageListener;
import net.flectone.pulse.module.integration.discord.model.DiscordClient;
import net.flectone.pulse.module.integration.discord.provider.DiscordClientProvider;
import net.flectone.pulse.module.integration.discord.service.DiscordWebhookService;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordIntegration implements FIntegration {

    private final AtomicLong taskGeneration = new AtomicLong(0);

    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final DiscordModule discordModule;
    private final DiscordWebhookService discordWebhookService;
    private final DiscordClientProvider discordClientProvider;
    private final Injector injector;

    @Getter private final FLogger fLogger;

    @Override
    public String getIntegrationName() {
        return "Discord";
    }

    @Override
    public void hook() {
        long taskId = taskGeneration.incrementAndGet();

        DiscordClient discordClient = discordClientProvider.create();
        if (discordClient == null) return;

        if (taskGeneration.get() != taskId) {
            discordClient.gateway().logout().block();
            return;
        }

        Integration.ChannelInfo channelInfo = discordModule.config().channelInfo();

        if (channelInfo.enable() && channelInfo.ticker().enable()) {
            long period = channelInfo.ticker().period();
            taskScheduler.runAsyncTimer(this::updateChannelInfo, period, period);
            updateChannelInfo();
        }

        DiscordMessageListener discordMessageListener = injector.getInstance(DiscordMessageListener.class);
        if (!discordModule.config().messageChannel().isEmpty()) {
            discordClient.gateway().getEventDispatcher()
                    .on(discordMessageListener.getEventType())
                    .flatMap(discordMessageListener::execute)
                    .subscribe();
        }

        discordWebhookService.initialize();

        logHook();
    }

    @Override
    public void unhook() {
        DiscordClient discordClient = discordClientProvider.get();
        if (discordClient == null) return;

        discordClient.gateway().logout().block();
        discordWebhookService.clearAll();

        logUnhook();
    }

    private void updateChannelInfo() {
        DiscordClient discordClient = discordClientProvider.get();
        if (discordClient == null) return;
        if (!discordModule.config().channelInfo().enable()) return;

        Localization.Integration.Discord localization = discordModule.localization();
        for (Map.Entry<String, String> entry : localization.infoChannel().entrySet()) {
            String id = entry.getKey();
            if (!NumberUtils.isParsable(id)) continue;

            Snowflake snowflake = Snowflake.of(id);
            discordClient.gateway().getChannelById(snowflake)
                    .flatMap(channel -> channel.getRestChannel().modify(ChannelModifyRequest.builder().name(messagePipeline.buildPlain(MessageContext.builder()
                            .sender(discordClient.sender())
                            .message(entry.getValue())
                            .build()
                    )).build(), null))
                    .subscribe();
        }
    }

}