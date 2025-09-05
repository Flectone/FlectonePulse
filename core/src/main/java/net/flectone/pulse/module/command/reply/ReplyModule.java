package net.flectone.pulse.module.command.reply;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

@Singleton
public class ReplyModule extends AbstractModuleCommand<Localization.Command.Reply> {

    private final Command.Reply command;
    private final Permission.Command.Reply permission;
    private final TellModule tellModule;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public ReplyModule(FileResolver fileResolver,
                       TellModule tellModule,
                       CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getReply(), Command::getReply, MessageType.COMMAND_REPLY);

        this.command = fileResolver.getCommand().getReply();
        this.permission = fileResolver.getPermission().getCommand().getReply();
        this.tellModule = tellModule;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptMessage = addPrompt(0, Localization.Command.Prompt::getMessage);
        registerCommand(manager -> manager
                .permission(permission.getName())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String receiverName = tellModule.getSenderReceiverMap().get(fPlayer.getUuid());
        if (receiverName == null) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Reply::getNullReceiver)
                    .build()
            );

            return;
        }

        String message = getArgument(commandContext, 0);

        tellModule.send(fPlayer, receiverName, message);
    }
}
