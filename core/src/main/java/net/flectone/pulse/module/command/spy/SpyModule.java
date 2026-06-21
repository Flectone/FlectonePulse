package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
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
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private final TaskScheduler taskScheduler;
    private final FPlayerService fPlayerService;

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

    public void checkAnvil(@NonNull UUID player, @Nullable String itemName) {
        if (!moduleController.isEnable(this)) return;
        if (StringUtils.isEmpty(itemName)) return;

        taskScheduler.runAsync(() -> {
            if (!needToSpy("action", "anvil")) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(player);

            spy(fPlayer, "anvil", itemName);
        });
    }

    public void checkSign(@NonNull UUID player, @Nullable String[] lines) {
        if (!moduleController.isEnable(this)) return;
        if (lines == null) return;

        taskScheduler.runAsync(() -> {
            if (!needToSpy("action", "sign")) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(player);

            String message = String.join(" ", lines);
            spy(fPlayer, "sign", message);
        });
    }

    public void checkBook(@NonNull UUID player, @Nullable String title, @Nullable List<String> pages) {
        if (!moduleController.isEnable(this)) return;

        taskScheduler.runAsync(() -> {
            if (!needToSpy("action", "book")) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(player);

            if (pages != null) {
                spy(fPlayer, "book", String.join(" ", pages));
            }

            if (StringUtils.isNotEmpty(title)) {
                spy(fPlayer, "book", title);
            }
        });
    }

    public void checkCommand(@NonNull UUID player, @Nullable String command) {
        if (!moduleController.isEnable(this)) return;
        if (StringUtils.isEmpty(command)) return;

        taskScheduler.runAsync(() -> {
            String[] arguments = command.split(" ");

            String commandName = command.startsWith("/") ? arguments[0].substring(1) : arguments[0];
            if (!needToSpy("command", commandName)) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(player);
            FPlayer fReceiver = arguments.length > 1 ? fPlayerService.getFPlayer(arguments[1]) : FPlayer.UNKNOWN;

            spy(fPlayer, commandName, command, fReceiver.isUnknown() ? List.of() : List.of(fReceiver));
        });
    }

    public void checkChat(@NonNull UUID player, @Nullable String message, @NonNull List<UUID> receivers) {
        if (!moduleController.isEnable(this)) return;
        if (StringUtils.isEmpty(message)) return;

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(player);
            checkChat(fPlayer, "chat", message, receivers.stream()
                    .map(fPlayerService::getFPlayer)
                    .toList()
            );
        });
    }

    public void checkChat(@NonNull FPlayer fPlayer, @NonNull String chat, @NonNull String message, @NonNull List<FPlayer> receivers) {
        if (!moduleController.isEnable(this)) return;
        if (!needToSpy("action", chat)) return;

        spy(fPlayer, chat, message, receivers);
    }

    public void spy(@NonNull FPlayer fPlayer, @NonNull String action, @NonNull String message) {
        spy(fPlayer, action, message, List.of());
    }

    public void spy(@NonNull FPlayer fPlayer, @NonNull String action, @NonNull String message, @NonNull List<FPlayer> receivers) {
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
