package net.flectone.pulse.module.integration.discord.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.Embed;
import discord4j.core.object.PartialMessage;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.poll.Poll;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.discord.execution.dispatcher.DiscordCommandDispatcher;
import net.flectone.pulse.module.integration.discord.model.DiscordClient;
import net.flectone.pulse.module.integration.discord.provider.DiscordClientProvider;
import net.flectone.pulse.module.integration.discord.sender.DiscordSender;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordMessageListener implements DiscordEventListener<MessageCreateEvent> {

    private final DiscordModule discordModule;
    private final DiscordClientProvider discordClientProvider;
    private final DiscordCommandDispatcher discordCommandDispatcher;
    private final DiscordSender discordSender;
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

    public void handleMessage(@NonNull Message message,
                              @Nullable Member member) {
        List<String> channel = discordModule.config().messageChannel().get(discordModule.name().name());
        if (channel == null) return;

        String channelId = message.getChannelId().asString();
        if (!channel.contains(channelId)) return;

        DiscordClient discordClient = discordClientProvider.get();
        if (discordClient == null) return;
        if (member != null && member.isBot() && (discordModule.config().ignoreAllBots() || member.getId().asLong() == discordClient.id())) return;

        Webhook webhook = null;

        Optional<Snowflake> webhookId = message.getWebhookId();
        if (webhookId.isPresent()) {
            if (discordModule.config().ignoreAllWebhooks()) return;

            webhook = message.getWebhook().block();
            if (webhook == null) return;

            // always ignore ourselves
            Optional<User> creator = webhook.getCreator();
            if (creator.isPresent() && creator.get().getId().asLong() == discordClient.id()) return;
        }

        // check command in message
        if (discordCommandDispatcher.executeCommand(message)) return;

        String content = getMessageContent(message);
        discordSender.sendMessage(
                channelId,
                member,
                webhook,
                content,
                retrieveReply(message).orElse(null)
        );
    }

    private Optional<Pair<String, String>> retrieveReply(@NonNull Message message) {
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
    private String getMessageContent(@NonNull Message message) {
        return getMessageContent(message.getContent(), message.getPoll().orElse(null), message.getAttachments(), message.getEmbeds());
    }

    @NonNull
    private String getMessageContent(@NonNull PartialMessage partialMessage) {
        return getMessageContent(partialMessage.getContent().orElse(null), null, partialMessage.getAttachments(), partialMessage.getEmbeds());
    }

    @NonNull
    private String getMessageContent(@Nullable String content,
                                     @Nullable Poll poll,
                                     @NonNull List<Attachment> attachments,
                                     @NonNull List<Embed> embeds) {
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

    private String extractTextFromEmbed(@NonNull Embed embed) {
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

}
