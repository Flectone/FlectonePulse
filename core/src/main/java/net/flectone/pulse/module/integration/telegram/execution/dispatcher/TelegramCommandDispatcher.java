package net.flectone.pulse.module.integration.telegram.execution.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.telegram.sender.TelegramSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Map;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TelegramCommandDispatcher {

    private final FPlayerService fPlayerService;
    private final TelegramModule telegramModule;
    private final TelegramSender telegramSender;
    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;

    public boolean executeCommand(@NonNull Message message) {
        String text = message.getText();
        if (StringUtils.isEmpty(text)) return false;

        String[] parts = text.toLowerCase().trim().split(" ", 2);
        String commandName = parts[0];
        String arguments = parts.length > 1 ? parts[1] : "";

        for (Map.Entry<String, Integration.Command> commandEntry : telegramModule.config().customCommand().entrySet()) {
            Integration.Command command = commandEntry.getValue();
            if (!command.aliases().contains(commandName)) continue;

            FPlayer fPlayer = getFPlayerArgument(command, arguments, message);
            if (fPlayer == null) return true;

            String localizationString = telegramModule.localization().customCommand().get(commandEntry.getKey());
            if (StringUtils.isEmpty(localizationString)) return true;

            taskScheduler.runAsync(() -> telegramSender.sendMessage(message, buildMessage(fPlayer, localizationString)));
            return true;
        }

        return false;
    }

    @Nullable
    private FPlayer getFPlayerArgument(Integration.@NonNull Command command,
                                       @NonNull String arguments,
                                       @NonNull Message message) {
        FPlayer fPlayer = fPlayerService.getRandomFPlayer();
        if (Boolean.FALSE.equals(command.needPlayer())) return fPlayer;

        if (arguments.isEmpty()) {
            telegramSender.sendMessage(message, buildMessage(fPlayer, Localization.Integration.Telegram::nullPlayer));
            return null;
        }

        String playerName = arguments.split(" ")[0];
        FPlayer argumentFPlayer = fPlayerService.getFPlayer(playerName);
        if (argumentFPlayer.isUnknown()) {
            telegramSender.sendMessage(message, buildMessage(fPlayer, Localization.Integration.Telegram::nullPlayer));
            return null;
        }

        return argumentFPlayer;
    }

    private String buildMessage(@NonNull FPlayer fPlayer,
                                @NonNull Function<Localization.Integration.Telegram, String> stringFunction) {
        return buildMessage(fPlayer, stringFunction.apply(telegramModule.localization()));
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
