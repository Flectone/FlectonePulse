package net.flectone.pulse.module.command.coin;

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
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.util.RandomUtil;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.function.Function;

@Singleton
public class CoinModule extends AbstractModuleCommand<Localization.Command.Coin> {

    @Getter private final Command.Coin command;
    private final Permission.Command.Coin permission;
    private final CommandRegistry commandRegistry;
    private final RandomUtil randomUtil;

    @Inject
    public CoinModule(FileResolver fileResolver,
                      CommandRegistry commandRegistry,
                      RandomUtil randomUtil) {
        super(localization -> localization.getCommand().getCoin(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.COIN));

        this.command = fileResolver.getCommand().getCoin();
        this.permission = fileResolver.getPermission().getCommand().getCoin();
        this.commandRegistry = commandRegistry;
        this.randomUtil = randomUtil;
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
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .handler(this)
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
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