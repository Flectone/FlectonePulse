package net.flectone.pulse.module.command.chatsetting.builder;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.inventory.Inventory;
import net.flectone.pulse.module.command.chatsetting.ChatsettingModule;
import net.flectone.pulse.module.command.chatsetting.handler.ChatsettingHandler;
import net.flectone.pulse.module.command.chatsetting.model.SubMenuItem;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.InventoryController;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class InventoryMenuBuilder implements MenuBuilder {

    private final ChatsettingModule chatsettingModule;
    private final PlatformServerAdapter platformServerAdapter;
    private final MessagePipeline messagePipeline;
    private final InventoryController inventoryController;
    private final ChatsettingHandler chatsettingHandler;
    private final FPlayerService fPlayerService;
    private final @Named("isNewerThanOrEqualsV_1_14") boolean isNewerThanOrEqualsV_1_14;

    @Override
    public void open(FPlayer fPlayer, UUID fTargetUUID) {
        FPlayer fTarget = fPlayerService.getFPlayer(fTargetUUID);

        Localization.Command.Chatsetting localization = chatsettingModule.localization(fPlayer);
        MessageContext headerContext = messagePipeline.createContext(fPlayer, fTarget, localization.inventory());
        Component header = messagePipeline.build(headerContext);

        Inventory.Builder inventoryBuilder = new Inventory.Builder()
                .name(header)
                .size(54);

        inventoryBuilder = createInventoryChatMenu(fPlayer, fTarget, inventoryBuilder, localization);
        inventoryBuilder = createInventoryFColorMenu(fPlayer, fTarget, FColor.Type.SEE, inventoryBuilder, chatsettingModule.config().menu().see(), localization.menu().see());
        inventoryBuilder = createInventoryFColorMenu(fPlayer, fTarget, FColor.Type.OUT, inventoryBuilder, chatsettingModule.config().menu().out(), localization.menu().out());

        for (String setting : chatsettingModule.config().checkbox().types().keySet()) {
            inventoryBuilder = createInventoryCheckbox(fPlayer, fTarget, setting, inventoryBuilder);
        }

        inventoryController.open(fPlayer, inventoryBuilder.build(isNewerThanOrEqualsV_1_14));
    }

    private Inventory.Builder createInventoryCheckbox(FPlayer fPlayer, FPlayer fTarget, String messageType, Inventory.Builder inventoryBuilder) {
        Command.Chatsetting.Checkbox checkbox = chatsettingModule.config().checkbox();

        int slot = checkbox.types().get(messageType);
        if (slot == -1) return inventoryBuilder;

        boolean enabled = fTarget.isSetting(messageType);

        String material = chatsettingModule.getCheckboxMaterial(enabled);
        String title = chatsettingModule.getCheckboxTitle(fPlayer, messageType, enabled);
        String lore = chatsettingModule.getCheckboxLore(fPlayer, enabled);

        return inventoryBuilder
                .addItem(slot, (ItemStack) platformServerAdapter.buildItemStack(fTarget, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) -> {
                    ChatsettingHandler.Status status = chatsettingHandler.handleCheckbox(fPlayer, fTarget, messageType);
                    if (status == ChatsettingHandler.Status.DENIED) return;

                    FPlayer finalFTarget = fPlayerService.getFPlayer(fTarget);
                    boolean currentEnabled = status.toBoolean();

                    String invertMaterial = chatsettingModule.getCheckboxMaterial(!currentEnabled);
                    String invertTitle = chatsettingModule.getCheckboxTitle(fPlayer, messageType, !currentEnabled);
                    String invertLore = chatsettingModule.getCheckboxLore(fPlayer, !currentEnabled);

                    ItemStack newItemStack = (ItemStack) platformServerAdapter.buildItemStack(finalFTarget, invertMaterial, invertTitle, invertLore);
                    inventoryController.changeItem(fPlayer, inventory, slot, newItemStack);

                    chatsettingModule.saveSetting(finalFTarget, messageType);
                });
    }

    private Inventory.Builder createInventoryChatMenu(FPlayer fPlayer, FPlayer fTarget, Inventory.Builder inventoryBuilder, Localization.Command.Chatsetting localization) {
        Command.Chatsetting.Menu.Chat chat = chatsettingModule.config().menu().chat();

        int slot = chat.slot();
        if (slot == -1) return inventoryBuilder;

        String currentChat = chatsettingModule.getPlayerChat(fTarget);

        String material = chatsettingModule.config().menu().material();

        String[] messages = Strings.CS.replace(
                localization.menu().chat().item(),
                "<chat>", currentChat
        ).split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[]{};

        return inventoryBuilder
                .addItem(slot, (ItemStack) platformServerAdapter.buildItemStack(fTarget, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) ->
                        chatsettingHandler.handleChatMenu(fPlayer, fTarget, chat, localization, this, null)
                );
    }

    private Inventory.Builder createInventoryFColorMenu(FPlayer fPlayer,
                                                        FPlayer fTarget,
                                                        FColor.Type type,
                                                        Inventory.Builder inventoryBuilder,
                                                        Command.Chatsetting.Menu.Color color,
                                                        Localization.Command.Chatsetting.Menu.SubMenu subMenu) {
        int slot = color.slot();
        if (slot == -1) return inventoryBuilder;

        String material = chatsettingModule.config().menu().material();

        String[] messages = subMenu.item().split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[]{};

        return inventoryBuilder
                .addItem(slot, (ItemStack) platformServerAdapter.buildItemStack(fTarget, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) ->
                        chatsettingHandler.handleFColorMenu(fPlayer, fTarget, type, color, subMenu, this, null)
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
                            @Nullable String id) {
        Inventory.Builder inventoryBuilder = new Inventory.Builder()
                .name(header)
                .size(54)
                .addCloseConsumer(inventory -> closeConsumer.run());

        FPlayer fTarget = fPlayerService.getFPlayer(fTargetUUID);

        for (int i = 0; i < items.size(); i++) {
            SubMenuItem item = items.get(i);
            String message = getItemMessage.apply(item);
            String[] messages = message.split("<br>");

            String title = messages.length > 0 ? messages[0] : "";
            String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[0];

            String material = item.material();

            inventoryBuilder.addItem(i, (ItemStack) platformServerAdapter.buildItemStack(fTarget, material, title, lore));
            inventoryBuilder.addClickHandler(i, (itemStack, inventory) -> chatsettingHandler.handleSubMenu(fPlayer, item, () -> {
                onSelect.accept(item);
                inventoryController.close(fPlayer.uuid());
                open(fPlayer, fTargetUUID);
            }));
        }

        inventoryController.open(fPlayer, inventoryBuilder.build(isNewerThanOrEqualsV_1_14));
    }
}
