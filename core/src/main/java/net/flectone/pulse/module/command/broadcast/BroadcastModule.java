package net.flectone.pulse.module.command.broadcast;

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
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BroadcastModule extends AbstractModuleCommand<Localization.Command.Broadcast> {

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptMessage = addPrompt(0, Localization.Command.Prompt::message);
        registerCommand(manager -> manager
                        .permission(permission().name())
                        .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String message = getArgument(commandContext, 0);

        sendMessage(EventMetadata.<Localization.Command.Broadcast>builder()
                .sender(fPlayer)
                .format(Localization.Command.Broadcast::format)
                .message(message)
                .range(config().range())
                .destination(config().destination())
                .sound(soundOrThrow())
                .proxy(dataOutputStream -> dataOutputStream.writeString(message))
                .integration()
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_BROADCAST;
    }

    @Override
    public Command.Broadcast config() {
        return fileFacade.command().broadcast();
    }

    @Override
    public Permission.Command.Broadcast permission() {
        return fileFacade.permission().command().broadcast();
    }

    @Override
    public Localization.Command.Broadcast localization(FEntity sender) {
        return fileFacade.localization(sender).command().broadcast();
    }
}
