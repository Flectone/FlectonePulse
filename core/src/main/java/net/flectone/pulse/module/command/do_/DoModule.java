package net.flectone.pulse.module.command.do_;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

@Singleton
public class DoModule extends AbstractModuleCommand<Localization.Command.Do> {

    private final Command.Do command;
    private final Permission.Command.Do permission;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public DoModule(FileResolver fileResolver,
                    CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getDo(), Command::getDo, MessageType.COMMAND_DO);

        this.command = fileResolver.getCommand().getDo();
        this.permission = fileResolver.getPermission().getCommand().getDo();
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptMessage = addPrompt(0, Localization.Command.Prompt::getMessage);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String message = getArgument(commandContext, 0);

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(Localization.Command.Do::getFormat)
                .message(message)
                .range(command.getRange())
                .destination(command.getDestination())
                .sound(getModuleSound())
                .proxy(dataOutputStream -> dataOutputStream.writeString(message))
                .integration()
                .build()
        );
    }
}