package net.flectone.pulse.platform.controller;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.inventory.MinecraftInventoryClickType;
import net.flectone.pulse.model.inventory.MinecraftInventory;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftInventoryController {

    private final Map<UUID, MinecraftInventory> inventoryMap = new ConcurrentHashMap<>();

    private final MinecraftPacketSender packetSender;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final TaskScheduler taskScheduler;

    public MinecraftInventory get(UUID uuid) {
        return inventoryMap.get(uuid);
    }

    public void close(UUID uuid) {
        MinecraftInventory inventory = inventoryMap.get(uuid);
        if (inventory == null) return;

        inventory.getCloseConsumerList().forEach(closeConsumer -> closeConsumer.accept(inventory));
        inventoryMap.remove(uuid);
    }

    public void closeAll() {
        WrapperPlayServerCloseWindow wrapper = new WrapperPlayServerCloseWindow();
        inventoryMap.keySet().forEach(uuid -> packetSender.send(uuid, wrapper));
        inventoryMap.clear();
    }

    public void open(FPlayer fPlayer, MinecraftInventory inventory) {
        inventoryMap.put(fPlayer.uuid(), inventory);

        packetSender.send(fPlayer, inventory.getWrapperWindow());
        packetSender.send(fPlayer, inventory.getWrapperItems());
    }

    public void click(MinecraftInventory inventory, int slot) {
        if (!inventory.getClickConsumerMap().containsKey(slot)) return;

        ItemStack itemStack = inventory.getWrapperItems().getItems().get(slot);

        inventory.getClickConsumerMap().get(slot).accept(itemStack, inventory);
    }

    public void process(UUID uuid, WrapperPlayClientClickWindow wrapper) {
        taskScheduler.runAsync(() -> {
            MinecraftInventory inventory = inventoryMap.get(uuid);

            MinecraftInventoryClickType clickType = getClickType(wrapper);

            boolean isWindowClicked = isWindowClick(inventory, clickType, wrapper);

            if (isWindowClicked || clickType == MinecraftInventoryClickType.PICKUP) {
                packetSender.send(uuid, new WrapperPlayServerWindowItems(wrapper.getWindowId(), 0, inventory.getWrapperItems().getItems(), null));

                if (isWindowClicked) {
                    click(inventory, wrapper.getSlot());
                }
            }

            platformPlayerAdapter.updateInventory(uuid);
        });
    }

    public void changeItem(FPlayer fPlayer, MinecraftInventory inventory, int slot, ItemStack newItemStack) {
        List<ItemStack> itemStacks = inventory.getWrapperItems().getItems();
        itemStacks.set(slot, newItemStack);

        WrapperPlayServerWindowItems wrapper = inventory.getWrapperItems();
        wrapper.setItems(itemStacks);

        inventory.setWrapperItems(wrapper);

        packetSender.send(fPlayer, wrapper);
    }

    public MinecraftInventoryClickType getClickType(WrapperPlayClientClickWindow wrapper) {
        return switch (wrapper.getWindowClickType()) {
            case PICKUP -> wrapper.getCarriedItemStack() != ItemStack.EMPTY
                    ? MinecraftInventoryClickType.PICKUP
                    : MinecraftInventoryClickType.PLACE;

            case QUICK_MOVE -> MinecraftInventoryClickType.SHIFT_CLICK;

            case SWAP -> switch (wrapper.getButton()) {
                case 0, 1, 2, 3, 4, 5, 6, 7, 8, 40 -> MinecraftInventoryClickType.PICKUP;
                default -> MinecraftInventoryClickType.PLACE;
            };

            case CLONE, THROW -> MinecraftInventoryClickType.PICKUP;

            case QUICK_CRAFT -> switch (wrapper.getButton()) {
                case 0, 4, 8 -> MinecraftInventoryClickType.DRAG_START;
                case 1, 5, 9 -> MinecraftInventoryClickType.DRAG_ADD;
                case 2, 6, 10 -> MinecraftInventoryClickType.DRAG_END;
                default -> MinecraftInventoryClickType.UNDEFINED;
            };

            case PICKUP_ALL -> MinecraftInventoryClickType.PICKUP_ALL;
            default -> MinecraftInventoryClickType.UNDEFINED;
        };
    }

    public boolean isWindowClick(MinecraftInventory inventory, MinecraftInventoryClickType clickType, WrapperPlayClientClickWindow wrapper) {
        return switch (clickType) {
            case SHIFT_CLICK -> true;
            case PICKUP, PLACE -> wrapper.getSlot() >= 0 && wrapper.getSlot() <= inventory.getSize() - 1;
            case DRAG_END, PICKUP_ALL -> wrapper.getSlot() >= 0 && wrapper.getSlot() <= inventory.getSize() - 1
                    || wrapper.getSlots().orElse(new HashMap<>()).keySet().stream().anyMatch(integer -> integer == wrapper.getSlot());
            default -> false;
        };
    }
}
