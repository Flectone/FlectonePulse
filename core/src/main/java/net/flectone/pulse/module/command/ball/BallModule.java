package net.flectone.pulse.module.command.ball;

import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.RandomUtil;

import java.util.List;
import java.util.function.Function;

public abstract class BallModule extends AbstractModuleCommand<Localization.Command.Ball> {

    @Getter private final Command.Ball command;
    @Getter private final Permission.Command.Ball permission;

    private final RandomUtil randomUtil;
    private final CommandUtil commandUtil;

    public BallModule(FileManager fileManager,
                      RandomUtil randomUtil,
                      CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getBall(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.BALL));

        this.randomUtil = randomUtil;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getBall();
        permission = fileManager.getPermission().getCommand().getBall();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        int answer = randomUtil.nextInt(0, resolveLocalization().getAnswers().size());

        String string = commandUtil.getString(0, arguments);

        builder(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageTag.COMMAND_BALL)
                .format(replaceAnswer(answer))
                .message(string)
                .proxy(output -> {
                    output.writeInt(answer);
                    output.writeUTF(string);
                })
                .integration(s -> {
                    List<String> answers = resolveLocalization().getAnswers();

                    return s.replace("<message>", string)
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

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
