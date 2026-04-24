package net.flectone.pulse.module.integration.discord.listener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.Embed;
import discord4j.core.object.PartialMessage;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.poll.Poll;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.discord.DiscordIntegration;
import net.flectone.pulse.module.integration.discord.model.DiscordMetadata;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageCreateListener implements EventListener<MessageCreateEvent> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final Provider<DiscordIntegration> discordIntegration;
    private final TaskScheduler taskScheduler;

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<@NonNull MessageCreateEvent> execute(MessageCreateEvent event) {
        Message message = event.getMessage();
        Member member = event.getMember().orElse(null);
        taskScheduler.runAsync(() -> handleMessage(message, member));

        return Mono.empty();
    }

    public void handleMessage(Message message, @Nullable Member member) {
        List<String> channel = config().messageChannel().get(name().name());
        if (channel == null) return;
        if (!channel.contains(message.getChannelId().asString())) return;
        if (member != null && member.isBot() && (config().ignoreAllBots() || member.getId().asLong() == discordIntegration.get().getClientID())) return;

        Webhook webhook = null;

        Optional<Snowflake> webhookId = message.getWebhookId();
        if (webhookId.isPresent()) {
            if (config().ignoreAllWebhooks()) return;

            webhook = message.getWebhook().block();

            // always ignore ourselves
            Optional<User> creator = webhook.getCreator();
            if (creator.isPresent()
                    && creator.get().getId().equals(discordIntegration.get().getClientID())) return;
        }

        // check command in message
        if (executeCommand(message)) return;

        String content = getMessageContent(message);
        sendMessage(
                member,
                webhook,
                content,
                retrieveReply(message).orElse(null)
        );
    }

    public void sendMessage(@Nullable Member member,
                            @Nullable Webhook webhook,
                            @NonNull String message,
                            @Nullable Pair<String, String> reply) {
        String userName = member != null ? member.getUsername() : webhook != null ? webhook.getName().orElse("") : "";
        String globalName = member != null ? member.getGlobalName().orElse(userName) : userName;
        String displayName = member != null ? member.getDisplayName() : globalName;
        String nickname = member != null ? member.getNickname().orElse(userName) : userName;

        messageDispatcher.dispatch(this, DiscordMetadata.<Localization.Integration.Discord>builder()
                .base(EventMetadata.<Localization.Integration.Discord>builder()
                        .sender(discordIntegration.get().getSender())
                        .format(localization -> {
                            Localization.Integration.Discord.ChannelEmbed channelEmbed = localization.messageChannel().get(name().name());
                            if (channelEmbed == null) return "";

                            return StringUtils.replaceEach(
                                    channelEmbed.content(),
                                    new String[]{"<name>", "<global_name>", "<nickname>", "<display_name>", "<user_name>"},
                                    new String[]{globalName, globalName, nickname, displayName, userName}
                            );
                        })
                        .range(Range.get(Range.Type.PROXY))
                        .destination(config().destination())
                        .message(message)
                        .sound(soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{TagResolver.resolver("reply", (_, _) -> {
                            if (reply == null) return MessagePipeline.ReplacementTag.emptyTag();

                            MessageContext tagContext = messagePipeline.createContext(localization(fResolver).formatReply())
                                    .addTagResolvers(
                                            TagResolver.resolver("reply_user", Tag.preProcessParsed(StringUtils.defaultString(reply.first()))),
                                            TagResolver.resolver("reply_message", (_, _) -> {
                                                MessageContext replyContext = messagePipeline.createContext(discordIntegration.get().getSender(), fResolver, reply.second())
                                                        .addFlag(MessageFlag.PLAYER_MESSAGE, true);

                                                return Tag.selfClosingInserting(messagePipeline.build(replyContext));
                                            })
                                    );

                            return Tag.inserting(messagePipeline.build(tagContext));
                        })})
                        .integration(string -> StringUtils.replaceEach(
                                string,
                                new String[]{"<name>", "<global_name>", "<nickname>", "<display_name>", "<user_name>"},
                                new String[]{globalName, globalName, nickname, displayName, userName}
                        ))
                        .build()
                )
                .globalName(globalName)
                .nickname(nickname)
                .displayName(displayName)
                .userName(userName)
                .build()
        );
    }

    @Override
    public Integration.Discord config() {
        return fileFacade.integration().discord();
    }

    @Override
    public Permission.Integration.Discord permission() {
        return fileFacade.permission().integration().discord();
    }

    @Override
    public Localization.Integration.Discord localization(FEntity sender) {
        return fileFacade.localization(sender).integration().discord();
    }

    private Optional<Pair<String, String>> retrieveReply(Message message) {
        if (!message.getMessageSnapshots().isEmpty()) {
            PartialMessage partialMessage = message.getMessageSnapshots().getFirst().getMessage();

            String content = getMessageContent(partialMessage);

            Optional<User> author = partialMessage.getAuthor();
            return author.map(user -> Pair.of(user.getUsername(), content))
                    .or(() -> Optional.of(Pair.of("Unknown", content)));
        }

        Optional<Message> optionalReferencedMessage = message.getReferencedMessage();
        if (optionalReferencedMessage.isEmpty()) return Optional.empty();

        Message referencedMessage = optionalReferencedMessage.get();

        String content = getMessageContent(referencedMessage);

        Optional<User> author = referencedMessage.getAuthor();
        if (author.isPresent()) return Optional.of(Pair.of(author.get().getUsername(), content));

        Optional<Snowflake> webhookId = referencedMessage.getWebhookId();
        if (webhookId.isPresent()) {
            Webhook webhook = referencedMessage.getWebhook().block();
            if (webhook != null) {
                return Optional.of(Pair.of(webhook.getName().orElse("Unknown"), content));
            }
        }

        return Optional.of(Pair.of("Unknown", content));
    }

    @NonNull
    private String getMessageContent(Message message) {
        return getMessageContent(message.getContent(), message.getPoll().orElse(null), message.getAttachments(), message.getEmbeds());
    }

    @NonNull
    private String getMessageContent(PartialMessage partialMessage) {
        return getMessageContent(partialMessage.getContent().orElse(null), null, partialMessage.getAttachments(), partialMessage.getEmbeds());
    }

    @NonNull
    private String getMessageContent(String content, Poll poll, List<Attachment> attachments, List<Embed> embeds) {
        StringBuilder contentBuilder = new StringBuilder();

        if (StringUtils.isNotEmpty(content)) {
            contentBuilder.append(content);
        }

        if (!embeds.isEmpty()) {
            if (!contentBuilder.isEmpty()) {
                contentBuilder.append("\n");
            }

            embeds.forEach(embed -> contentBuilder.append(extractTextFromEmbed(embed)));
        }

        if (poll != null) {
            if (!contentBuilder.isEmpty()) {
                contentBuilder.append("\n");
            }

            contentBuilder.append(poll.getQuestion().getText().orElse("")).append("\n");
            poll.getAnswers().forEach(answer -> contentBuilder
                    .append(" - ")
                    .append(answer.getText().orElse(""))
                    .append("\n")
            );
        }

        if (!attachments.isEmpty()) {
            if (!contentBuilder.isEmpty()) {
                contentBuilder.append(' ');
            }

            contentBuilder.append(attachments.stream()
                    .map(Attachment::getUrl)
                    .collect(Collectors.joining(" "))
            );
        }

        return contentBuilder.toString();
    }

    private String extractTextFromEmbed(Embed embed) {
        StringBuilder stringBuilder = new StringBuilder();

        embed.getAuthor().ifPresent(author -> {
            stringBuilder.append(author.getName().orElse(""));
            stringBuilder.append("\n");
        });

        embed.getTitle().ifPresent(string -> stringBuilder
                .append(string)
                .append("\n")
        );

        embed.getDescription().ifPresent(string -> stringBuilder
                .append(string)
                .append("\n")
        );

        embed.getFooter().ifPresent(footer -> {
            stringBuilder.append(footer.getText());
            stringBuilder.append("\n");
        });

        embed.getFields().forEach(field -> stringBuilder
                .append(field.getName())
                .append(": ")
                .append(field.getValue())
                .append("\n")
        );

        return stringBuilder.toString();
    }

    private boolean executeCommand(Message message) {
        String text = message.getContent();
        if (StringUtils.isEmpty(text)) return false;

        String[] parts = text.toLowerCase().trim().split(" ", 2);
        String commandName = parts[0];
        String arguments = parts.length > 1 ? parts[1] : "";

        Snowflake channel = message.getChannelId();

        for (Map.Entry<String, Integration.Command> commandEntry : config().customCommand().entrySet()) {
            Integration.Command command = commandEntry.getValue();
            if (!command.aliases().contains(commandName)) continue;

            FPlayer fPlayer = getFPlayerArgument(command, arguments, channel);
            if (fPlayer == null) return true;

            Localization.Integration.Discord.ChannelEmbed channelEmbed = localization().customCommand().get(commandEntry.getKey());
            if (channelEmbed == null) return true;

            taskScheduler.runRegion(fPlayer, () -> sendMessageToDiscord(fPlayer, channel, channelEmbed));
            return true;
        }

        return false;
    }

    @Nullable
    private FPlayer getFPlayerArgument(Integration.Command command, String arguments, Snowflake channel) {
        FPlayer fPlayer = fPlayerService.getRandomFPlayer();
        if (Boolean.FALSE.equals(command.needPlayer())) return fPlayer;

        if (arguments.isEmpty()) {
            sendMessageToDiscord(channel, buildMessage(fPlayer, Localization.Integration.Discord::nullPlayer));
            return null;
        }

        String playerName = arguments.split(" ")[0];
        FPlayer argumentFPlayer = fPlayerService.getFPlayer(playerName);
        if (argumentFPlayer.isUnknown()) {
            sendMessageToDiscord(channel, buildMessage(fPlayer, Localization.Integration.Discord::nullPlayer));
            return null;
        }

        return argumentFPlayer;
    }

    private String buildMessage(FPlayer fPlayer, Function<Localization.Integration.Discord, String> stringFunction) {
        return buildMessage(fPlayer, stringFunction.apply(localization()));
    }

    private String buildMessage(FPlayer fPlayer, String localization) {
        MessageContext messageContext = messagePipeline.createContext(fPlayer, localization)
                .addFlags(
                        new MessageFlag[]{MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING, MessageFlag.OBJECT_SPRITE_PROCESSING},
                        new boolean[]{false, false}
                );

        return messagePipeline.buildPlain(messageContext);
    }

    private void sendMessageToDiscord(Snowflake channel, String text) {
        discordIntegration.get().sendMessage(channel, text);
    }

    private void sendMessageToDiscord(FPlayer fPlayer, Snowflake channel, Localization.Integration.Discord.ChannelEmbed channelEmbed) {
        discordIntegration.get().sendMessage(fPlayer, channel, channelEmbed, string -> buildMessage(fPlayer, string));
    }
}
