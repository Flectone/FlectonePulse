package net.flectone.pulse.module.command.coin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.coin.model.CoinMetadata;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CoinModule extends AbstractModuleCommand<Localization.Command.Coin> {

    private final FileFacade fileFacade;
    private final RandomUtil randomUtil;

    @Override
    public void onEnable() {
        super.onEnable();

        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        int percent = randomUtil.nextInt(config().draw() ? 0 : 1, 101);

        sendMessage(CoinMetadata.<Localization.Command.Coin>builder()
                .sender(fPlayer)
                .format(replaceResult(percent))
                .percent(percent)
                .range(config().range())
                .destination(config().destination())
                .sound(soundOrThrow())
                .proxy(output -> output.writeInt(percent))
                .integration(string -> Strings.CS.replace(
                        string,
                        "<result>",
                        percent == 0 ? "" : percent > 50 ? localization().head() : localization().tail()
                ))
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_COIN;
    }

    @Override
    public Command.Coin config() {
        return fileFacade.command().coin();
    }

    @Override
    public Permission.Command.Coin permission() {
        return fileFacade.permission().command().coin();
    }

    @Override
    public Localization.Command.Coin localization(FEntity sender) {
        return fileFacade.localization(sender).command().coin();
    }

    public Function<Localization.Command.Coin, String> replaceResult(int percent) {
        return message -> percent != 0
                ? Strings.CS.replace(message.format(), "<result>", percent > 50 ? message.head() : message.tail())
                : message.formatDraw();
    }
}