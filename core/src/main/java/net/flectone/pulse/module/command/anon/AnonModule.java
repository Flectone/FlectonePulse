package net.flectone.pulse.module.command.anon;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AnonModule extends AbstractModuleCommand<Localization.Command.Anon> {

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptMessage = addPrompt(0, Localization.Command.Prompt::message);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String message = getArgument(commandContext, 0);

        sendMessage(EventMetadata.<Localization.Command.Anon>builder()
                .sender(fPlayer)
                .format(Localization.Command.Anon::format)
                .message(message)
                .destination(config().destination())
                .range(config().range())
                .sound(soundOrThrow())
                .proxy(dataOutputStream -> dataOutputStream.writeString(message))
                .integration()
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_ANON;
    }

    @Override
    public Command.Anon config() {
        return fileFacade.command().anon();
    }

    @Override
    public Permission.Command.Anon permission() {
        return fileFacade.permission().command().anon();
    }

    @Override
    public Localization.Command.Anon localization(FEntity sender) {
        return fileFacade.localization(sender).command().anon();
    }
}

