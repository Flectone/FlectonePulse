package net.flectone.pulse.module.command.ball;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.ball.model.BallMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.function.Function;

@Singleton
public class BallModule extends AbstractModuleCommand<Localization.Command.Ball> {

    private final FileResolver fileResolver;
    private final RandomUtil randomUtil;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public BallModule(FileResolver fileResolver,
                      RandomUtil randomUtil,
                      CommandParserProvider commandParserProvider) {
        super(MessageType.COMMAND_BALL);

        this.fileResolver = fileResolver;
        this.randomUtil = randomUtil;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createCooldown(config().getCooldown(), permission().getCooldownBypass());
        createSound(config().getSound(), permission().getSound());

        String promptMessage = addPrompt(0, Localization.Command.Prompt::getMessage);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        int answer = randomUtil.nextInt(0, localization().getAnswers().size());
        String message = getArgument(commandContext, 0);

        sendMessage(BallMetadata.<Localization.Command.Ball>builder()
                .sender(fPlayer)
                .format(replaceAnswer(answer))
                .answer(answer)
                .message(message)
                .destination(config().getDestination())
                .range(config().getRange())
                .sound(getModuleSound())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeInt(answer);
                    dataOutputStream.writeString(message);
                })
                .integration(string -> {
                    List<String> answers = localization().getAnswers();

                    String answerString = !answers.isEmpty()
                            ? answers.get(Math.min(answer, answers.size() - 1))
                            : StringUtils.EMPTY;

                    return Strings.CS.replace(string, "<answer>", answerString);
                })
                .build()
        );
    }

    @Override
    public Command.Ball config() {
        return fileResolver.getCommand().getBall();
    }

    @Override
    public Permission.Command.Ball permission() {
        return fileResolver.getPermission().getCommand().getBall();
    }

    @Override
    public Localization.Command.Ball localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getBall();
    }

    public Function<Localization.Command.Ball, String> replaceAnswer(int answer) {
        return message -> {
            List<String> answers = message.getAnswers();

            String answerString = !answers.isEmpty()
                    ? answers.get(Math.min(answer, answers.size() - 1))
                    : StringUtils.EMPTY;

            return Strings.CS.replace(message.getFormat(), "<answer>", answerString);
        };
    }
}
