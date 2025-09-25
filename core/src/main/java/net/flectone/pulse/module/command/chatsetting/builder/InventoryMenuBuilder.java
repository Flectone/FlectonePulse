package net.flectone.pulse.module.command.chatsetting.builder;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Singleton
public class InventoryMenuBuilder implements MenuBuilder {

    private final ChatsettingModule chatsettingModule;
    private final PlatformServerAdapter platformServerAdapter;
    private final MessagePipeline messagePipeline;
    private final InventoryController inventoryController;
    private final ChatsettingHandler chatsettingHandler;
    private final boolean isNewerThanOrEqualsV_1_14;

    @Inject
    public InventoryMenuBuilder(ChatsettingModule chatsettingModule,
                                PlatformServerAdapter platformServerAdapter,
                                MessagePipeline messagePipeline,
                                InventoryController inventoryController,
                                ChatsettingHandler chatsettingHandler,
                                PacketProvider packetProvider) {
        this.chatsettingModule = chatsettingModule;
        this.platformServerAdapter = platformServerAdapter;
        this.messagePipeline = messagePipeline;
        this.inventoryController = inventoryController;
        this.chatsettingHandler = chatsettingHandler;
        this.isNewerThanOrEqualsV_1_14 = packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_14);
    }

    @Override
    public void open(FPlayer fPlayer, FPlayer fTarget) {
        Localization.Command.Chatsetting localization = chatsettingModule.localization(fPlayer);
        Component header = messagePipeline.builder(fPlayer, fTarget, localization.getInventory()).build();

        Inventory.Builder inventoryBuilder = new Inventory.Builder()
                .name(header)
                .size(54);

        inventoryBuilder = createInventoryChatMenu(fPlayer, fTarget, inventoryBuilder, localization);
        inventoryBuilder = createInventoryFColorMenu(fPlayer, fTarget, FColor.Type.SEE, inventoryBuilder, chatsettingModule.config().getMenu().getSee(), localization.getMenu().getSee());
        inventoryBuilder = createInventoryFColorMenu(fPlayer, fTarget, FColor.Type.OUT, inventoryBuilder, chatsettingModule.config().getMenu().getOut(), localization.getMenu().getOut());

        for (MessageType setting : MessageType.values()) {
            inventoryBuilder = createInventoryCheckbox(fPlayer, fTarget, setting, inventoryBuilder);
        }

        inventoryController.open(fPlayer, inventoryBuilder.build(isNewerThanOrEqualsV_1_14));
    }

    private Inventory.Builder createInventoryCheckbox(FPlayer fPlayer, FPlayer fTarget, MessageType messageType, Inventory.Builder inventoryBuilder) {
        Command.Chatsetting.Checkbox checkbox = chatsettingModule.config().getCheckbox();
        if (!checkbox.getTypes().containsKey(messageType.name())) return inventoryBuilder;

        int slot = checkbox.getTypes().get(messageType.name());
        if (slot == -1) return inventoryBuilder;

        boolean enabled = fTarget.isSetting(messageType);

        String material = chatsettingModule.getCheckboxMaterial(enabled);
        String title = chatsettingModule.getCheckboxTitle(fPlayer, messageType.name(), enabled);
        String lore = chatsettingModule.getCheckboxLore(fPlayer, enabled);

        return inventoryBuilder
                .addItem(slot, platformServerAdapter.buildItemStack(fTarget, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) -> {
                    ChatsettingHandler.Status status = chatsettingHandler.handleCheckbox(fPlayer, fTarget, messageType);
                    if (status == ChatsettingHandler.Status.DENIED) return;

                    boolean currentEnabled = status.toBoolean();
                    String invertMaterial = chatsettingModule.getCheckboxMaterial(!currentEnabled);
                    String invertTitle = chatsettingModule.getCheckboxTitle(fPlayer, messageType.name(), !currentEnabled);
                    String invertLore = chatsettingModule.getCheckboxLore(fPlayer, !currentEnabled);

                    ItemStack newItemStack = platformServerAdapter.buildItemStack(fTarget, invertMaterial, invertTitle, invertLore);
                    inventoryController.changeItem(fPlayer, inventory, slot, newItemStack);

                    chatsettingModule.saveSetting(fPlayer, messageType);
                });
    }

    private Inventory.Builder createInventoryChatMenu(FPlayer fPlayer, FPlayer fTarget, Inventory.Builder inventoryBuilder, Localization.Command.Chatsetting localization) {
        Command.Chatsetting.Menu.Chat chat = chatsettingModule.config().getMenu().getChat();

        int slot = chat.getSlot();
        if (slot == -1) return inventoryBuilder;

        String currentChat = chatsettingModule.getPlayerChat(fTarget);

        String material = chatsettingModule.config().getMenu().getMaterial();

        String[] messages = Strings.CS.replace(
                localization.getMenu().getChat().getItem(),
                "<chat>", currentChat
        ).split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[]{};

        return inventoryBuilder
                .addItem(slot, platformServerAdapter.buildItemStack(fTarget, material, title, lore))
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
        int slot = color.getSlot();
        if (slot == -1) return inventoryBuilder;

        String material = chatsettingModule.config().getMenu().getMaterial();

        String[] messages = subMenu.getItem().split("<br>");

        String title = messages.length > 0 ? messages[0] : "";
        String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[]{};

        return inventoryBuilder
                .addItem(slot,platformServerAdapter.buildItemStack(fTarget, material, title, lore))
                .addClickHandler(slot, (itemStack, inventory) ->
                        chatsettingHandler.handleFColorMenu(fPlayer, fTarget, type, color, subMenu, this, null)
                );
    }

    @Override
    public void openSubMenu(FPlayer fPlayer, FPlayer fTarget,
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

        for (int i = 0; i < items.size(); i++) {
            SubMenuItem item = items.get(i);
            String message = getItemMessage.apply(item);
            String[] messages = message.split("<br>");

            String title = messages.length > 0 ? messages[0] : "";
            String[] lore = messages.length > 1 ? Arrays.copyOfRange(messages, 1, messages.length) : new String[0];

            String material = item.material();

            inventoryBuilder.addItem(i, platformServerAdapter.buildItemStack(fTarget, material, title, lore));
            inventoryBuilder.addClickHandler(i, (itemStack, inventory) -> chatsettingHandler.handleSubMenu(fPlayer, item, () -> {
                onSelect.accept(item);
                inventoryController.close(fPlayer.getUuid());
                open(fPlayer, fTarget);
            }));
        }

        inventoryController.open(fPlayer, inventoryBuilder.build(isNewerThanOrEqualsV_1_14));
    }
}
