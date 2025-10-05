package net.flectone.pulse.module.command.anon;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

@Singleton
public class AnonModule extends AbstractModuleCommand<Localization.Command.Anon> {

    private final FileResolver fileResolver;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public AnonModule(FileResolver fileResolver,
                      CommandParserProvider commandParserProvider) {
        super(MessageType.COMMAND_ANON);

        this.fileResolver = fileResolver;
        this.commandParserProvider = commandParserProvider;
    }

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
                .format(Localization.Command.Anon::getFormat)
                .message(message)
                .destination(config().getDestination())
                .range(config().getRange())
                .sound(getModuleSound())
                .proxy(dataOutputStream -> dataOutputStream.writeString(message))
                .integration()
                .build()
        );
    }

    @Override
    public Command.Anon config() {
        return fileResolver.getCommand().getAnon();
    }

    @Override
    public Permission.Command.Anon permission() {
        return fileResolver.getPermission().getCommand().getAnon();
    }

    @Override
    public Localization.Command.Anon localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getAnon();
    }
}

