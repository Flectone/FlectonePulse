package net.flectone.pulse.module.integration.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Status;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.WebhookCreateSpec;
import discord4j.discordjson.json.*;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.MultipartRequest;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Integration;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.discord.listener.MessageCreateListener;
import net.flectone.pulse.module.integration.discord.ticker.DiscordTicker;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.MessageTag;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

@Singleton
public class DiscordIntegration extends AbstractModule implements FIntegration {

    private final Integration.Discord integration;

    private final List<Long> webhooks = new ArrayList<>();

    private final ComponentUtil componentUtil;
    private final FileManager fileManager;
    private final ThreadManager threadManager;
    private final FPlayerManager fPlayerManager;
    private final MessageCreateListener messageCreateListener;
    private final FLogger fLogger;

    private DiscordClient discordClient;
    private GatewayDiscordClient gateway;
    private long clientID;

    @Inject
    private DiscordTicker discordTicker;

    @Inject
    public DiscordIntegration(FileManager fileManager,
                              ThreadManager threadManager,
                              FPlayerManager fPlayerManager,
                              ComponentUtil componentUtil,
                              MessageCreateListener messageCreateListener,
                              FLogger fLogger) {
        this.fileManager = fileManager;
        this.threadManager = threadManager;
        this.fPlayerManager = fPlayerManager;
        this.componentUtil = componentUtil;
        this.messageCreateListener = messageCreateListener;
        this.fLogger = fLogger;

        integration = fileManager.getIntegration().getDiscord();
    }

