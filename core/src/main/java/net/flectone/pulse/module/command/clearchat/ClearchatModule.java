package net.flectone.pulse.module.command.clearchat;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.clearchat.listener.ClearchatProxyMessageListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.filter.RangeFilter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClearchatModule implements ModuleCommand<Localization.Command.Clearchat> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final RangeFilter rangeFilter;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .optional(promptPlayer, commandParserProvider.playerParser(), commandParserProvider.playerSuggestionPermission(false, permission().other()))
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(ClearchatProxyMessageListener.class);
        }
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleCommand.super.permissionBuilder()
                .add(permission().other());
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String promptPlayer = commandModuleController.getPrompt(this, 0);
        Optional<String> optionalPlayer = commandContext.optional(promptPlayer);

        FPlayer fTarget = fPlayer;

        if (optionalPlayer.isPresent() && permissionChecker.check(fPlayer, permission().other())) {
            String player = optionalPlayer.get();

            Range range = player.equalsIgnoreCase("all")
                    ? Range.get(Range.Type.PROXY)
                    : Range.fromString(player).orElse(null);
            if (range != null) {
                fPlayerService.getOnlineFPlayers().stream()
                        .filter(rangeFilter.createFilter(fPlayer, range))
                        .forEach(this::clearChat);
                return;
            }

            fTarget = fPlayerService.getFPlayer(player);
            if (fTarget.isUnknown()) {
                messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Clearchat>builder()
                        .sender(fPlayer)
                        .format(Localization.Command.Clearchat::nullPlayer)
                        .build()
                );

                return;
            }
        }

        clearChat(fTarget);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_CLEARCHAT;
    }

    @Override
    public Command.Clearchat config() {
        return fileFacade.command().clearchat();
    }

    @Override
    public Permission.Command.Clearchat permission() {
        return fileFacade.permission().command().clearchat();
    }

    @Override
    public Localization.Command.Clearchat localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().clearchat();
    }

    public void clearChat(FPlayer fPlayer) {
        clearChat(fPlayer, true);
    }

    public void clearChat(FPlayer fPlayer, boolean checkProxy) {
        if (checkProxy
                && !platformPlayerAdapter.isOnline(fPlayer)
                && proxySender.send(fPlayer, ModuleName.COMMAND_CLEARCHAT)) {
            return;
        }

        messageDispatcher.dispatch(this, EventMetadata.<Localization.Command.Clearchat>builder()
                .sender(fPlayer)
                .format("<br> ".repeat(config().length()))
                .build()
        );

        messageDispatcher.dispatch(this, EventMetadata.<Localization.Command.Clearchat>builder()
                .sender(fPlayer)
                .format(Localization.Command.Clearchat::format)
                .destination(config().destination())
                .sound(soundOrThrow())
                .build()
        );
    }
}
