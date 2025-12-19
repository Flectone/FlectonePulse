package net.flectone.pulse.module.integration.discord.listener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.discord.DiscordIntegration;
import net.flectone.pulse.module.integration.discord.model.DiscordMetadata;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageCreateListener extends EventListener<MessageCreateEvent> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;
    private final Provider<DiscordIntegration> discordIntegration;

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<MessageCreateEvent> execute(MessageCreateEvent event) {
        Message discordMessage = event.getMessage();

        List<String> channel = config().messageChannel().get(MessageType.FROM_DISCORD_TO_MINECRAFT.name());
        if (channel == null) return Mono.empty();
        if (!channel.contains(discordMessage.getChannelId().asString())) return Mono.empty();

        Optional<Member> user = event.getMember();
        if (user.isEmpty() || user.get().isBot()) return Mono.empty();

        if (executeCommand(discordMessage)) return Mono.empty();

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
                    Localization.Integration.Discord.ChannelEmbed channelEmbed = localization.messageChannel().get(MessageType.FROM_DISCORD_TO_MINECRAFT.name());
                    if (channelEmbed == null) return "";

                    return StringUtils.replaceEach(
                            channelEmbed.content(),
                            new String[]{"<name>", "<global_name>", "<nickname>", "<display_name>", "<user_name>"},
                            new String[]{globalName, globalName, nickname, displayName, userName}
                    );
                })
                .globalName(globalName)
                .nickname(nickname)
                .displayName(displayName)
                .userName(userName)
                .range(Range.get(Range.Type.PROXY))
                .destination(config().destination())
                .message(message)
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{TagResolver.resolver("reply", (argumentQueue, context) -> {
                    if (reply == null) return Tag.selfClosingInserting(Component.empty());

                    Component componentReply = messagePipeline.builder(localization().formatReply())
                            .tagResolvers(
                                    TagResolver.resolver("reply_user", Tag.preProcessParsed(StringUtils.defaultString(reply.first()))),
                                    TagResolver.resolver("reply_message", Tag.preProcessParsed(StringUtils.defaultString(reply.second())))
                            )
                            .build();

                    return Tag.inserting(componentReply);
                })})
                .integration(string -> StringUtils.replaceEach(
                        string,
                        new String[]{"<name>", "<global_name>", "<nickname>", "<display_name>", "<user_name>"},
                        new String[]{globalName, globalName, nickname, displayName, userName}
                ))
                .build()
        );
    }

    @Override
    public void onEnable() {}

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

    private boolean executeCommand(Message message) {
        String text = message.getContent();
        if (StringUtils.isEmpty(text)) return false;

        String[] parts = text.toLowerCase().trim().split(" ", 2);
        String commandName = parts[0];
        String arguments = parts.length > 1 ? parts[1] : "";

        for (Map.Entry<String, Integration.Command> commandEntry : config().customCommand().entrySet()) {
            Integration.Command command = commandEntry.getValue();
            if (!command.aliases().contains(commandName)) continue;

            FPlayer fPlayer = FPlayer.UNKNOWN;
            Snowflake channel = message.getChannelId();

            if (command.needPlayer()) {
                if (arguments.isEmpty()) {
                    sendMessageToDiscord(channel, buildMessage(fPlayer, Localization.Integration.Discord::nullPlayer));
                    return true;
                }

                String playerName = arguments.split(" ")[0];
                fPlayer = fPlayerService.getFPlayer(playerName);
                if (fPlayer.isUnknown()) {
                    sendMessageToDiscord(channel, buildMessage(fPlayer, Localization.Integration.Discord::nullPlayer));
                    return true;
                }
            }

            Localization.Integration.Discord.ChannelEmbed channelEmbed = localization().customCommand().get(commandEntry.getKey());
            if (channelEmbed == null) return true;

            sendMessageToDiscord(fPlayer, channel, channelEmbed);
            return true;
        }

        return false;
    }

    private String buildMessage(FPlayer fPlayer, Function<Localization.Integration.Discord, String> stringFunction) {
        return buildMessage(fPlayer, stringFunction.apply(localization()));
    }

    private String buildMessage(FPlayer fPlayer, String localization) {
        return messagePipeline.builder(fPlayer, localization)
                .flag(MessageFlag.OBJECT_PLAYER_HEAD, false)
                .flag(MessageFlag.OBJECT_SPRITE, false)
                .plainSerializerBuild();
    }

    private void sendMessageToDiscord(Snowflake channel, String text) {
        discordIntegration.get().sendMessage(channel, text);
    }

    private void sendMessageToDiscord(FPlayer fPlayer, Snowflake channel, Localization.Integration.Discord.ChannelEmbed channelEmbed) {
        discordIntegration.get().sendMessage(fPlayer, channel, channelEmbed, string -> buildMessage(fPlayer, string));
    }
}
