package net.flectone.pulse.module.command.broadcast;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

@Singleton
public class BroadcastModule extends AbstractModuleCommand<Localization.Command.Broadcast> {

    private final Command.Broadcast command;
    private final Permission.Command.Broadcast permission;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public BroadcastModule(FileResolver fileResolver,
                           CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getBroadcast(), Command::getBroadcast, fPlayer -> fPlayer.isSetting(FPlayer.Setting.BROADCAST), MessageType.COMMAND_BROADCAST);

        this.command = fileResolver.getCommand().getBroadcast();
        this.permission = fileResolver.getPermission().getCommand().getBroadcast();
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

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;

        String message = getArgument(commandContext, 0);

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(Localization.Command.Broadcast::getFormat)
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
