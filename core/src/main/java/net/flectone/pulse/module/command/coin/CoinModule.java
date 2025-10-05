package net.flectone.pulse.module.command.coin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.coin.model.CoinMetadata;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.function.Function;

@Singleton
public class CoinModule extends AbstractModuleCommand<Localization.Command.Coin> {

    private final FileResolver fileResolver;
    private final RandomUtil randomUtil;

    @Inject
    public CoinModule(FileResolver fileResolver,
                      RandomUtil randomUtil) {
        super(MessageType.COMMAND_COIN);

        this.fileResolver = fileResolver;
        this.randomUtil = randomUtil;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        int percent = randomUtil.nextInt(config().isDraw() ? 0 : 1, 101);

        sendMessage(CoinMetadata.<Localization.Command.Coin>builder()
                .sender(fPlayer)
                .format(replaceResult(percent))
                .percent(percent)
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .proxy(output -> output.writeInt(percent))
                .integration(string -> Strings.CS.replace(
                        string,
                        "<result>",
                        replaceResult(percent).apply(localization())
                ))
                .build()
        );
    }

    @Override
    public Command.Coin config() {
        return fileResolver.getCommand().getCoin();
    }

    @Override
    public Permission.Command.Coin permission() {
        return fileResolver.getPermission().getCommand().getCoin();
    }

    @Override
    public Localization.Command.Coin localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getCoin();
    }

    public Function<Localization.Command.Coin, String> replaceResult(int percent) {
        return message -> percent != 0
                ? Strings.CS.replace(message.getFormat(), "<result>", percent > 50 ? message.getHead() : message.getTail())
                : message.getFormatDraw();
    }
}