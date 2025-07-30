package net.flectone.pulse.module.command.ball;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.constant.DisableSource;
import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.util.RandomUtil;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.List;
import java.util.function.Function;

@Singleton
public class BallModule extends AbstractModuleCommand<Localization.Command.Ball> {

    @Getter private final Command.Ball command;
    private final Permission.Command.Ball permission;
    private final RandomUtil randomUtil;
    private final CommandRegistry commandRegistry;

    @Inject
    public BallModule(FileResolver fileResolver,
                      RandomUtil randomUtil,
                      CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getBall(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.BALL));

        this.command = fileResolver.getCommand().getBall();
        this.permission = fileResolver.getPermission().getCommand().getBall();
        this.randomUtil = randomUtil;
        this.commandRegistry = commandRegistry;
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

        String commandName = getName(command);
        String promptMessage = getPrompt().getMessage();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptMessage, commandRegistry.nativeMessageParser())
                        .handler(this)
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptMessage = getPrompt().getMessage();
        String message = commandContext.get(promptMessage);

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
