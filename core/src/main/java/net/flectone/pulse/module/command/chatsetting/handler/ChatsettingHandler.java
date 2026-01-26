package net.flectone.pulse.module.command.chatsetting.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.chatsetting.builder.MenuBuilder;
import net.flectone.pulse.module.command.chatsetting.model.SubMenuItem;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatsettingHandler {

    private final FileFacade fileFacade;
    private final ChatsettingModule chatsettingModule;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;

    public Permission.Message.Chat chatPermission() {
        return fileFacade.permission().message().chat();
    }

    public void handleChatMenu(FPlayer fPlayer,
                               FPlayer fTarget,
                               Command.Chatsetting.Menu.Chat chat,
                               Localization.Command.Chatsetting localization,
                               MenuBuilder menuBuilder,
                               @Nullable String id) {
        if (!permissionChecker.check(fPlayer, chatsettingModule.permission().settings().get(SettingText.CHAT_NAME.name()))) {
            chatsettingModule.sendErrorMessage(EventMetadata.<Localization.Command.Chatsetting>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatsetting::noPermission)
                    .build()
            );
            return;
        }

        List<SubMenuItem> items = chat.types().stream()
                .map(t -> new SubMenuItem(t.name(), t.material(), null, chatPermission().types().get(t.name())))
                .toList();

        Function<SubMenuItem, String> getItemMessage = item -> Strings.CS.replace(
                localization.menu().chat().types().getOrDefault(item.name(), ""),
                "<chat>", item.name()
        );

        Consumer<SubMenuItem> onSelect = item -> fTarget.setSetting(SettingText.CHAT_NAME, "default".equalsIgnoreCase(item.name()) ? null : item.name());

        String headerStr = localization.menu().chat().inventory();
        MessageContext headerContext = messagePipeline.createContext(fPlayer, fTarget, headerStr);
        Component header = messagePipeline.build(headerContext);

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
        if (!permissionChecker.check(fPlayer, chatsettingModule.permission().settings().get("FCOLOR_" + type.name()))) {
            chatsettingModule.sendMessage(EventMetadata.<Localization.Command.Chatsetting>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatsetting::noPermission)
                    .build()
            );
            return;
        }

        List<SubMenuItem> items = color.types().stream()
                .map(t -> new SubMenuItem(t.name(), t.material(), t.colors(), null))
                .toList();

        Function<SubMenuItem, String> getItemMessage = item -> {
            String message = subMenu.types().getOrDefault(item.name(), "");
            for (Map.Entry<Integer, String> entry : item.colors().entrySet()) {
                String trigger = "<fcolor:" + entry.getKey() + ">";

                // "null" - skip color
                // "" (empty) - default color
                String value = StringUtils.isEmpty(entry.getValue())
                        ? fileFacade.message().format().fcolor().defaultColors().getOrDefault(entry.getKey(), trigger)
                        : "null".equals(entry.getValue()) ? trigger : entry.getValue();

                message = Strings.CS.replace(message, trigger, value);
            }
            return message;
        };

        Consumer<SubMenuItem> onSelect = item -> {
            Set<FColor> fColors = new HashSet<>(fTarget.getFColors().getOrDefault(type, Set.of()));

            // skip "null" colors replace
            item.colors().entrySet().stream()
                    .filter(entry -> !"null".equals(entry.getValue()))
                    .forEach(entry -> {
                        Integer number = entry.getKey();
                        String value = entry.getValue();

                        fColors.removeIf(fColor -> fColor.number() == number);

                        if (StringUtils.isNotEmpty(value)) {
                            fColors.add(new FColor(number, value));
                        }

                    });

            fTarget.getFColors().put(type, Set.copyOf(fColors));
        };

        String headerStr = subMenu.inventory();
        MessageContext headerContext = messagePipeline.createContext(fPlayer, fTarget, headerStr);
        Component header = messagePipeline.build(headerContext);

        Runnable closeConsumer = () -> fPlayerService.saveColors(fTarget);

        menuBuilder.openSubMenu(fPlayer, fTarget, header, closeConsumer, items, getItemMessage, onSelect, id);
    }

    public void handleSubMenu(FPlayer fPlayer, SubMenuItem item, Runnable successRunnable) {
        if (item.perm() != null && !permissionChecker.check(fPlayer, item.perm())) {
            chatsettingModule.sendMessage(EventMetadata.<Localization.Command.Chatsetting>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatsetting::noPermission)
                    .build()
            );
            return;
        }

        successRunnable.run();
    }

    public Status handleCheckbox(FPlayer fPlayer, FPlayer fTarget, String messageType) {
        if (!permissionChecker.check(fPlayer, chatsettingModule.permission().settings().get(messageType))) {
            chatsettingModule.sendMessage(EventMetadata.<Localization.Command.Chatsetting>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatsetting::noPermission)
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