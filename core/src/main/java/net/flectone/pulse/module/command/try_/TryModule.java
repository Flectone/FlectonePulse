package net.flectone.pulse.module.command.try_;

import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.RandomUtil;

import java.util.function.Function;

public abstract class TryModule extends AbstractModuleCommand<Localization.Command.Try> {

    @Getter
    private final Command.Try command;
    @Getter
    private final Permission.Command.Try permission;

    private final RandomUtil randomUtil;
    private final CommandUtil commandUtil;

    public TryModule(FileManager fileManager,
                     RandomUtil randomUtil,
                     CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getTry(), fPlayer -> fPlayer.is(FPlayer.Setting.TRY));

        this.randomUtil = randomUtil;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getTry();
        permission = fileManager.getPermission().getCommand().getTry();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        int min = command.getMin();
        int max = command.getMax();

        int random = randomUtil.nextInt(min, max);

        String message = commandUtil.getString(0, arguments);

        builder(fPlayer)
                .range(command.getRange())
                .tag(MessageTag.COMMAND_TRY)
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