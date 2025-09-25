package net.flectone.pulse.module.command.broadcast;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

@Singleton
public class BroadcastModule extends AbstractModuleCommand<Localization.Command.Broadcast> {

    private final FileResolver fileResolver;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public BroadcastModule(FileResolver fileResolver,
                           CommandParserProvider commandParserProvider) {
        super(MessageType.COMMAND_BROADCAST);

        this.fileResolver = fileResolver;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createCooldown(config().getCooldown(), permission().getCooldownBypass());
        createSound(config().getSound(), permission().getSound());

        String promptMessage = addPrompt(0, Localization.Command.Prompt::getMessage);
        registerCommand(manager -> manager
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
                .format(Localization.Command.Broadcast::getFormat)
                .message(message)
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .proxy(dataOutputStream -> dataOutputStream.writeString(message))
                .integration()
                .build()
        );
    }

    @Override
    public Command.Broadcast config() {
        return fileResolver.getCommand().getBroadcast();
    }

    @Override
    public Permission.Command.Broadcast permission() {
        return fileResolver.getPermission().getCommand().getBroadcast();
    }

    @Override
    public Localization.Command.Broadcast localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getBroadcast();
    }
}
