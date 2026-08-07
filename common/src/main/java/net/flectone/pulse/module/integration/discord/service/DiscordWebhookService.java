package net.flectone.pulse.module.integration.discord.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.common.util.Snowflake;
import discord4j.core.spec.WebhookCreateSpec;
import discord4j.discordjson.json.WebhookData;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.discord.model.DiscordClient;
import net.flectone.pulse.module.integration.discord.parser.DiscordSnowflakeParser;
import net.flectone.pulse.module.integration.discord.provider.DiscordClientProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordWebhookService {

    private final Map<Long, WebhookData> channelWebhooks = new ConcurrentHashMap<>();

    private final DiscordModule discordModule;
    private final DiscordSnowflakeParser discordSnowflakeParser;
    private final DiscordClientProvider discordClientProvider;

    public void initialize() {
        DiscordClient discordClient = discordClientProvider.get();
        if (discordClient == null) return;

        Set<Long> uniqueChannels = discordModule.config().messageChannel().values().stream()
                .flatMap(List::stream)
                .filter(id -> !id.isEmpty())
                .map(id -> {
                    Optional<Snowflake> snowflake = discordSnowflakeParser.parse(id);
                    if (snowflake.isEmpty()) return null;

                    return snowflake.get().asLong();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (long channelID : uniqueChannels) {
            List<WebhookData> botWebhooks = discordClient.client().getWebhookService().getChannelWebhooks(channelID)
                    .filter(data -> data.applicationId().isPresent() && data.applicationId().get().asLong() == discordClient.id())
                    .collectList()
                    .block();

            if (botWebhooks != null && !botWebhooks.isEmpty()) {
                WebhookData kept = botWebhooks.getFirst();
                for (int i = 1; i < botWebhooks.size(); i++) {
                    discordClient.client().getWebhookService().deleteWebhook(botWebhooks.get(i).id().asLong(), null).block();
                }

                channelWebhooks.put(channelID, kept);
            }
        }
    }

    public void clearAll() {
        channelWebhooks.clear();
    }

    @Nullable
    public WebhookData createWebhook(long channelID) {
        DiscordClient discordClient = discordClientProvider.get();
        if (discordClient == null) return null;

        WebhookCreateSpec.Builder builder = WebhookCreateSpec.builder()
                .name(BuildConfig.PROJECT_NAME + "Webhook");

        WebhookCreateSpec webhook = builder.build();

        return discordClient.client()
                .getWebhookService().createWebhook(channelID, webhook.asRequest(), null)
                .block();
    }

    @Nullable
    public WebhookData getWebhook(long channelId) {
        return channelWebhooks.get(channelId);
    }

    public void saveWebhook(long channelId, @NonNull WebhookData webhookData) {
        channelWebhooks.put(channelId, webhookData);
    }

}
