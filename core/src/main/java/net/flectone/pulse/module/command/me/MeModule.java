package net.flectone.pulse.module.command.me;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.apache.commons.lang3.Strings;
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
        if (isModuleDisabledFor(fPlayer)) return;

        String message = getArgument(commandContext, 0);

        builder(fPlayer)
                .tag(MessageType.COMMAND_ME)
                .destination(command.getDestination())
                .range(command.getRange())
                .format(Localization.Command.Me::getFormat)
                .message(message)
                .proxy(output -> output.writeUTF(message))
                .integration(s -> Strings.CS.replace(s, "<message>", message))
                .sound(getSound())
                .sendBuilt();
    }
}
