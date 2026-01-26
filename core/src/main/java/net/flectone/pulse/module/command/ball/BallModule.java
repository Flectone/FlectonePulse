package net.flectone.pulse.module.command.ball;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.ball.model.BallMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BallModule extends AbstractModuleCommand<Localization.Command.Ball> {

    private final FileFacade fileFacade;
    private final RandomUtil randomUtil;
    private final CommandParserProvider commandParserProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptMessage = addPrompt(0, Localization.Command.Prompt::message);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        int answer = randomUtil.nextInt(0, localization().answers().size());
        String message = getArgument(commandContext, 0);

        sendMessage(BallMetadata.<Localization.Command.Ball>builder()
                .base(EventMetadata.<Localization.Command.Ball>builder()
                        .sender(fPlayer)
                        .format(replaceAnswer(answer))
                        .message(message)
                        .destination(config().destination())
                        .range(config().range())
                        .sound(soundOrThrow())
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeInt(answer);
                            dataOutputStream.writeString(message);
                        })
                        .integration(string -> {
                            List<String> answers = localization().answers();

                            String answerString = !answers.isEmpty()
                                    ? answers.get(Math.min(answer, answers.size() - 1))
                                    : StringUtils.EMPTY;

                            return Strings.CS.replace(string, "<answer>", answerString);
                        })
                        .build()
                )
                .answer(answer)
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_BALL;
    }

    @Override
    public Command.Ball config() {
        return fileFacade.command().ball();
    }

    @Override
    public Permission.Command.Ball permission() {
        return fileFacade.permission().command().ball();
    }

    @Override
    public Localization.Command.Ball localization(FEntity sender) {
        return fileFacade.localization(sender).command().ball();
    }

    public Function<Localization.Command.Ball, String> replaceAnswer(int answer) {
        return message -> {
            List<String> answers = message.answers();

            String answerString = !answers.isEmpty()
                    ? answers.get(Math.min(answer, answers.size() - 1))
                    : StringUtils.EMPTY;

            return Strings.CS.replace(message.format(), "<answer>", answerString);
        };
    }
}
