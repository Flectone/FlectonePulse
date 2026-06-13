package net.flectone.pulse.module.integration.twitch.execution.dispatcher;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.twitch.sender.TwitchSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TwitchCommandDispatcher {

    private final TwitchModule twitchModule;
    private final TwitchSender twitchSender;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;
    private final TaskScheduler taskScheduler;

    public boolean executeCommand(@NonNull ChannelMessageEvent event) {
        String text = event.getMessage();
        if (StringUtils.isEmpty(text)) return false;

        String[] parts = text.toLowerCase().trim().split(" ", 2);
        String commandName = parts[0];
        String arguments = parts.length > 1 ? parts[1] : "";

        String channel = event.getChannel().getName();

        for (Map.Entry<String, Integration.Command> commandEntry : twitchModule.config().customCommand().entrySet()) {
            Integration.Command command = commandEntry.getValue();
            if (!command.aliases().contains(commandName)) continue;

            FPlayer fPlayer = getFPlayerArgument(command, arguments, channel);
            if (fPlayer == null) return true;

            String localizationString = twitchModule.localization().customCommand().get(commandEntry.getKey());
            if (StringUtils.isEmpty(localizationString)) return true;

            taskScheduler.runAsync(() -> twitchSender.sendMessage(channel, buildMessage(fPlayer, localizationString)));
            return true;
        }

        return false;
    }

    @Nullable
    private FPlayer getFPlayerArgument(Integration.@NonNull Command command,
                                       @NonNull String arguments,
                                       @NonNull String channel) {
        FPlayer fPlayer = fPlayerService.getRandomFPlayer();
        if (Boolean.FALSE.equals(command.needPlayer())) return fPlayer;

        if (arguments.isEmpty()) {
            twitchSender.sendMessage(channel, buildMessage(fPlayer, Localization.Integration.Twitch::nullPlayer));
            return null;
        }

        String playerName = arguments.split(" ")[0];
        FPlayer argumentFPlayer = fPlayerService.getFPlayer(playerName);
        if (argumentFPlayer.isUnknown()) {
            twitchSender.sendMessage(channel, buildMessage(fPlayer, Localization.Integration.Twitch::nullPlayer));
            return null;
        }

        return argumentFPlayer;
    }

    private String buildMessage(@NonNull FPlayer fPlayer,
                                @NonNull Function<Localization.Integration.Twitch, String> stringFunction) {
        return buildMessage(fPlayer, stringFunction.apply(twitchModule.localization()));
    }

    private String buildMessage(@NonNull FPlayer fPlayer,
                                @NonNull String localization) {
        return messagePipeline.buildPlain(MessageContext.builder()
                .sender(fPlayer)
                .message(localization)
                .flags(
                        new MessageFlag[]{MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING, MessageFlag.OBJECT_SPRITE_PROCESSING},
                        new boolean[]{false, false}
                )
                .build()
        );
    }

}
