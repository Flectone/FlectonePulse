package net.flectone.pulse.module.command.coin;

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

import java.util.function.Function;

public abstract class CoinModule extends AbstractModuleCommand<Localization.Command.Coin> {

    @Getter private final Command.Coin command;
    @Getter private final Permission.Command.Coin permission;

    private final CommandUtil commandUtil;
    private final RandomUtil randomUtil;

    public CoinModule(FileManager fileManager,
                      CommandUtil commandUtil,
                      RandomUtil randomUtil) {
        super(localization -> localization.getCommand().getCoin(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.COIN));

        this.commandUtil = commandUtil;
        this.randomUtil = randomUtil;

        command = fileManager.getCommand().getCoin();
        permission = fileManager.getPermission().getCommand().getCoin();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        int percent = randomUtil.nextInt(command.isDraw() ? 0 : 1, 101);

        builder(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageTag.COMMAND_COIN)
                .format(replaceResult(percent))
                .proxy(output -> output.writeInt(percent))
                .integration(s -> s.replace("<result>", replaceResult(percent).apply(resolveLocalization())))
                .sound(getSound())
                .sendBuilt();
    }

    public Function<Localization.Command.Coin, String> replaceResult(int percent) {
        return message -> percent != 0
                ? message.getFormat().replace("<result>", percent > 50 ? message.getHead() : message.getTail())
                : message.getFormatDraw();
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