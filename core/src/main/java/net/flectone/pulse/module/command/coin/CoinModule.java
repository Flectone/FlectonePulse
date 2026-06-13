package net.flectone.pulse.module.command.coin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.coin.listener.CoinProxyMessageListener;
import net.flectone.pulse.module.command.coin.model.CoinMetadata;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.generator.RandomGenerator;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CoinModule implements ModuleCommand<Localization.Command.Coin> {

    private final FileFacade fileFacade;
    private final RandomGenerator randomUtil;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ListenerRegistry listenerRegistry;
    private final ProxyRegistry proxyRegistry;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(CoinProxyMessageListener.class);
        }
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        int percent = randomUtil.nextInt(config().draw() ? 0 : 1, 101);

        messageDispatcher.dispatch(this, CoinMetadata.<Localization.Command.Coin>builder()
                .base(EventMetadata.<Localization.Command.Coin>builder()
                        .sender(fPlayer)
                        .format(replaceResult(percent))
                        .range(config().range())
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .proxy(output -> output.writeInt(percent))
                        .integration(IntegrationMetadata.builder()
                                .format(string -> Strings.CS.replace(
                                        string,
                                        "<result>",
                                        percent == 0 ? "" : percent > 50 ? localization().head() : localization().tail()
                                ))
                                .messageNames(List.of(name().name() + "_" + (percent == 0 ? "DRAW" : percent > 50 ? "HEAD" : "TAIL")))
                                .build()
                        )
                        .build()
                )
                .percent(percent)
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_COIN;
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
    public Localization.Command.Coin localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().coin();
    }

    public Function<Localization.Command.Coin, String> replaceResult(int percent) {
        return message -> percent != 0
                ? Strings.CS.replace(message.format(), "<result>", percent > 50 ? message.head() : message.tail())
                : message.formatDraw();
    }
}