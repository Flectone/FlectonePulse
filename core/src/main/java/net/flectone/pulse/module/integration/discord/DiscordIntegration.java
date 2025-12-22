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
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.discord.listener.MessageCreateListener;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordIntegration implements FIntegration {

    private final Map<Long, WebhookData> channelWebhooks = new HashMap<>();

    private final FileFacade fileFacade;
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
        return fileFacade.integration().discord();
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> discordString) {
        if (gateway == null) return;

        List<String> channels = config().messageChannel().get(messageName);
        if (channels == null) return;
        if (channels.isEmpty()) return;

        channels.forEach(string -> {
            Optional<Snowflake> channel = parseSnowflake(string);
            if (channel.isEmpty()) return;

            Localization.Integration.Discord localization = fileFacade.localization().integration().discord();
            Localization.Integration.Discord.ChannelEmbed channelEmbed = localization.messageChannel().getOrDefault(messageName, new Localization.Integration.Discord.ChannelEmbed("<final_message>", null, null));
            sendMessage(sender, channel.get(), channelEmbed, discordString);
        });
    }

    public void sendMessage(Snowflake channel, String text) {
        MessageCreateSpec.Builder messageCreateSpecBuilder = MessageCreateSpec.builder()
                .allowedMentions(AllowedMentions.suppressAll())
                .content(text);

        discordClient.getChannelById(channel)
                .createMessage(messageCreateSpecBuilder.build().asRequest())
                .subscribe();
    }

    public void sendMessage(FEntity sender, Snowflake channel, Localization.Integration.Discord.ChannelEmbed channelEmbed, UnaryOperator<String> discordString) {
        if (channelEmbed == null) return;

        String skin = skinService.getSkin(sender);

        UnaryOperator<String> replaceSkin = s -> Strings.CS.replace(
                s,
                "<skin>",
                skin
        );

        UnaryOperator<String> replaceString = s -> discordString.andThen(replaceSkin).apply(s);

        Localization.Integration.Discord.Embed messageEmbed = channelEmbed.embed();

        EmbedCreateSpec embed = null;
        if (messageEmbed != null) {
            embed = createEmbed(messageEmbed, replaceSkin, replaceString);
        }

        String webhookAvatar = channelEmbed.webhookAvatar();
        if (StringUtils.isNotEmpty(webhookAvatar)) {
            long channelID = channel.asLong();

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
                    .content(replaceString.apply(channelEmbed.content()));

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

        String content = replaceString.apply(channelEmbed.content());
        if (StringUtils.isEmpty(content) && embed == null) return;

        messageCreateSpecBuilder.content(content);

        discordClient.getChannelById(channel)
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

        if (StringUtils.isNotEmpty(embed.color())) {
            Color color = Color.decode(embed.color());
            embedBuilder.color(discord4j.rest.util.Color.of(color.getRGB()));
        }

        if (StringUtils.isNotEmpty(embed.title())) {
            embedBuilder.title(discordString.apply(embed.title()));
        }

        if (StringUtils.isNotEmpty(embed.url())) {
            embedBuilder.url(replaceSkin.apply(embed.url()));
        }

        Localization.Integration.Discord.Embed.Author author = embed.author();
        if (StringUtils.isNotEmpty(author.name())
                || StringUtils.isNotEmpty(author.url())
                || StringUtils.isNotEmpty(author.iconUrl())) {
            embedBuilder.author(
                    discordString.apply(author.name()),
                    replaceSkin.apply(author.url()),
                    replaceSkin.apply(author.iconUrl())
            );
        }

        if (StringUtils.isNotEmpty(embed.description())) {
            embedBuilder.description(discordString.apply(embed.description()));
        }

        if (StringUtils.isNotEmpty(embed.thumbnail())) {
            embedBuilder.thumbnail(discordString.apply(embed.thumbnail()));
        }

        if (StringUtils.isNotEmpty(embed.image())) {
            embedBuilder.image(replaceSkin.apply(embed.image()));
        }

        if (embed.timestamp()) {
            embedBuilder.timestamp(Instant.now());
        }

        Localization.Integration.Discord.Embed.Footer footer = embed.footer();
        if (StringUtils.isNotEmpty(footer.text()) || StringUtils.isNotEmpty(footer.iconUrl())) {
            embedBuilder.footer(
                    discordString.apply(footer.text()),
                    replaceSkin.apply(footer.iconUrl())
            );
        }

        if (embed.fields() != null && !embed.fields().isEmpty()) {
            for (Localization.Integration.Discord.Embed.Field field : embed.fields()) {
                if (StringUtils.isEmpty(field.name()) || StringUtils.isEmpty(field.value())) continue;

                embedBuilder.addField(field.name(), field.value(), field.inline());
            }
        }

        return embedBuilder.build();
    }

    @Override
    public void hook() {
        String token = systemVariableResolver.substituteEnvVars(config().token());
        if (token.isEmpty()) return;

        discordClient = DiscordClient.create(token);

        gateway = discordClient.gateway().login().block();
        if (gateway == null) return;

        Integration.Discord.Presence presence = config().presence();

        if (presence.enable()) {
            Integration.Discord.Presence.Activity activity = presence.activity();

            ClientActivity clientActivity = activity.enable()
                    ? ClientActivity.of(Activity.Type.valueOf(activity.type()), activity.name(), activity.url())
                    : null;

            gateway.updatePresence(ClientPresence.of(Status.valueOf(presence.status()), clientActivity)).block();
        }

        Integration.ChannelInfo channelInfo = config().channelInfo();

        if (channelInfo.enable() && channelInfo.ticker().enable()) {
            long period = channelInfo.ticker().period();
            taskScheduler.runAsyncTimer(this::updateChannelInfo, period, period);
            updateChannelInfo();
        }

        if (!config().messageChannel().isEmpty()) {
            gateway.getEventDispatcher()
                    .on(messageCreateListener.getEventType())
                    .flatMap(messageCreateListener::execute)
                    .subscribe();
        }

        ApplicationInfo applicationInfo = gateway.getApplicationInfo().block();
        if (applicationInfo == null) return;

        clientID = applicationInfo.getId().asLong();

        Set<Long> uniqueChannels = config().messageChannel().values().stream()
                .flatMap(List::stream)
                .filter(id -> !id.isEmpty())
                .map(id -> {
                    Optional<Snowflake> snowflake = parseSnowflake(id);
                    if (snowflake.isEmpty()) return null;

                    return snowflake.get().asLong();
                })
                .filter(Objects::nonNull)
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

        if (!config().channelInfo().enable()) return;

        Localization.Integration.Discord localization = fileFacade.localization().integration().discord();
        for (Map.Entry<String, String> entry : localization.infoChannel().entrySet()) {
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

    private Optional<Snowflake> parseSnowflake(String string) {
        try {
            return Optional.of(Snowflake.of(string));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}