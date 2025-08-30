package net.flectone.pulse.module.command.try_;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.try_.model.TryMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;
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
        super(localization -> localization.getCommand().getTry(), Command::getTry, fPlayer -> fPlayer.isSetting(FPlayer.Setting.TRY), MessageType.COMMAND_TRY);

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
        if (isModuleDisabledFor(fPlayer)) return;

        int min = command.getMin();
        int max = command.getMax();
        int random = randomUtil.nextInt(min, max);

        String message = getArgument(commandContext, 0);

        sendMessage(TryMetadata.<Localization.Command.Try>builder()
                .sender(fPlayer)
                .format(replacePercent(random))
                .percent(random)
                .range(command.getRange())
                .destination(command.getDestination())
                .message(message)
                .sound(getModuleSound())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeInt(random);
                    dataOutputStream.writeString(message);
                })
                .integration(string -> Strings.CS.replace(string, "<percent>", String.valueOf(random)))
                .build()
        );
    }

    public Function<Localization.Command.Try, String> replacePercent(int value) {
        return message -> Strings.CS.replace(
                value >= command.getGood() ? message.getFormatTrue() : message.getFormatFalse(),
                "<percent>",
                String.valueOf(value)
        );
    }
}