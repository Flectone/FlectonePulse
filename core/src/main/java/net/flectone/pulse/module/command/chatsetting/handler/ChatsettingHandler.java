package net.flectone.pulse.module.command.chatsetting.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.chatsetting.builder.MenuBuilder;
import net.flectone.pulse.module.command.chatsetting.model.SubMenuItem;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

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
    private final MessageDispatcher messageDispatcher;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;

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
            messageDispatcher.dispatchError(chatsettingModule, EventMetadata.<Localization.Command.Chatsetting>builder()
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
                localization.menu().chat().types().getOrDefault(item.name(), item.name()),
                "<chat>", item.name()
        );

        Consumer<SubMenuItem> onSelect = item -> {
            String chatName = "default".equalsIgnoreCase(item.name()) ? null : item.name();
            socialService.saveSetting(fTarget, SettingText.CHAT_NAME, chatName);
        };

        Component header = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .receiver(fTarget)
                .message(localization.menu().chat().inventory())
                .build()
        );

        Runnable closeConsumer = () -> {};

        menuBuilder.openSubMenu(fPlayer, fTarget.uuid(), header, closeConsumer, items, getItemMessage, onSelect, id);
    }

    public void handleFColorMenu(FPlayer fPlayer,
                                 FPlayer fTarget,
                                 FColor.Type type,
                                 Command.Chatsetting.Menu.Color color,
                                 Localization.Command.Chatsetting.Menu.SubMenu subMenu,
                                 MenuBuilder menuBuilder,
                                 @Nullable String id) {
        if (!permissionChecker.check(fPlayer, chatsettingModule.permission().settings().get("FCOLOR_" + type.name()))) {
            messageDispatcher.dispatchError(chatsettingModule, EventMetadata.<Localization.Command.Chatsetting>builder()
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
            Set<FColor> fColors = new ObjectOpenHashSet<>(socialService.loadColors(fTarget).getOrDefault(type, Set.of()));

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

            socialService.saveColors(fTarget, type, fColors);
        };

        Component header = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .receiver(fTarget)
                .message(subMenu.inventory())
                .build()
        );

        Runnable closeConsumer = () -> {};

        menuBuilder.openSubMenu(fPlayer, fTarget.uuid(), header, closeConsumer, items, getItemMessage, onSelect, id);
    }

    public void handleSubMenu(FPlayer fPlayer, SubMenuItem item, Runnable successRunnable) {
        if (item.perm() != null && !permissionChecker.check(fPlayer, item.perm())) {
            messageDispatcher.dispatchError(chatsettingModule, EventMetadata.<Localization.Command.Chatsetting>builder()
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
            messageDispatcher.dispatchError(chatsettingModule, EventMetadata.<Localization.Command.Chatsetting>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Chatsetting::noPermission)
                    .build()
            );

            return Status.DENIED;
        }

        boolean currentEnabled = socialService.isSetting(fTarget, messageType);

        chatsettingModule.saveSetting(fTarget, messageType, !currentEnabled);

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