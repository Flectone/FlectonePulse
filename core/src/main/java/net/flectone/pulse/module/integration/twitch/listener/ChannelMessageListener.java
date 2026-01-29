package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.util.ChatReply;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.twitch.TwitchIntegration;
import net.flectone.pulse.module.integration.twitch.model.TwitchMetadata;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChannelMessageListener extends EventListener<ChannelMessageEvent> {

    private final FileFacade fileFacade;
    private final Provider<TwitchIntegration> twitchIntegration;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;
    private final TaskScheduler taskScheduler;

    public Class<ChannelMessageEvent> getEventType() {
        return ChannelMessageEvent.class;
    }

    public void execute(ChannelMessageEvent event) {
        if (executeCommand(event)) return;

        List<String> channel = config().messageChannel().get(MessageType.FROM_TWITCH_TO_MINECRAFT.name());
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

    public void sendMessage(String nickname, String channel, String message, Pair<String, String> reply) {
        sendMessage(TwitchMetadata.<Localization.Integration.Twitch>builder()
                .base(EventMetadata.<Localization.Integration.Twitch>builder()
                        .sender(twitchIntegration.get().getSender())
                        .format(localization -> StringUtils.replaceEach(
                                StringUtils.defaultString(localization.messageChannel().get(MessageType.FROM_TWITCH_TO_MINECRAFT.name())),
                                new String[]{"<name>", "<channel>"},
                                new String[]{String.valueOf(nickname), String.valueOf(channel)}
                        ))
                        .message(message)
                        .range(Range.get(Range.Type.PROXY))
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{TagResolver.resolver("reply", (argumentQueue, context) -> {
                            if (reply == null) return Tag.selfClosingInserting(Component.empty());

                            MessageContext tagContext = messagePipeline.createContext(localization().formatReply())
                                    .addTagResolvers(
                                            TagResolver.resolver("reply_user", Tag.preProcessParsed(StringUtils.defaultString(reply.first()))),
                                            TagResolver.resolver("reply_message", Tag.preProcessParsed(StringUtils.defaultString(reply.second())))
                                    );

                            return Tag.inserting(messagePipeline.build(tagContext));
                        })})
                        .integration(string -> StringUtils.replaceEach(
                                string,
                                new String[]{"<name>", "<channel>"},
                                new String[]{nickname, channel}
                        ))
                        .build()
                )
                .nickname(nickname)
                .channel(channel)
                .build()
        );
    }

    @Override
    public void onEnable() {}

    @Override
    public Integration.Twitch config() {
        return fileFacade.integration().twitch();
    }

    @Override
    public Permission.Integration.Twitch permission() {
        return fileFacade.permission().integration().twitch();
    }

    @Override
    public Localization.Integration.Twitch localization(FEntity sender) {
        return fileFacade.localization(sender).integration().twitch();
    }

    private boolean executeCommand(ChannelMessageEvent event) {
        String text = event.getMessage();
        if (StringUtils.isEmpty(text)) return false;

        String[] parts = text.toLowerCase().trim().split(" ", 2);
        String commandName = parts[0];
        String arguments = parts.length > 1 ? parts[1] : "";

        String channel = event.getChannel().getName();

        for (Map.Entry<String, Integration.Command> commandEntry : config().customCommand().entrySet()) {
            Integration.Command command = commandEntry.getValue();
            if (!command.aliases().contains(commandName)) continue;

            FPlayer fPlayer = getFPlayerArgument(command, arguments, channel);
            if (fPlayer == null) return true;

            String localizationString = localization().customCommand().get(commandEntry.getKey());
            if (StringUtils.isEmpty(localizationString)) return true;

            taskScheduler.runRegion(fPlayer, () -> sendMessageToTwitch(channel, buildMessage(fPlayer, localizationString)));
            return true;
        }

        return false;
    }

    @Nullable
    private FPlayer getFPlayerArgument(Integration.Command command, String arguments, String channel) {
        FPlayer fPlayer = fPlayerService.getRandomFPlayer();
        if (Boolean.FALSE.equals(command.needPlayer())) return fPlayer;

        if (arguments.isEmpty()) {
            sendMessageToTwitch(channel, buildMessage(fPlayer, Localization.Integration.Twitch::nullPlayer));
            return null;
        }

        String playerName = arguments.split(" ")[0];
        FPlayer argumentFPlayer = fPlayerService.getFPlayer(playerName);
        if (argumentFPlayer.isUnknown()) {
            sendMessageToTwitch(channel, buildMessage(fPlayer, Localization.Integration.Twitch::nullPlayer));
            return null;
        }

        return argumentFPlayer;
    }

    private String buildMessage(FPlayer fPlayer, Function<Localization.Integration.Twitch, String> stringFunction) {
        return buildMessage(fPlayer, stringFunction.apply(localization()));
    }

    private String buildMessage(FPlayer fPlayer, String localization) {
        MessageContext messageContext = messagePipeline.createContext(fPlayer, localization)
                .addFlags(
                        new MessageFlag[]{MessageFlag.OBJECT_PLAYER_HEAD, MessageFlag.OBJECT_SPRITE},
                        new boolean[]{false, false}
                );

        return messagePipeline.buildPlain(messageContext);
    }

    public void sendMessageToTwitch(String channel, String message) {
        twitchIntegration.get().sendMessage(channel, message);
    }
}
