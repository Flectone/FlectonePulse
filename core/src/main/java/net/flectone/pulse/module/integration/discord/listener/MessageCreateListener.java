package net.flectone.pulse.module.integration.discord.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.discord.model.DiscordMetadata;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageCreateListener extends EventListener<MessageCreateEvent> {

    private final FileResolver fileResolver;

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<MessageCreateEvent> execute(MessageCreateEvent event) {
        Message discordMessage = event.getMessage();

        List<String> channel = config().getMessageChannel().get(MessageType.FROM_DISCORD_TO_MINECRAFT.name());
        if (channel == null) return Mono.empty();
        if (!channel.contains(discordMessage.getChannelId().asString())) return Mono.empty();

        Optional<Member> user = event.getMember();
        if (user.isEmpty() || user.get().isBot()) return Mono.empty();

        String message = getMessageContent(discordMessage);
        if (message == null) return Mono.empty();

        Pair<String, String> reply = null;
        if (discordMessage.getReferencedMessage().isPresent()) {
            Message referencedMessage = discordMessage.getReferencedMessage().get();
            if (referencedMessage.getAuthor().isPresent()) {
                reply = Pair.of(referencedMessage.getAuthor().get().getUsername(), getMessageContent(referencedMessage));
            }
        }

        sendMessage(user.get(), message, reply);

        return Mono.empty();
    }

    @Async
    public void sendMessage(Member member, String message, Pair<String, String> reply) {
        String globalName = member.getGlobalName().orElse("");
        String nickname = member.getNickname().orElse("");
        String displayName = member.getDisplayName();
        String userName = member.getUsername();

        sendMessage(DiscordMetadata.<Localization.Integration.Discord>builder()
                .sender(FPlayer.UNKNOWN)
                .format(localization -> {
                    Localization.Integration.Discord.ChannelEmbed channelEmbed = localization.getMessageChannel().get(MessageType.FROM_DISCORD_TO_MINECRAFT.name());
                    if (channelEmbed == null) return "";

                    return StringUtils.replaceEach(
                            channelEmbed.getContent(),
                            new String[]{"<name>", "<global_name>", "<nickname>", "<display_name>", "<user_name>", "<reply_user>"},
                            new String[]{globalName, globalName, nickname, displayName, userName, reply == null ? "" : "@" + reply.first() + " "}
                    );
                })
                .globalName(globalName)
                .nickname(nickname)
                .displayName(displayName)
                .userName(userName)
                .range(Range.get(Range.Type.PROXY))
                .destination(config().getDestination())
                .message(message)
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{TagResolver.resolver("reply_message", (argumentQueue, context) ->
                        Tag.preProcessParsed(reply == null ? "" : StringUtils.defaultString(reply.second()))
                )})
                .integration(string -> StringUtils.replaceEach(
                        string,
                        new String[]{"<name>", "<global_name>", "<nickname>", "<display_name>", "<user_name>", "<reply_user>"},
                        new String[]{globalName, globalName, nickname, displayName, userName, reply == null ? "" : "@" + reply.first() + " "}
                ))
                .build()
        );
    }

    @Override
    public void onEnable() {}

    @Override
    public Integration.Discord config() {
        return fileResolver.getIntegration().getDiscord();
    }

    @Override
    public Permission.Integration.Discord permission() {
        return fileResolver.getPermission().getIntegration().getDiscord();
    }

    @Override
    public Localization.Integration.Discord localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getIntegration().getDiscord();
    }

    private String getMessageContent(Message message) {
        String content = message.getContent();
        if (content.isEmpty()) {
            if (message.getAttachments().isEmpty()) return null;

            return String.join(" ", message.getAttachments()
                    .stream()
                    .map(Attachment::getUrl)
                    .toList()
            );
        }

        return content;
    }
}
