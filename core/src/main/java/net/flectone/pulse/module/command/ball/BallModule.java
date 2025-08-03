package net.flectone.pulse.module.command.ball;

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

import java.util.List;
import java.util.function.Function;

@Singleton
public class BallModule extends AbstractModuleCommand<Localization.Command.Ball> {

    private final Command.Ball command;
    private final Permission.Command.Ball permission;
    private final RandomUtil randomUtil;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public BallModule(FileResolver fileResolver,
                      RandomUtil randomUtil,
                      CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getBall(), Command::getBall, fPlayer -> fPlayer.isSetting(FPlayer.Setting.BALL));

        this.command = fileResolver.getCommand().getBall();
        this.permission = fileResolver.getPermission().getCommand().getBall();
        this.randomUtil = randomUtil;
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

        int answer = randomUtil.nextInt(0, resolveLocalization().getAnswers().size());

        builder(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageType.COMMAND_BALL)
                .format(replaceAnswer(answer))
                .message(message)
                .proxy(output -> {
                    output.writeInt(answer);
                    output.writeUTF(message);
                })
                .integration(s -> {
                    List<String> answers = resolveLocalization().getAnswers();

                    return s.replace("<message>", message)
                            .replace("<answer>", answers.size() >= answer
                                    ? answers.get(answer)
                                    : answers.get(answers.size() - 1));
                })
                .sound(getSound())
                .sendBuilt();
    }

    public Function<Localization.Command.Ball, String> replaceAnswer(int answer) {
        return message -> {
            List<String> answers = message.getAnswers();

            String string = answers.size() >= answer
                    ? answers.get(answer)
                    : answers.get(answers.size() - 1);

            return message.getFormat().replace("<answer>", string);
        };
    }
}
