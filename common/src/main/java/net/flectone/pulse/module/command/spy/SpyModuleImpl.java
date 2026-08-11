package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.checker.PermissionChecker;
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
import net.flectone.pulse.module.command.spy.listener.SpyProxyMessageListener;
import net.flectone.pulse.module.command.spy.model.SpyMessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.context.CommandContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpyModuleImpl implements SpyModule {

    private final FileFacade fileFacade;
    private final SocialService socialService;
    private final PermissionChecker permissionChecker;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;
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

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .destination(config().destination())
                .sound(soundOrThrow())
                .messageContext(fResolver -> SpyMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(!turnedBefore ? localization(fResolver).formatTrue() : localization(fResolver).formatFalse())
                                .build()
                        )
                        .turned(!turnedBefore)
                        .action("turning")
                        .build()
                )
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

    @Override
    public void checkAnvil(@NonNull UUID player, @Nullable String itemName) {
        if (!moduleController.isEnable(this)) return;
        if (StringUtils.isEmpty(itemName)) return;

        taskScheduler.runAsync(() -> {
            if (!needToSpy("action", "anvil")) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(player);

            spy(fPlayer, "anvil", itemName);
        });
    }

    @Override
    public void checkSign(@NonNull UUID player, @Nullable String[] lines) {
        if (!moduleController.isEnable(this)) return;
        if (lines == null) return;

        taskScheduler.runAsync(() -> {
            if (!needToSpy("action", "sign")) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(player);

            String message = prepareMessage(lines);
            if (StringUtils.isEmpty(message)) return;

            spy(fPlayer, "sign", message);
        });
    }

    @Override
    public void checkBook(@NonNull UUID player, @Nullable String title, @Nullable List<String> pages) {
        if (!moduleController.isEnable(this)) return;

        taskScheduler.runAsync(() -> {
            if (!needToSpy("action", "book")) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(player);

            if (pages != null) {
                String message = prepareMessage(pages);
                if (StringUtils.isNotEmpty(message)) {
                    spy(fPlayer, "book", message);
                }
            }

            if (StringUtils.isNotEmpty(title)) {
                spy(fPlayer, "book", title);
            }
        });
    }

    @Override
    public void checkCommand(@NonNull UUID player, @Nullable String command) {
        if (!moduleController.isEnable(this)) return;
        if (StringUtils.isEmpty(command)) return;

        taskScheduler.runAsync(() -> {
            String[] arguments = command.split(" ");

            String commandName = command.startsWith("/") ? arguments[0].substring(1) : arguments[0];
            if (!needToSpy("command", commandName)) return;

            FPlayer fPlayer = fPlayerService.getFPlayer(player);
            FPlayer fReceiver = arguments.length > 1 ? fPlayerService.getFPlayer(arguments[1]) : FPlayer.UNKNOWN;

            spy(fPlayer, commandName, command, fReceiver.isUnknown() ? Set.of() : Set.of(fReceiver));
        });
    }

    @Override
    public void checkChat(@NonNull UUID player, @Nullable String message, @NonNull List<UUID> receivers) {
        if (!moduleController.isEnable(this)) return;
        if (StringUtils.isEmpty(message)) return;

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(player);
            checkChat(fPlayer, "chat", message, receivers.stream()
                    .map(fPlayerService::getFPlayer)
                    .collect(Collectors.toSet())
            );
        });
    }

    @Override
    public void checkChat(@NonNull FPlayer fPlayer, @NonNull String chat, @NonNull String message, @NonNull Set<FPlayer> receivers) {
        if (!moduleController.isEnable(this)) return;
        if (!needToSpy("action", chat)) return;

        spy(fPlayer, chat, message, receivers);
    }

    @Override
    public void spy(@NonNull FPlayer fPlayer, @NonNull String action, @NonNull String message) {
        spy(fPlayer, action, message, Set.of());
    }

    @Override
    public void spy(@NonNull FPlayer fPlayer, @NonNull String action, @NonNull String message, @NonNull Set<FPlayer> receivers) {
        if (!moduleController.isEnable(this)) return;

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .range(config().range())
                .filter(createFilter(fPlayer, receivers))
                .destination(config().destination())
                .messageContext(fResolver -> SpyMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(localization(fResolver).formatLog())
                                .tagResolvers(messagePipeline.messageTag(fPlayer, fResolver, message), Placeholder.parsed("action", localization(fResolver).actions().getOrDefault(action, action)))
                                .build()
                        )
                        .string(message)
                        .turned(true)
                        .action(action)
                        .build()
                )
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeUTF(action);
                    dataOutputStream.writeUTF(message);
                })
                .integration(() -> IntegrationMessageFormat.builder()
                        .messageNames(List.of(name().name() + "_" + action.toUpperCase()))
                        .build()
                )
                .build()
        );
    }

    @Override
    public Predicate<FPlayer> createFilter(FPlayer fPlayer, Set<FPlayer> receivers) {
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

    private String prepareMessage(String[] strings) {
        return Arrays.stream(strings)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "));
    }

    private String prepareMessage(Collection<String> strings) {
        return strings.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "));
    }

}
