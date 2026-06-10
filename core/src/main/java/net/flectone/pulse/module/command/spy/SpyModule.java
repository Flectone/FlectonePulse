package net.flectone.pulse.module.command.spy;

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
import net.flectone.pulse.module.command.spy.listener.SpyProxyMessageListener;
import net.flectone.pulse.module.command.spy.model.SpyMetadata;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpyModule implements ModuleCommand<Localization.Command.Spy> {

    private final FileFacade fileFacade;
    private final SocialService socialService;
    private final PermissionChecker permissionChecker;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        commandModuleController.registerCommand(this, manager -> manager
                .permission(permission().name())
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(SpyProxyMessageListener.class);
        }
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        boolean turnedBefore = socialService.getSetting(fPlayer, SettingText.SPY_STATUS) != null;

        socialService.saveSetting(fPlayer, SettingText.SPY_STATUS, turnedBefore ? null : "1");

        messageDispatcher.dispatch(this, SpyMetadata.<Localization.Command.Spy>builder()
                .base(EventMetadata.<Localization.Command.Spy>builder()
                        .sender(fPlayer)
                        .format(localization -> !turnedBefore ? localization.formatTrue() : localization.formatFalse())
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .build()
                )
                .turned(!turnedBefore)
                .action("turning")
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_SPY;
    }

    @Override
    public Command.Spy config() {
        return fileFacade.command().spy();
    }

    @Override
    public Permission.Command.Spy permission() {
        return fileFacade.permission().command().spy();
    }

    @Override
    public Localization.Command.Spy localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().spy();
    }

    public void check(FPlayer fPlayer, String chat, String message, List<FPlayer> receivers) {
        if (!moduleController.isEnable(this)) return;
        if (!needToSpy("action", chat)) return;

        spy(fPlayer, chat, message, receivers);
    }

    public void spy(FPlayer fPlayer, String action, String message) {
        spy(fPlayer, action, message, List.of());
    }

    public void spy(FPlayer fPlayer, String action, String message, List<FPlayer> receivers) {
        if (!moduleController.isEnable(this)) return;

        messageDispatcher.dispatch(this, SpyMetadata.<Localization.Command.Spy>builder()
                .base(EventMetadata.<Localization.Command.Spy>builder()
                        .sender(fPlayer)
                        .format(Localization.Command.Spy::formatLog)
                        .range(config().range())
                        .destination(config().destination())
                        .message(message)
                        .filter(createFilter(fPlayer, receivers))
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeString(action);
                            dataOutputStream.writeString(message);
                        })
                        .tagResolvers(fReceiver -> new TagResolver[]{
                                Placeholder.parsed("action", localization(fReceiver).actions().getOrDefault(action, action))
                        })
                        .integration(IntegrationMetadata.builder()
                                .messageNames(List.of(name().name() + "_" + action.toUpperCase()))
                                .build()
                        )
                        .build()
                )
                .turned(true)
                .action(action)
                .build()
        );
    }

    public Predicate<FPlayer> createFilter(FPlayer fPlayer, List<FPlayer> receivers) {
        return fReceiver -> !fPlayer.equals(fReceiver)
                && !receivers.contains(fReceiver)
                && permissionChecker.check(fReceiver, permission())
                && socialService.getSetting(fReceiver, SettingText.SPY_STATUS) != null
                && fReceiver.isOnline();
    }

    protected boolean needToSpy(String category, String value) {
        Map<String, List<String>> categories = config().categories();

        List<String> values = categories.get(category);

        return values != null && values.contains(value);
    }

}
