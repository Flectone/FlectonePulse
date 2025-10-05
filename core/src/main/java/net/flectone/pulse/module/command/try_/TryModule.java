package net.flectone.pulse.module.command.try_;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.try_.model.TryMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.function.Function;

@Singleton
public class TryModule extends AbstractModuleCommand<Localization.Command.Try> {

    private final FileResolver fileResolver;
    private final RandomUtil randomUtil;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public TryModule(FileResolver fileResolver,
                     RandomUtil randomUtil,
                     CommandParserProvider commandParserProvider) {
        super(MessageType.COMMAND_TRY);

        this.fileResolver = fileResolver;
        this.randomUtil = randomUtil;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        String promptMessage = addPrompt(0, Localization.Command.Prompt::getMessage);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
                .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        int min = config().getMin();
        int max = config().getMax();
        int random = randomUtil.nextInt(min, max);

        String message = getArgument(commandContext, 0);

        sendMessage(TryMetadata.<Localization.Command.Try>builder()
                .sender(fPlayer)
                .format(replacePercent(random))
                .percent(random)
                .range(config().getRange())
                .destination(config().getDestination())
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

    @Override
    public Command.Try config() {
        return fileResolver.getCommand().getTry();
    }

    @Override
    public Permission.Command.Try permission() {
        return fileResolver.getPermission().getCommand().getTry();
    }

    @Override
    public Localization.Command.Try localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getTry();
    }

    public Function<Localization.Command.Try, String> replacePercent(int value) {
        return message -> Strings.CS.replace(
                value >= config().getGood() ? message.getFormatTrue() : message.getFormatFalse(),
                "<percent>",
                String.valueOf(value)
        );
    }
}