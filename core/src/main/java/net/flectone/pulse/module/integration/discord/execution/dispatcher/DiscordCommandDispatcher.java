package net.flectone.pulse.module.integration.discord.execution.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.discord.sender.DiscordSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordCommandDispatcher {

    private final DiscordModule discordModule;
    private final DiscordSender discordSender;
    private final TaskScheduler taskScheduler;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;

    public boolean executeCommand(@NonNull Message message) {
        String text = message.getContent();
        if (StringUtils.isEmpty(text)) return false;

        String[] parts = text.toLowerCase().trim().split(" ", 2);
        String commandName = parts[0];
        String arguments = parts.length > 1 ? parts[1] : "";

        Snowflake channel = message.getChannelId();

        for (Map.Entry<String, Integration.Command> commandEntry : discordModule.config().customCommand().entrySet()) {
            Integration.Command command = commandEntry.getValue();
            if (!command.aliases().contains(commandName)) continue;

            FPlayer fPlayer = getFPlayerArgument(command, arguments, channel);
            if (fPlayer == null) return true;

            Localization.Integration.Discord.ChannelEmbed channelEmbed = discordModule.localization().customCommand().get(commandEntry.getKey());
            if (channelEmbed == null) return true;

            taskScheduler.runAsync(() -> discordSender.sendMessage(fPlayer, channel, channelEmbed, string -> buildMessage(fPlayer, string)));
            return true;
        }

        return false;
    }

    @Nullable
    private FPlayer getFPlayerArgument(Integration.@NonNull Command command,
                                       @NonNull String arguments,
                                       @NonNull Snowflake channel) {
        FPlayer fPlayer = fPlayerService.getRandomFPlayer();
        if (Boolean.FALSE.equals(command.needPlayer())) return fPlayer;

        if (arguments.isEmpty()) {
            discordSender.sendMessage(channel, buildMessage(fPlayer, Localization.Integration.Discord::nullPlayer));
            return null;
        }

        String playerName = arguments.split(" ")[0];
        FPlayer argumentFPlayer = fPlayerService.getFPlayer(playerName);
        if (argumentFPlayer.isUnknown()) {
            discordSender.sendMessage(channel, buildMessage(fPlayer, Localization.Integration.Discord::nullPlayer));
            return null;
        }

        return argumentFPlayer;
    }

    private String buildMessage(@NonNull FPlayer fPlayer,
                                @NonNull Function<Localization.Integration.Discord, String> stringFunction) {
        return buildMessage(fPlayer, stringFunction.apply(discordModule.localization()));
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
