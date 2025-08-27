package net.flectone.pulse.module.command.chatsetting.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.chatsetting.builder.MenuBuilder;
import net.flectone.pulse.module.command.chatsetting.model.SubMenuItem;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class ChatsettingHandler {

    private final Permission.Message.Chat chatPermission;
    private final Permission.Command.Chatsetting permission;
    private final ChatsettingModule chatsettingModule;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;

    @Inject
    public ChatsettingHandler(FileResolver fileResolver,
                              ChatsettingModule chatsettingModule,
                              PermissionChecker permissionChecker,
                              MessagePipeline messagePipeline,
                              FPlayerService fPlayerService) {
        this.chatPermission = fileResolver.getPermission().getMessage().getChat();
        this.permission = fileResolver.getPermission().getCommand().getChatsetting();
        this.chatsettingModule = chatsettingModule;
        this.permissionChecker = permissionChecker;
        this.messagePipeline = messagePipeline;
        this.fPlayerService = fPlayerService;
    }

    public void handleChatMenu(FPlayer fPlayer,
                               FPlayer fTarget,
                               Command.Chatsetting.Menu.Chat chat,
                               Localization.Command.Chatsetting localization,
                               MenuBuilder menuBuilder,
                               @Nullable String id) {
        if (!permissionChecker.check(fPlayer, permission.getSettings().get(FPlayer.Setting.CHAT))) {
            chatsettingModule.sendMessage(chatsettingModule.metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatsetting::getNoPermission)
                    .build()
            );

            return;
        }

        List<SubMenuItem> items = chat.getTypes().stream()
                .map(t -> new SubMenuItem(t.getName(), t.getMaterial(), null, chatPermission.getTypes().get(t.getName())))
                .toList();

        Function<SubMenuItem, String> getItemMessage = item -> Strings.CS.replace(
                localization.getMenu().getChat().getTypes().getOrDefault(item.name(), ""),
                "<chat>", item.name()
        );

        Consumer<SubMenuItem> onSelect = item -> fTarget.setSetting(FPlayer.Setting.CHAT, "default".equalsIgnoreCase(item.name()) ? null : item.name());

        String headerStr = localization.getMenu().getChat().getInventory();
        Component header = messagePipeline.builder(fPlayer, fTarget, headerStr).build();

        Runnable closeConsumer = () -> chatsettingModule.updateSettings(fTarget);

        menuBuilder.openSubMenu(fPlayer, fTarget, header, closeConsumer, items, getItemMessage, onSelect, id);
    }

    public void handleFColorMenu(FPlayer fPlayer,
                                 FPlayer fTarget,
                                 FColor.Type type,
                                 Command.Chatsetting.Menu.Color color,
                                 Localization.Command.Chatsetting.Menu.SubMenu subMenu,
                                 MenuBuilder menuBuilder,
                                 @Nullable String id) {
        if (!permissionChecker.check(fPlayer, permission.getFcolors().get(type))) {
            chatsettingModule.sendMessage(chatsettingModule.metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatsetting::getNoPermission)
                    .build()
            );

            return;
        }

        List<SubMenuItem> items = color.getTypes().stream()
                .map(t -> new SubMenuItem(t.getName(), t.getMaterial(), t.getColors(), null))
                .toList();

        Function<SubMenuItem, String> getItemMessage = item -> {
            String message = subMenu.getTypes().getOrDefault(item.name(), "");
            for (Map.Entry<Integer, String> entry : item.colors().entrySet()) {
                String trigger = "<fcolor:" + entry.getKey() + ">";
                String value = entry.getValue().isBlank() ? trigger : entry.getValue();
                message = Strings.CS.replace(message, trigger, value);
            }
            return message;
        };

        Consumer<SubMenuItem> onSelect = item -> fTarget.getFColors().put(type, item.colors().entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isBlank())
                .map(entry -> new FColor(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet())
        );

        String headerStr = subMenu.getInventory();
        Component header = messagePipeline.builder(fPlayer, fTarget, headerStr).build();

        Runnable closeConsumer = () -> fPlayerService.saveColors(fTarget);

        menuBuilder.openSubMenu(fPlayer, fTarget, header, closeConsumer, items, getItemMessage, onSelect, id);
    }

    public void handleSubMenu(FPlayer fPlayer, SubMenuItem item, Runnable successRunnable) {
        if (item.perm() != null && !permissionChecker.check(fPlayer, item.perm())) {
            chatsettingModule.sendMessage(chatsettingModule.metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatsetting::getNoPermission)
                    .build()
            );

            return;
        }

        successRunnable.run();
    }

    public Status handleCheckbox(FPlayer fPlayer, FPlayer fTarget, FPlayer.Setting setting) {
        if (!permissionChecker.check(fPlayer, permission.getSettings().get(setting))) {
            chatsettingModule.sendMessage(chatsettingModule.metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatsetting::getNoPermission)
                    .build()
            );

            return Status.DENIED;
        }

        boolean currentEnabled = fTarget.isSetting(setting);

        if (currentEnabled) {
            fTarget.removeSetting(setting);
        } else {
            fTarget.setSetting(setting, "");
        }

        return currentEnabled ? Status.ENABLED : Status.DISABLED;
    }

    public enum Status {
        DENIED,
        ENABLED,
        DISABLED;

        public boolean toBoolean() {
            return switch (this) {
                case ENABLED -> true;
                case DISABLED -> false;
                default -> throw new IllegalArgumentException();
            };
        }
    }

}
