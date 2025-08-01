package net.flectone.pulse.platform.controller;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.inventory.ClickType;
import net.flectone.pulse.model.inventory.Inventory;
import net.flectone.pulse.platform.sender.PacketSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InventoryController {

    private final Map<UUID, Inventory> inventoryMap = new ConcurrentHashMap<>();

    private final PacketSender packetSender;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public InventoryController(PacketSender packetSender,
                               PlatformPlayerAdapter platformPlayerAdapter) {
        this.packetSender = packetSender;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    public Inventory get(UUID uuid) {
        return inventoryMap.get(uuid);
    }

    public void close(UUID uuid) {
        Inventory inventory = inventoryMap.get(uuid);
        if (inventory == null) return;

        inventory.getCloseConsumerList().forEach(closeConsumer -> closeConsumer.accept(inventory));
        inventoryMap.remove(uuid);
    }

    public void closeAll() {
        WrapperPlayServerCloseWindow wrapper = new WrapperPlayServerCloseWindow();
        inventoryMap.keySet().forEach(uuid -> packetSender.send(uuid, wrapper));
        inventoryMap.clear();
    }

    public void open(FPlayer fPlayer, Inventory inventory) {
        inventoryMap.put(fPlayer.getUuid(), inventory);

        packetSender.send(fPlayer, inventory.getWrapperWindow());
        packetSender.send(fPlayer, inventory.getWrapperItems());
    }

    public void click(Inventory inventory, int slot) {
        if (!inventory.getClickConsumerMap().containsKey(slot)) return;

        ItemStack itemStack = inventory.getWrapperItems().getItems().get(slot);

        inventory.getClickConsumerMap().get(slot).accept(itemStack, inventory);
    }

    @Async
    public void process(UUID uuid, WrapperPlayClientClickWindow wrapper) {
        Inventory inventory = inventoryMap.get(uuid);

        ClickType clickType = getClickType(wrapper);

        boolean isWindowClicked = isWindowClick(inventory, clickType, wrapper);

        if (isWindowClicked || clickType == ClickType.PICKUP) {
            packetSender.send(uuid, new WrapperPlayServerWindowItems(wrapper.getWindowId(), 0, inventory.getWrapperItems().getItems(), null));

            if (isWindowClicked) {
                click(inventory, wrapper.getSlot());
            }
        }

        platformPlayerAdapter.updateInventory(uuid);
    }

    public void changeItem(FPlayer fPlayer, Inventory inventory, int slot, ItemStack newItemStack) {
        List<ItemStack> itemStacks = inventory.getWrapperItems().getItems();
        itemStacks.set(slot, newItemStack);

        WrapperPlayServerWindowItems wrapper = inventory.getWrapperItems();
        wrapper.setItems(itemStacks);

        inventory.setWrapperItems(wrapper);

        packetSender.send(fPlayer, wrapper);
    }

    public ClickType getClickType(WrapperPlayClientClickWindow wrapper) {
        return switch (wrapper.getWindowClickType()) {
            case PICKUP -> wrapper.getCarriedItemStack() != ItemStack.EMPTY
                    ? ClickType.PICKUP
                    : ClickType.PLACE;

            case QUICK_MOVE -> ClickType.SHIFT_CLICK;

            case SWAP -> switch (wrapper.getButton()) {
                case 0, 1, 2, 3, 4, 5, 6, 7, 8, 40 -> ClickType.PICKUP;
                default -> ClickType.PLACE;
            };

            case CLONE, THROW -> ClickType.PICKUP;

            case QUICK_CRAFT -> switch (wrapper.getButton()) {
                case 0, 4, 8 -> ClickType.DRAG_START;
                case 1, 5, 9 -> ClickType.DRAG_ADD;
                case 2, 6, 10 -> ClickType.DRAG_END;
                default -> ClickType.UNDEFINED;
            };

            case PICKUP_ALL -> ClickType.PICKUP_ALL;
            default -> ClickType.UNDEFINED;
        };
    }

    public boolean isWindowClick(Inventory inventory, ClickType clickType, WrapperPlayClientClickWindow wrapper) {
        return switch (clickType) {
            case SHIFT_CLICK -> true;
            case PICKUP, PLACE -> wrapper.getSlot() >= 0 && wrapper.getSlot() <= inventory.getSize() - 1;
            case DRAG_END, PICKUP_ALL -> wrapper.getSlot() >= 0 && wrapper.getSlot() <= inventory.getSize() - 1
                    || wrapper.getSlots().orElse(new HashMap<>()).keySet().stream().anyMatch(integer -> integer == wrapper.getSlot());
            default -> false;
        };
    }
}
