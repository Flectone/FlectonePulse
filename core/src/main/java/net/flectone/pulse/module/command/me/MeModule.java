package net.flectone.pulse.module.command.me;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MeModule extends AbstractModuleCommand<Localization.Command.Me> {

    private final FileResolver fileResolver;
    private final CommandParserProvider commandParserProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptMessage = addPrompt(0, Localization.Command.Prompt::getMessage);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String message = getArgument(commandContext, 0);

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(Localization.Command.Me::getFormat)
                .destination(config().getDestination())
                .range(config().getRange())
                .message(message)
                .sound(getModuleSound())
                .proxy(dataOutputStream -> dataOutputStream.writeString(message))
                .integration()
                .build()
        );

    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_ME;
    }

    @Override
    public Command.Me config() {
        return fileResolver.getCommand().getMe();
    }

    @Override
    public Permission.Command.Me permission() {
        return fileResolver.getPermission().getCommand().getMe();
    }

    @Override
    public Localization.Command.Me localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getMe();
    }
}
