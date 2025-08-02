package net.flectone.pulse.module.command.coin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.RandomUtil;
import org.incendo.cloud.context.CommandContext;

import java.util.function.Function;

@Singleton
public class CoinModule extends AbstractModuleCommand<Localization.Command.Coin> {

    private final Command.Coin command;
    private final Permission.Command.Coin permission;
    private final RandomUtil randomUtil;

    @Inject
    public CoinModule(FileResolver fileResolver,
                      RandomUtil randomUtil) {
        super(localization -> localization.getCommand().getCoin(), Command::getCoin, fPlayer -> fPlayer.isSetting(FPlayer.Setting.COIN));

        this.command = fileResolver.getCommand().getCoin();
        this.permission = fileResolver.getPermission().getCommand().getCoin();
        this.randomUtil = randomUtil;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        int percent = randomUtil.nextInt(command.isDraw() ? 0 : 1, 101);

        builder(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageType.COMMAND_COIN)
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
}