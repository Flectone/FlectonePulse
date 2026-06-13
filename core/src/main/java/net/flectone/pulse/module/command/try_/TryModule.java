package net.flectone.pulse.module.command.try_;

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
import net.flectone.pulse.module.command.try_.listener.TryProxyMessageListener;
import net.flectone.pulse.module.command.try_.model.TryMetadata;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
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
public class TryModule implements ModuleCommand<Localization.Command.CommandTry> {

    private final FileFacade fileFacade;
    private final RandomGenerator randomUtil;
    private final CommandParserProvider commandParserProvider;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ListenerRegistry listenerRegistry;
    private final ProxyRegistry proxyRegistry;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        String promptMessage = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::message);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
                .handler(this)
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(TryProxyMessageListener.class);
        }
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        int min = config().min();
        int max = config().max();
        int random = randomUtil.nextInt(min, max);

        String message = commandModuleController.getArgument(this, commandContext, 0);

        messageDispatcher.dispatch(this, TryMetadata.<Localization.Command.CommandTry>builder()
                .base(EventMetadata.<Localization.Command.CommandTry>builder()
                        .sender(fPlayer)
                        .format(replacePercent(random))
                        .range(config().range())
                        .destination(config().destination())
                        .message(message)
                        .sound(soundOrThrow())
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeInt(random);
                            dataOutputStream.writeString(message);
                        })
                        .integration(IntegrationMetadata.builder()
                                .messageNames(List.of(name().name() + "_" + String.valueOf(isGood(random)).toUpperCase()))
                                .build())
                        .build()
                )
                .percent(random)
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_TRY;
    }

    @Override
    public Command.CommandTry config() {
        return fileFacade.command().commandTry();
    }

    @Override
    public Permission.Command.CommandTry permission() {
        return fileFacade.permission().command().commandTry();
    }

    @Override
    public Localization.Command.CommandTry localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().commandTry();
    }

    public Function<Localization.Command.CommandTry, String> replacePercent(int value) {
        return message -> Strings.CS.replace(
                isGood(value) ? message.formatTrue() : message.formatFalse(),
                "<percent>",
                String.valueOf(value)
        );
    }

    private boolean isGood(int value) {
        return value >= config().good();
    }
}