package net.flectone.pulse.module.integration.discord.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Webhook;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.ImmutableWebhookExecuteRequest;
import discord4j.discordjson.json.WebhookData;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.MultipartRequest;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.discord.model.DiscordClient;
import net.flectone.pulse.module.integration.discord.model.DiscordMetadata;
import net.flectone.pulse.module.integration.discord.parser.DiscordSnowflakeParser;
import net.flectone.pulse.module.integration.discord.provider.DiscordClientProvider;
import net.flectone.pulse.module.integration.discord.service.DiscordWebhookService;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordSender {

    private final SkinService skinService;
    private final DiscordModule discordModule;
    private final DiscordClientProvider discordClientProvider;
    private final DiscordSnowflakeParser discordSnowflakeParser;
    private final DiscordWebhookService discordWebhookService;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;

    public void sendMessage(@NonNull FEntity sender,
                            @NonNull String messageName,
                            @NonNull UnaryOperator<String> discordString) {
        if (discordClientProvider.get() == null) return;

        List<String> channels = discordModule.config().messageChannel().get(messageName);
        if (channels == null) return;
        if (channels.isEmpty()) return;

        channels.forEach(string -> {
            Optional<Snowflake> channel = discordSnowflakeParser.parse(string);
            if (channel.isEmpty()) return;

            Localization.Integration.Discord localization = discordModule.localization();
            Localization.Integration.Discord.ChannelEmbed channelEmbed = localization.messageChannel().getOrDefault(messageName, new Localization.Integration.Discord.ChannelEmbed("<final_message>", null, null, null));
            sendMessage(sender, channel.get(), channelEmbed, discordString);
        });
    }

    public void sendMessage(@NonNull Snowflake channel,
                            @NonNull String text) {
        DiscordClient discordClient = discordClientProvider.get();
        if (discordClient == null) return;

        MessageCreateSpec.Builder messageCreateSpecBuilder = MessageCreateSpec.builder()
                .allowedMentions(AllowedMentions.suppressAll())
                .content(text);

        discordClient.client().getChannelById(channel)
                .createMessage(messageCreateSpecBuilder.build().asRequest())
                .subscribe();
    }

    public void sendMessage(@NonNull FEntity sender,
                            @NonNull Snowflake channel,
                            Localization.Integration.Discord.@NonNull ChannelEmbed channelEmbed,
                            @NonNull UnaryOperator<String> discordString) {
        if (channelEmbed == null) return;

        DiscordClient discordClient = discordClientProvider.get();
        if (discordClient == null) return;

        String skin = skinService.getSkin(sender);

        UnaryOperator<String> replaceSkin = string -> Strings.CS.replace(
                string,
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

            WebhookData webhookData = discordWebhookService.getWebhook(channelID);

            if (webhookData == null) {
                webhookData = discordWebhookService.createWebhook(channelID);
                if (webhookData == null) return;

                discordWebhookService.saveWebhook(channelID, webhookData);
            }

            ImmutableWebhookExecuteRequest.Builder webhookBuilder = WebhookExecuteRequest.builder()
                    .allowedMentions(AllowedMentionsData.builder().build())
                    .username(StringUtils.isEmpty(channelEmbed.webhookName()) || "<player>".equals(channelEmbed.webhookName())
                            ? sender.name()
                            : messagePipeline.buildPlain(MessageContext.builder()
                                                         .sender(sender)
                                                         .receiver(FPlayer.UNKNOWN)
                                                         .message(channelEmbed.webhookName())
                                                         .build()
                            )
                    )
                    .avatarUrl(replaceSkin.apply(webhookAvatar))
                    .content(replaceString.apply(channelEmbed.content()));

            if (embed != null) {
                webhookBuilder.addEmbed(embed.asRequest());
            }

            discordClient.client().getWebhookService().executeWebhook(
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

        discordClient.client().getChannelById(channel)
                .createMessage(messageCreateSpecBuilder.build().asRequest())
                .subscribe();
    }

    public void sendMessage(@NonNull String channelId,
                            @Nullable Member member,
                            @Nullable Webhook webhook,
                            @NonNull String message,
                            @Nullable Pair<String, String> reply) {
        DiscordClient discordClient = discordClientProvider.get();
        if (discordClient == null) return;

        String userName = member != null ? member.getUsername() : webhook != null ? webhook.getName().orElse("") : "";
        String globalName = member != null ? member.getGlobalName().orElse(userName) : userName;
        String displayName = member != null ? member.getDisplayName() : globalName;
        String nickname = member != null ? member.getNickname().orElse(userName) : userName;

        messageDispatcher.dispatch(discordModule, DiscordMetadata.<Localization.Integration.Discord>builder()
                .base(EventMetadata.<Localization.Integration.Discord>builder()
                        .sender(discordClient.sender())
                        .format(localization -> {
                            Localization.Integration.Discord.ChannelEmbed channelEmbed = localization.messageChannel().get(discordModule.name().name());
                            if (channelEmbed == null) return "";

                            return StringUtils.replaceEach(
                                    channelEmbed.content(),
                                    new String[]{"<name>", "<global_name>", "<nickname>", "<display_name>", "<user_name>"},
                                    new String[]{globalName, globalName, nickname, displayName, userName}
                            );
                        })
                        .range(Range.get(Range.Type.PROXY))
                        .destination(discordModule.config().destination())
                        .message(message)
                        .sound(discordModule.soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.resolver("reply", (_, _) -> {
                            if (reply == null) return MessagePipeline.ReplacementTag.emptyTag();

                            return Tag.inserting(messagePipeline.build(MessageContext.builder()
                                    .message(discordModule.localization(fResolver).formatReply())
                                    .tagResolvers(
                                            messagePipeline.resolver("reply_user", Tag.preProcessParsed(StringUtils.defaultString(reply.getLeft()))),
                                            messagePipeline.resolver("reply_message", (_, _) -> Tag.selfClosingInserting(messagePipeline.build(MessageContext.builder()
                                                    .sender(discordClient.sender())
                                                    .receiver(fResolver)
                                                    .message(reply.getRight())
                                                    .flag(MessageFlag.PLAYER_MESSAGE, true)
                                                    .build()
                                            )))
                                    )
                                    .build()
                            ));
                        })})
                        .integration(IntegrationMetadata.builder()
                                .format(string -> StringUtils.replaceEach(
                                        string,
                                        new String[]{"<name>", "<global_name>", "<nickname>", "<display_name>", "<user_name>"},
                                        new String[]{globalName, globalName, nickname, displayName, userName}
                                ))
                                .messageNames(List.of(discordModule.name().name() + "_" + channelId))
                                .build()
                        )
                        .build()
                )
                .globalName(globalName)
                .nickname(nickname)
                .displayName(displayName)
                .userName(userName)
                .build()
        );
    }

    private EmbedCreateSpec createEmbed(Localization.Integration.Discord.@NonNull Embed embed,
                                        @NonNull UnaryOperator<String> replaceSkin,
                                        @NonNull UnaryOperator<String> discordString) {
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
        if (author != null) {
            embedBuilder.author(
                    discordString.apply(StringUtils.defaultString(author.name())),
                    replaceSkin.apply(StringUtils.defaultString(author.url())),
                    replaceSkin.apply(StringUtils.defaultString(author.iconUrl()))
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

        if (Boolean.TRUE.equals(embed.timestamp())) {
            embedBuilder.timestamp(Instant.now());
        }

        Localization.Integration.Discord.Embed.Footer footer = embed.footer();
        if (footer != null) {
            embedBuilder.footer(
                    discordString.apply(StringUtils.defaultString(footer.text())),
                    replaceSkin.apply(StringUtils.defaultString(footer.iconUrl()))
            );
        }

        if (embed.fields() != null && !embed.fields().isEmpty()) {
            for (Localization.Integration.Discord.Embed.Field field : embed.fields()) {
                if (StringUtils.isEmpty(field.name()) || StringUtils.isEmpty(field.value())) continue;

                embedBuilder.addField(field.name(), field.value(), Boolean.TRUE.equals(field.inline()));
            }
        }

        return embedBuilder.build();
    }

}
