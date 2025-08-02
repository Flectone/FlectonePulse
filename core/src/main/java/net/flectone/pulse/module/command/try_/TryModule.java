package net.flectone.pulse.module.command.try_;

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
import net.flectone.pulse.util.RandomUtil;
import org.incendo.cloud.context.CommandContext;

import java.util.function.Function;

@Singleton
public class TryModule extends AbstractModuleCommand<Localization.Command.Try> {

    private final Command.Try command;
    private final Permission.Command.Try permission;
    private final RandomUtil randomUtil;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public TryModule(FileResolver fileResolver,
                     RandomUtil randomUtil,
                     CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getTry(), Command::getTry, fPlayer -> fPlayer.isSetting(FPlayer.Setting.TRY));

        this.command = fileResolver.getCommand().getTry();
        this.permission = fileResolver.getPermission().getCommand().getTry();
        this.randomUtil = randomUtil;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
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
                .handler(this)
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        int min = command.getMin();
        int max = command.getMax();
        int random = randomUtil.nextInt(min, max);

        String message = getArgument(commandContext, 0);

        builder(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageType.COMMAND_TRY)
                .format(replacePercent(random))
                .message((fResolver, s)  -> message)
                .proxy(output -> {
                    output.writeInt(random);
                    output.writeUTF(message);
                })
                .integration(s -> s
                        .replace("<message>", message)
                        .replace("<percent>", String.valueOf(random))
                )
                .sound(getSound())
                .sendBuilt();
    }

    public Function<Localization.Command.Try, String> replacePercent(int value) {
        return message -> (value >= command.getGood() ? message.getFormatTrue() : message.getFormatFalse())
                .replace("<percent>", String.valueOf(value));
    }
}