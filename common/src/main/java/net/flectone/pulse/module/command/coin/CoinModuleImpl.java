package net.flectone.pulse.module.command.coin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.coin.listener.CoinProxyMessageListener;
import net.flectone.pulse.module.command.coin.model.CoinMessageContext;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.random.RandomGenerator;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CoinModuleImpl implements CoinModule {

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

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .range(config().range())
                .destination(config().destination())
                .sound(soundOrThrow())
                .messageContext(fResolver -> CoinMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(replaceResult(fResolver, percent))
                                .build()
                        )
                        .percent(percent)
                        .build()
                )
                .proxy(output -> output.writeInt(percent))
                .integration(() -> IntegrationMessageFormat.builder()
                        .format(string -> Strings.CS.replace(
                                string,
                                "<result>",
                                percent == 0 ? "" : percent > 50 ? localization(FPlayer.UNKNOWN).head() : localization(FPlayer.UNKNOWN).tail()
                        ))
                        .messageNames(List.of(name().name() + "_" + (percent == 0 ? "DRAW" : percent > 50 ? "HEAD" : "TAIL")))
                        .build()
                )
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

    @Override
    public String replaceResult(FPlayer fPlayer, int percent) {
        Localization.Command.Coin localization = localization(fPlayer);
        return percent != 0
                ? Strings.CS.replace(localization.format(), "<result>", percent > 50 ? localization.head() : localization.tail())
                : localization.formatDraw();
    }
}