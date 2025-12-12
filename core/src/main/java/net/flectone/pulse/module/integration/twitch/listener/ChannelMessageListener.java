package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.util.ChatReply;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.twitch.TwitchIntegration;
import net.flectone.pulse.module.integration.twitch.model.TwitchMetadata;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChannelMessageListener extends EventListener<ChannelMessageEvent> {

    private final FileResolver fileResolver;
    private final Provider<TwitchIntegration> twitchIntegration;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;

    public Class<ChannelMessageEvent> getEventType() {
        return ChannelMessageEvent.class;
    }

    public void execute(ChannelMessageEvent event) {
        if (executeCommand(event)) return;

        List<String> channel = config().getMessageChannel().get(MessageType.FROM_TWITCH_TO_MINECRAFT.name());
        if (channel == null || channel.isEmpty()) return;

        String channelName = event.getChannel().getName();
        if (!channel.contains(channelName)) return;

        String nickname = event.getUser().getName();
        String message = event.getMessage();

        Pair<String, String> reply = null;
        if (event.getReplyInfo() != null) {
            ChatReply chatReply = event.getReplyInfo();

            // remove @ping from message
            int firstSpaceIndex = message.indexOf(' ');
            if (firstSpaceIndex != -1) {
                message = message.substring(firstSpaceIndex).trim();
            }

            reply = Pair.of(chatReply.getThreadUserName(), chatReply.getMessageBody());
        }

        sendMessage(nickname, channelName, message, reply);
    }

    @Async
    public void sendMessage(String nickname, String channel, String message, Pair<String, String> reply) {
        sendMessage(TwitchMetadata.<Localization.Integration.Twitch>builder()
                .sender(FPlayer.UNKNOWN)
                .format(localization -> StringUtils.replaceEach(
                        StringUtils.defaultString(localization.getMessageChannel().get(MessageType.FROM_TWITCH_TO_MINECRAFT.name())),
                        new String[]{"<name>", "<channel>", "<reply_user>"},
                        new String[]{String.valueOf(nickname), String.valueOf(channel), reply == null ? "" : "@" + reply.first() + " "}
                ))
                .nickname(nickname)
                .channel(channel)
                .message(message)
                .range(Range.get(Range.Type.PROXY))
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{TagResolver.resolver("reply_message", (argumentQueue, context) ->
                        Tag.preProcessParsed(reply == null ? "" : StringUtils.defaultString(reply.second()))
                )})
                .integration(string -> StringUtils.replaceEach(
                        string,
                        new String[]{"<name>", "<channel>", "<reply_user>"},
                        new String[]{nickname, channel, reply == null ? "" : "@" + reply.first() + " "}
                ))
                .build()
        );
    }

    @Override
    public void onEnable() {}

    @Override
    public Integration.Twitch config() {
        return fileResolver.getIntegration().getTwitch();
    }

    @Override
    public Permission.Integration.Twitch permission() {
        return fileResolver.getPermission().getIntegration().getTwitch();
    }

    @Override
    public Localization.Integration.Twitch localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getIntegration().getTwitch();
    }

    private boolean executeCommand(ChannelMessageEvent event) {
        String text = event.getMessage();
        if (StringUtils.isEmpty(text)) return false;

        String[] parts = text.toLowerCase().trim().split(" ", 2);
        String commandName = parts[0];
        String arguments = parts.length > 1 ? parts[1] : "";

        for (Map.Entry<String, Integration.Command> commandEntry : config().getCustomCommand().entrySet()) {
            Integration.Command command = commandEntry.getValue();
            if (!command.getAliases().contains(commandName)) continue;

            FPlayer fPlayer = FPlayer.UNKNOWN;
            String channel = event.getChannel().getName();

            if (command.isNeedPlayer()) {
                if (arguments.isEmpty()) {
                    sendMessageToTwitch(channel, buildMessage(fPlayer, Localization.Integration.Twitch::getNullPlayer));
                    return true;
                }

                String playerName = arguments.split(" ")[0];
                fPlayer = fPlayerService.getFPlayer(playerName);
                if (fPlayer.isUnknown()) {
                    sendMessageToTwitch(channel, buildMessage(fPlayer, Localization.Integration.Twitch::getNullPlayer));
                    return true;
                }
            }

            String localizationString = localization().getCustomCommand().get(commandEntry.getKey());
            if (StringUtils.isEmpty(localizationString)) return true;

            String formattedMessage = buildMessage(fPlayer, localizationString);
            sendMessageToTwitch(channel, formattedMessage);
            return true;
        }

        return false;
    }

    private String buildMessage(FPlayer fPlayer, Function<Localization.Integration.Twitch, String> stringFunction) {
        return buildMessage(fPlayer, stringFunction.apply(localization()));
    }

    private String buildMessage(FPlayer fPlayer, String localization) {
        return messagePipeline.builder(fPlayer, localization)
                .flag(MessageFlag.OBJECT_PLAYER_HEAD, false)
                .flag(MessageFlag.OBJECT_SPRITE, false)
                .plainSerializerBuild();
    }

    public void sendMessageToTwitch(String channel, String message) {
        twitchIntegration.get().sendMessage(channel, message);
    }
}
