package net.flectone.pulse.module.command.chatsetting.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
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
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatsettingHandler {

    private final FileResolver fileResolver;
    private final ChatsettingModule chatsettingModule;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;

    public Permission.Message.Chat chatPermission() {
        return fileResolver.getPermission().getMessage().getChat();
    }

    public void handleChatMenu(FPlayer fPlayer,
                               FPlayer fTarget,
                               Command.Chatsetting.Menu.Chat chat,
                               Localization.Command.Chatsetting localization,
                               MenuBuilder menuBuilder,
                               @Nullable String id) {
        if (!permissionChecker.check(fPlayer, chatsettingModule.permission().getSettings().get(SettingText.CHAT_NAME.name()))) {
            chatsettingModule.sendErrorMessage(chatsettingModule.metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatsetting::getNoPermission)
                    .build()
            );

            return;
        }

        List<SubMenuItem> items = chat.getTypes().stream()
                .map(t -> new SubMenuItem(t.getName(), t.getMaterial(), null, chatPermission().getTypes().get(t.getName())))
                .toList();

        Function<SubMenuItem, String> getItemMessage = item -> Strings.CS.replace(
                localization.getMenu().getChat().getTypes().getOrDefault(item.name(), ""),
                "<chat>", item.name()
        );

        Consumer<SubMenuItem> onSelect = item -> fTarget.setSetting(SettingText.CHAT_NAME, "default".equalsIgnoreCase(item.name()) ? null : item.name());

        String headerStr = localization.getMenu().getChat().getInventory();
        Component header = messagePipeline.builder(fPlayer, fTarget, headerStr).build();

        Runnable closeConsumer = () -> chatsettingModule.saveSetting(fTarget, SettingText.CHAT_NAME);

        menuBuilder.openSubMenu(fPlayer, fTarget, header, closeConsumer, items, getItemMessage, onSelect, id);
    }

    public void handleFColorMenu(FPlayer fPlayer,
                                 FPlayer fTarget,
                                 FColor.Type type,
                                 Command.Chatsetting.Menu.Color color,
                                 Localization.Command.Chatsetting.Menu.SubMenu subMenu,
                                 MenuBuilder menuBuilder,
                                 @Nullable String id) {
        if (!permissionChecker.check(fPlayer, chatsettingModule.permission().getSettings().get("FCOLOR_" + type.name()))) {
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

    public Status handleCheckbox(FPlayer fPlayer, FPlayer fTarget, MessageType messageType) {
        if (!permissionChecker.check(fPlayer, chatsettingModule.permission().getSettings().get(messageType.name()))) {
            chatsettingModule.sendMessage(chatsettingModule.metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatsetting::getNoPermission)
                    .build()
            );

            return Status.DENIED;
        }

        boolean currentEnabled = fTarget.isSetting(messageType);
        fTarget.setSetting(messageType, !currentEnabled);

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
