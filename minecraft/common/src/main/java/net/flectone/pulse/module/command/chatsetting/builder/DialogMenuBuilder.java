package net.flectone.pulse.module.command.chatsetting.builder;

import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.action.DynamicCustomAction;
import com.github.retrooper.packetevents.protocol.dialog.body.DialogBody;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.dialog.Dialog;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.chatsetting.handler.ChatsettingHandler;
import net.flectone.pulse.module.command.chatsetting.model.SubMenuItem;
import net.flectone.pulse.platform.controller.DialogController;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DialogMenuBuilder implements MenuBuilder {

    private final ChatsettingModule chatsettingModule;
    private final MessagePipeline messagePipeline;
    private final DialogController dialogController;
    private final ChatsettingHandler chatsettingHandler;
    private final FPlayerService fPlayerService;

    @Override
    public void open(FPlayer fPlayer, UUID fTargetUUID) {
        FPlayer fTarget = fPlayerService.getFPlayer(fTargetUUID);

        Localization.Command.Chatsetting localization = chatsettingModule.localization(fPlayer);
        MessageContext headerContext = messagePipeline.createContext(fPlayer, fTarget, localization.inventory().trim());
        Component header = messagePipeline.build(headerContext);

        DialogBody dialogBody = new PlainMessageDialogBody(new PlainMessage(Component.empty(), 10));

        CommonDialogData commonDialogData = new CommonDialogData(
                header,
                null,
                true,
                false,
                DialogAction.CLOSE,
                List.of(dialogBody),
                Collections.emptyList()
        );

        Dialog.Builder dialogBuilder = new Dialog.Builder(commonDialogData, chatsettingModule.config().modern().columns());

        dialogBuilder = createDialogChatMenu(fPlayer, fTarget, dialogBuilder, localization);
        dialogBuilder = createDialogFColorMenu(fPlayer, fTarget, FColor.Type.SEE, dialogBuilder, chatsettingModule.config().menu().see(), localization.menu().see());
        dialogBuilder = createDialogFColorMenu(fPlayer, fTarget, FColor.Type.OUT, dialogBuilder, chatsettingModule.config().menu().out(), localization.menu().out());

        for (String setting : chatsettingModule.config().checkbox().types().keySet()) {
            dialogBuilder = createDialogCheckbox(fPlayer, fTarget, setting, dialogBuilder);
        }

        dialogController.open(fPlayer, dialogBuilder.build(), false);
    }

    private Dialog.Builder createDialogCheckbox(FPlayer fPlayer, FPlayer fTarget, String messageType, Dialog.Builder dialogBuilder) {
        Command.Chatsetting.Checkbox checkbox = chatsettingModule.config().checkbox();

        int slot = checkbox.types().get(messageType);
        if (slot == -1) return dialogBuilder;

        boolean enabled = fTarget.isSetting(messageType);

        String title = chatsettingModule.getCheckboxTitle(fPlayer, messageType, enabled);
        MessageContext titleContext = messagePipeline.createContext(fPlayer, fTarget, title);
        Component componentTitle = messagePipeline.build(titleContext);

        String lore = chatsettingModule.getCheckboxLore(fPlayer, enabled);
        MessageContext loreContext = messagePipeline.createContext(fPlayer, fTarget, lore);
        Component componentLore = messagePipeline.build(loreContext);

        String id = "fp_" + UUID.randomUUID();

        ActionButton button = new ActionButton(
                new CommonButtonData(componentTitle, componentLore, chatsettingModule.config().modern().buttonWidth()),
                new DynamicCustomAction(ResourceLocation.minecraft(id), null)
        );

        return dialogBuilder
                .addButton(slot, button)
                .addClickHandler(id, dialog -> {
                    ChatsettingHandler.Status status = chatsettingHandler.handleCheckbox(fPlayer, fTarget, messageType);
                    if (status == ChatsettingHandler.Status.DENIED) return;

                    FPlayer finalFTarget = fPlayerService.getFPlayer(fTarget);
                    boolean currentEnabled = status.toBoolean();

                    String invertTitle = chatsettingModule.getCheckboxTitle(fPlayer, messageType, !currentEnabled);
                    MessageContext invertTitleContext = messagePipeline.createContext(fPlayer, finalFTarget, invertTitle);
                    Component componentInvertTitle = messagePipeline.build(invertTitleContext);

                    String invertLore = chatsettingModule.getCheckboxLore(fPlayer, !currentEnabled);
                    MessageContext invertLoreContext = messagePipeline.createContext(fPlayer, finalFTarget, invertLore);
                    Component componentInvertLore = messagePipeline.build(invertLoreContext);

                    ActionButton invertButton = new ActionButton(
                            new CommonButtonData(componentInvertTitle, componentInvertLore, chatsettingModule.config().modern().buttonWidth()),
                            new DynamicCustomAction(ResourceLocation.minecraft(id), null)
                    );

                    dialogController.changeButton(fPlayer, dialog, id, invertButton);

                    chatsettingModule.saveSetting(finalFTarget, messageType);
                });
    }

    private Dialog.Builder createDialogChatMenu(FPlayer fPlayer, FPlayer fTarget, Dialog.Builder dialogBuilder, Localization.Command.Chatsetting localization) {
        Command.Chatsetting.Menu.Chat chat = chatsettingModule.config().menu().chat();

        int slot = chat.slot();
        if (slot == -1) return dialogBuilder;

        String currentChat = chatsettingModule.getPlayerChat(fTarget);

        String[] messages = Strings.CS.replace(
                localization.menu().chat().item(),
                "<chat>", currentChat
        ).split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String lore = messages.length > 1 ? String.join("<br>", Arrays.copyOfRange(messages, 1, messages.length)) : "";

        MessageContext titleContext = messagePipeline.createContext(fTarget, title);
        Component componentTitle = messagePipeline.build(titleContext);

        MessageContext loreContext = messagePipeline.createContext(fTarget, lore);
        Component componentLore = messagePipeline.build(loreContext);

        String id = "fp_chat";

        ActionButton button = new ActionButton(
                new CommonButtonData(componentTitle, componentLore, chatsettingModule.config().modern().buttonWidth()),
                new DynamicCustomAction(ResourceLocation.minecraft(id), null)
        );

        return dialogBuilder
                .addButton(slot, button)
                .addClickHandler(id, dialog ->
                        chatsettingHandler.handleChatMenu(fPlayer, fTarget, chat, localization, this, id)
                );
    }

    private Dialog.Builder createDialogFColorMenu(FPlayer fPlayer,
                                                  FPlayer fTarget,
                                                  FColor.Type type,
                                                  Dialog.Builder dialogBuilder,
                                                  Command.Chatsetting.Menu.Color color,
                                                  Localization.Command.Chatsetting.Menu.SubMenu subMenu) {
        int slot = color.slot();
        if (slot == -1) return dialogBuilder;

        String[] messages = subMenu.item().split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String lore = messages.length > 1 ? String.join("<br>", Arrays.copyOfRange(messages, 1, messages.length)) : "";

        MessageContext titleContext = messagePipeline.createContext(fTarget, title);
        Component componentTitle = messagePipeline.build(titleContext);

        MessageContext loreContext = messagePipeline.createContext(fTarget, lore);
        Component componentLore = messagePipeline.build(loreContext);

        String id = "fp_fcolor_" + type.ordinal();

        ActionButton button = new ActionButton(
                new CommonButtonData(componentTitle, componentLore, chatsettingModule.config().modern().buttonWidth()),
                new DynamicCustomAction(ResourceLocation.minecraft(id), null)
        );

        return dialogBuilder
                .addButton(slot, button)
                .addClickHandler(id, dialog ->
                        chatsettingHandler.handleFColorMenu(fPlayer, fTarget, type, color, subMenu, this, id)
                );
    }

    @Override
    public void openSubMenu(FPlayer fPlayer,
                            UUID fTargetUUID,
                            Component header,
                            Runnable closeConsumer,
                            List<SubMenuItem> items,
                            Function<SubMenuItem, String> getItemMessage,
                            Consumer<SubMenuItem> onSelect,
                            String id) {
        DialogBody dialogBody = new PlainMessageDialogBody(new PlainMessage(Component.empty(), 10));
        CommonDialogData commonDialogData = new CommonDialogData(
                header,
                null,
                true,
                false,
                DialogAction.CLOSE,
                List.of(dialogBody),
                Collections.emptyList()
        );

        Dialog.Builder dialogBuilder = new Dialog.Builder(commonDialogData, chatsettingModule.config().modern().columns())
                .addCloseConsumer(dialog -> closeConsumer.run());

        FPlayer fTarget = fPlayerService.getFPlayer(fTargetUUID);

        for (int i = 0; i < items.size(); i++) {
            SubMenuItem item = items.get(i);
            String message = getItemMessage.apply(item);
            String[] messages = message.split("<br>");

            String title = messages.length > 0 ? messages[0] : "";
            String lore = messages.length > 1 ? String.join("<br>", Arrays.copyOfRange(messages, 1, messages.length)) : "";

            MessageContext titleContext = messagePipeline.createContext(fTarget, title);
            Component componentTitle = messagePipeline.build(titleContext);

            MessageContext loreContext = messagePipeline.createContext(fTarget, lore);
            Component componentLore = messagePipeline.build(loreContext);

            String subId = id + "_" + i;

            ActionButton button = new ActionButton(
                    new CommonButtonData(componentTitle, componentLore, chatsettingModule.config().modern().buttonWidth()),
                    new DynamicCustomAction(ResourceLocation.minecraft(subId), null)
            );

            dialogBuilder.addButton(i, button);
            dialogBuilder.addClickHandler(subId, dialog -> chatsettingHandler.handleSubMenu(fPlayer, item, () -> {
                onSelect.accept(item);
                dialogController.close(fPlayer.uuid());
                open(fPlayer, fTargetUUID);
            }));
        }

        dialogController.open(fPlayer, dialogBuilder.build(), false);
    }
}