    @Async
    public void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> discordString) {
        String integrationChannel = integration.getMessageChannel().get(messageTag);
        if (integrationChannel == null) return;
        if (integrationChannel.isEmpty()) return;

        Localization.Integration.Discord localization = fileManager.getLocalization().getIntegration().getDiscord();
        Localization.Integration.Discord.ChannelEmbed messageChannelEmbed = localization.getMessageChannel().get(messageTag);
        if (messageChannelEmbed == null) return;

        String skin = fPlayerManager.getSkin(sender);
        UnaryOperator<String> replaceString = s -> discordString.apply(s)
                .replace("<skin>", skin);

        EmbedCreateSpec embed = null;

        if (messageChannelEmbed.getEmbed().isEnable()) {
            embed = createEmbed(messageChannelEmbed, replaceString);
        }

        Localization.Integration.Discord.Webhook messageWebhook = messageChannelEmbed.getWebhook();
        if (messageChannelEmbed.getWebhook().isEnable()) {

            long channelID = Snowflake.of(integrationChannel).asLong();

            WebhookData webhookPlayerData = discordClient.getWebhookService().getChannelWebhooks(channelID)
                    .filter(webhookData -> webhookData.name().isPresent() && webhookData.name().get().equals(sender.getName()))
                    .blockFirst();

            if (webhookPlayerData == null) {
                String avatarURL = replaceString.apply(messageWebhook.getAvatar());
                webhookPlayerData = createWebhook(avatarURL, sender.getName(), channelID);
            }

            if (webhookPlayerData == null) return;

            long webhookID = webhookPlayerData.id().asLong();

            if (!webhooks.contains(webhookID)
                    && webhookPlayerData.applicationId().isPresent()
                    && webhookPlayerData.applicationId().get().asLong() == clientID) {
                webhooks.add(webhookID);
                threadManager.runAsyncLater(() -> {
                    discordClient.getWebhookService().deleteWebhook(webhookID, null)
                            .subscribe();
                    webhooks.remove(webhookID);
                }, 20 * 60);
            }

            ImmutableWebhookExecuteRequest.Builder webhookBuilder = WebhookExecuteRequest.builder()
                    .allowedMentions(AllowedMentionsData.builder().build());

            if (embed != null) {
                webhookBuilder.addEmbed(embed.asRequest());
            }

            webhookBuilder.content(replaceString.apply(messageWebhook.getContent()));

            discordClient.getWebhookService().executeWebhook(webhookPlayerData.id().asLong(), webhookPlayerData.token().get(), true,
                    MultipartRequest.ofRequest(webhookBuilder.build())
            ).subscribe();

            return;
        }

        MessageCreateSpec.Builder messageCreateSpecBuilder = MessageCreateSpec.builder()
                .allowedMentions(AllowedMentions.suppressAll());

        if (embed != null) {
            messageCreateSpecBuilder.addEmbed(embed);
        }

        String content = replaceString.apply(messageChannelEmbed.getContent());
        messageCreateSpecBuilder.content(content);

        discordClient.getChannelById(Snowflake.of(integrationChannel))
                .createMessage(messageCreateSpecBuilder.build().asRequest())
                .subscribe();
    }

    private WebhookData createWebhook(String avatarURL, String fPlayerName, long channelID) {
        WebhookCreateSpec webhook = WebhookCreateSpec.builder()
                .avatarOrNull(discord4j.rest.util.Image.ofUrl(avatarURL).block())
                .name(fPlayerName)
                .build();

        return discordClient.getWebhookService().createWebhook(channelID, webhook.asRequest(), null)
                .block();
    }

    private EmbedCreateSpec createEmbed(Localization.Integration.Discord.ChannelEmbed localizationChannel,
                                        UnaryOperator<String> discordString) {
        Localization.Integration.Discord.Embed embed = localizationChannel.getEmbed();
        if (!embed.isEnable()) return null;

        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder();

        if (!embed.getColor().isEmpty()) {
            Color color = Color.decode(embed.getColor());
            embedBuilder.color(discord4j.rest.util.Color.of(color.getRGB()));
        }

        if (!embed.getTitle().isEmpty()) {
            embedBuilder.title(discordString.apply(embed.getTitle()));
        }

        if (!embed.getUrl().isEmpty()) {
            embedBuilder.url(discordString.apply(embed.getUrl()));
        }

        Localization.Integration.Discord.Embed.Author author = embed.getAuthor();
        if (!author.getName().isEmpty() || !author.getUrl().isEmpty() || !author.getIconUrl().isEmpty()) {
            embedBuilder.author(
                    discordString.apply(author.getName()),
                    discordString.apply(author.getUrl()),
                    discordString.apply(author.getIconUrl())
            );
        }

        if (!embed.getDescription().isEmpty()) {
            embedBuilder.description(discordString.apply(embed.getDescription()));
        }

        if (!embed.getThumbnail().isEmpty()) {
            embedBuilder.thumbnail(discordString.apply(embed.getThumbnail()));
        }

        if (!embed.getFields().isEmpty()) {
            for (var field : embed.getFields()) {
                embedBuilder.addField(
                        discordString.apply(field.getName()),
                        discordString.apply(field.getValue()),
                        field.isInline()
                );
            }
        }

        if (!embed.getImage().isEmpty()) {
            embedBuilder.image(discordString.apply(embed.getImage()));
        }

        if (embed.isTimestamp()) {
            embedBuilder.timestamp(Instant.now());
        }

        Localization.Integration.Discord.Embed.Footer footer = embed.getFooter();

        if (!footer.getText().isEmpty() || !footer.getIconUrl().isEmpty()) {
            embedBuilder.footer(
                    discordString.apply(footer.getText()),
                    discordString.apply(footer.getIconUrl())
            );
        }

        return embedBuilder.build();
    }

    @Async
    @Override
    public void hook() {
        String token = integration.getToken();
        if (token.isEmpty()) return;

        discordClient = DiscordClient.create(token);

        gateway = discordClient.gateway().login().block();
        if (gateway == null) return;

        Integration.Discord.Presence presence = integration.getPresence();

        if (presence.isEnable()) {
            Integration.Discord.Presence.Activity activity = presence.getActivity();

            ClientActivity clientActivity = activity.isEnable()
                    ? ClientActivity.of(Activity.Type.valueOf(activity.getType()), activity.getName(), activity.getUrl())
                    : null;

            gateway.updatePresence(ClientPresence.of(Status.valueOf(presence.getStatus()), clientActivity)).block();
        }

        Integration.Discord.ChannelInfo channelInfo = integration.getChannelInfo();

        if (channelInfo.isEnable()) {
            if (channelInfo.getTicker().isEnable()) {
                discordTicker.runTaskTimerAsync(channelInfo.getTicker().getPeriod(), channelInfo.getTicker().getPeriod());
                updateChannelInfo();
            }
        }

        if (!integration.getMessageChannel().isEmpty()) {
            gateway.getEventDispatcher()
                    .on(messageCreateListener.getEventType())
                    .flatMap(messageCreateListener::execute)
                    .subscribe();
        }

        ApplicationInfo applicationInfo = gateway.getApplicationInfo().block();
        if (applicationInfo == null) return;

        clientID = applicationInfo.getId().asLong();

        fLogger.info("Discord integration enabled");
    }

    public void updateChannelInfo() {
        if (gateway == null) return;

        if (!integration.getChannelInfo().isEnable()) return;

        Localization.Integration.Discord localization = fileManager.getLocalization().getIntegration().getDiscord();
        for (Map.Entry<String, String> entry : localization.getInfoChannel().entrySet()) {
            gateway.getChannelById(Snowflake.of(entry.getKey()))
                    .blockOptional()
                    .ifPresent(channel -> {
                        String name = PlainTextComponentSerializer.plainText()
                                .serialize(componentUtil.builder(entry.getValue()).build());

                        channel.getRestChannel()
                                .modify(ChannelModifyRequest.builder()
                                                .name(name)
                                                .build(),
                                        null
                                )
                                .block();
                    });
        }
    }

    @Override
    public void reload() {
        disconnect();

        if (!isEnable()) return;
        hook();
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public void disconnect() {
        if (gateway == null) return;

        gateway.logout().block();
        webhooks.clear();
    }
}
