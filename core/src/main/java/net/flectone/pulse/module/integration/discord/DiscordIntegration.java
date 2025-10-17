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
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.discord.listener.MessageCreateListener;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordIntegration implements FIntegration {

    private final Map<Long, WebhookData> channelWebhooks = new HashMap<>();

    private final FileResolver fileResolver;
    private final TaskScheduler taskScheduler;
    private final SkinService skinService;
    private final MessageCreateListener messageCreateListener;
    private final MessagePipeline messagePipeline;
    private final SystemVariableResolver systemVariableResolver;
    private final FLogger fLogger;

    private DiscordClient discordClient;
    private GatewayDiscordClient gateway;
    private long clientID;

    public Integration.Discord config() {
        return fileResolver.getIntegration().getDiscord();
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> discordString) {
        if (gateway == null) return;

        String integrationChannel = config().getMessageChannel().get(messageName);
        if (integrationChannel == null) return;
        if (integrationChannel.isEmpty()) return;

        Localization.Integration.Discord localization = fileResolver.getLocalization().getIntegration().getDiscord();
        Localization.Integration.Discord.ChannelEmbed messageChannelEmbed = localization.getMessageChannel().getOrDefault(messageName, new Localization.Integration.Discord.ChannelEmbed());

        String skin = skinService.getSkin(sender);

        UnaryOperator<String> replaceSkin = s -> Strings.CS.replace(
                s,
                "<skin>",
                skin
        );

        UnaryOperator<String> replaceString = s -> discordString.andThen(replaceSkin).apply(s);

        Localization.Integration.Discord.Embed messageEmbed = messageChannelEmbed.getEmbed();

        EmbedCreateSpec embed = null;
        if (messageEmbed != null) {
            embed = createEmbed(messageEmbed, replaceSkin, replaceString);
        }

        String webhookAvatar = messageChannelEmbed.getWebhookAvatar();
        if (StringUtils.isNotEmpty(webhookAvatar)) {
            long channelID = Snowflake.of(integrationChannel).asLong();

            WebhookData webhookData = channelWebhooks.get(channelID);

            if (webhookData == null) {
                webhookData = createWebhook(channelID);
                if (webhookData == null) return;

                channelWebhooks.put(channelID, webhookData);
            }

            String username = sender.getName();

            ImmutableWebhookExecuteRequest.Builder webhookBuilder = WebhookExecuteRequest.builder()
                    .allowedMentions(AllowedMentionsData.builder().build())
                    .username(username)
                    .avatarUrl(replaceSkin.apply(webhookAvatar))
                    .content(replaceString.apply(messageChannelEmbed.getContent()));

            if (embed != null) {
                webhookBuilder.addEmbed(embed.asRequest());
            }

            discordClient.getWebhookService().executeWebhook(
                    webhookData.id().asLong(),
                    webhookData.token().get(),
                    false,
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

    private WebhookData createWebhook(long channelID) {
        WebhookCreateSpec.Builder builder = WebhookCreateSpec.builder()
                .name(BuildConfig.PROJECT_NAME + "Webhook");

        WebhookCreateSpec webhook = builder.build();

        return discordClient.getWebhookService().createWebhook(channelID, webhook.asRequest(), null)
                .block();
    }

    private EmbedCreateSpec createEmbed(Localization.Integration.Discord.Embed embed,
                                        UnaryOperator<String> replaceSkin,
                                        UnaryOperator<String> discordString) {
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder();

        if (StringUtils.isNotEmpty(embed.getColor())) {
            Color color = Color.decode(embed.getColor());
            embedBuilder.color(discord4j.rest.util.Color.of(color.getRGB()));
        }

        if (StringUtils.isNotEmpty(embed.getTitle())) {
            embedBuilder.title(discordString.apply(embed.getTitle()));
        }

        if (StringUtils.isNotEmpty(embed.getUrl())) {
            embedBuilder.url(replaceSkin.apply(embed.getUrl()));
        }

        Localization.Integration.Discord.Embed.Author author = embed.getAuthor();
        if (StringUtils.isNotEmpty(author.getName())
                || StringUtils.isNotEmpty(author.getUrl())
                || StringUtils.isNotEmpty(author.getIconUrl())) {
            embedBuilder.author(
                    discordString.apply(author.getName()),
                    replaceSkin.apply(author.getUrl()),
                    replaceSkin.apply(author.getIconUrl())
            );
        }

        if (StringUtils.isNotEmpty(embed.getDescription())) {
            embedBuilder.description(discordString.apply(embed.getDescription()));
        }

        if (StringUtils.isNotEmpty(embed.getThumbnail())) {
            embedBuilder.thumbnail(discordString.apply(embed.getThumbnail()));
        }

        if (StringUtils.isNotEmpty(embed.getImage())) {
            embedBuilder.image(replaceSkin.apply(embed.getImage()));
        }

        if (embed.isTimestamp()) {
            embedBuilder.timestamp(Instant.now());
        }

        Localization.Integration.Discord.Embed.Footer footer = embed.getFooter();
        if (StringUtils.isNotEmpty(footer.getText()) || StringUtils.isNotEmpty(footer.getIconUrl())) {
            embedBuilder.footer(
                    discordString.apply(footer.getText()),
                    replaceSkin.apply(footer.getIconUrl())
            );
        }

        if (embed.getFields() != null && !embed.getFields().isEmpty()) {
            for (Localization.Integration.Discord.Embed.Field field : embed.getFields()) {
                if (StringUtils.isEmpty(field.getName()) || StringUtils.isEmpty(field.getValue())) continue;

                embedBuilder.addField(field.getName(), field.getValue(), field.isInline());
            }
        }

        return embedBuilder.build();
    }

    @Override
    public void hook() {
        String token = systemVariableResolver.substituteEnvVars(config().getToken());
        if (token.isEmpty()) return;

        discordClient = DiscordClient.create(token);

        gateway = discordClient.gateway().login().block();
        if (gateway == null) return;

        Integration.Discord.Presence presence = config().getPresence();

        if (presence.isEnable()) {
            Integration.Discord.Presence.Activity activity = presence.getActivity();

            ClientActivity clientActivity = activity.isEnable()
                    ? ClientActivity.of(Activity.Type.valueOf(activity.getType()), activity.getName(), activity.getUrl())
                    : null;

            gateway.updatePresence(ClientPresence.of(Status.valueOf(presence.getStatus()), clientActivity)).block();
        }

        Integration.Discord.ChannelInfo channelInfo = config().getChannelInfo();

        if (channelInfo.isEnable() && channelInfo.getTicker().isEnable()) {
            long period = channelInfo.getTicker().getPeriod();
            taskScheduler.runAsyncTimer(this::updateChannelInfo, period, period);
            updateChannelInfo();
        }

        if (!config().getMessageChannel().isEmpty()) {
            gateway.getEventDispatcher()
                    .on(messageCreateListener.getEventType())
                    .flatMap(messageCreateListener::execute)
                    .subscribe();
        }

        ApplicationInfo applicationInfo = gateway.getApplicationInfo().block();
        if (applicationInfo == null) return;

        clientID = applicationInfo.getId().asLong();

        Set<Long> uniqueChannels = config().getMessageChannel().values().stream()
                .filter(id -> !id.isEmpty())
                .map(id -> Snowflake.of(id).asLong())
                .collect(Collectors.toSet());

        for (long channelID : uniqueChannels) {
            List<WebhookData> botWebhooks = discordClient.getWebhookService().getChannelWebhooks(channelID)
                    .filter(data -> data.applicationId().isPresent() && data.applicationId().get().asLong() == clientID)
                    .collectList()
                    .block();

            if (botWebhooks != null && !botWebhooks.isEmpty()) {
                WebhookData kept = botWebhooks.getFirst();
                for (int i = 1; i < botWebhooks.size(); i++) {
                    discordClient.getWebhookService().deleteWebhook(botWebhooks.get(i).id().asLong(), null).block();
                }

                channelWebhooks.put(channelID, kept);
            }
        }

        fLogger.info("Discord integration enabled");
    }

    @Override
    public void unhook() {
        if (gateway == null) return;

        gateway.logout().block();
        channelWebhooks.clear();

        fLogger.info("Discord integration disabled");
    }

    public void updateChannelInfo() {
        if (gateway == null) return;

        if (!config().getChannelInfo().isEnable()) return;

        Localization.Integration.Discord localization = fileResolver.getLocalization().getIntegration().getDiscord();
        for (Map.Entry<String, String> entry : localization.getInfoChannel().entrySet()) {
            String id = entry.getKey();
            if (!NumberUtils.isParsable(id)) continue;

            Snowflake snowflake = Snowflake.of(id);
            gateway.getChannelById(snowflake)
                    .blockOptional()
                    .ifPresent(channel -> {
                        String name = PlainTextComponentSerializer.plainText()
                                .serialize(messagePipeline.builder(entry.getValue()).build());

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
}