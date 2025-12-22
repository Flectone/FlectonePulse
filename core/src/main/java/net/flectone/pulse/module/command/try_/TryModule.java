package net.flectone.pulse.module.command.try_;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.try_.model.TryMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TryModule extends AbstractModuleCommand<Localization.Command.CommandTry> {

    private final FileFacade fileFacade;
    private final RandomUtil randomUtil;
    private final CommandParserProvider commandParserProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptMessage = addPrompt(0, Localization.Command.Prompt::message);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
                .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        int min = config().min();
        int max = config().max();
        int random = randomUtil.nextInt(min, max);

        String message = getArgument(commandContext, 0);

        sendMessage(TryMetadata.<Localization.Command.CommandTry>builder()
                .sender(fPlayer)
                .format(replacePercent(random))
                .percent(random)
                .range(config().range())
                .destination(config().destination())
                .message(message)
                .sound(soundOrThrow())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeInt(random);
                    dataOutputStream.writeString(message);
                })
                .integration(string -> Strings.CS.replace(string, "<percent>", String.valueOf(random)))
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_TRY;
    }

    @Override
    public Command.CommandTry config() {
        return fileFacade.command().commandTry();
    }

    @Override
    public Permission.Command.CommandTry permission() {
        return fileFacade.permission().command().commandTry();
    }

    @Override
    public Localization.Command.CommandTry localization(FEntity sender) {
        return fileFacade.localization(sender).command().commandTry();
    }

    public Function<Localization.Command.CommandTry, String> replacePercent(int value) {
        return message -> Strings.CS.replace(
                value >= config().good() ? message.formatTrue() : message.formatFalse(),
                "<percent>",
                String.valueOf(value)
        );
    }
}