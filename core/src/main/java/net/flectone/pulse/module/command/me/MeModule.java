package net.flectone.pulse.module.command.me;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.DisableSource;
import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.provider.CommandParserProvider;
import net.flectone.pulse.resolver.FileResolver;
import org.incendo.cloud.context.CommandContext;

@Singleton
public class MeModule extends AbstractModuleCommand<Localization.Command.Me> {

    private final Command.Me command;
    private final Permission.Command.Me permission;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public MeModule(FileResolver fileResolver,
                    CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getMe(), Command::getMe, fPlayer -> fPlayer.isSetting(FPlayer.Setting.ME));

        this.command = fileResolver.getCommand().getMe();
        this.permission = fileResolver.getPermission().getCommand().getMe();
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

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String message = getArgument(commandContext, 0);

        builder(fPlayer)
                .tag(MessageType.COMMAND_ME)
                .destination(command.getDestination())
                .range(command.getRange())
                .format(Localization.Command.Me::getFormat)
                .message(message)
                .proxy(output -> output.writeUTF(message))
                .integration(s -> s.replace("<message>", message))
                .sound(getSound())
                .sendBuilt();
    }
}
