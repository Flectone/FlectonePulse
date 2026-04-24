package net.flectone.pulse.model.inventory;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class MinecraftInventory {

    private final int size;
    private final Int2ObjectOpenHashMap<BiConsumer<ItemStack, MinecraftInventory>> clickConsumerMap = new Int2ObjectOpenHashMap<>();
    private final List<Consumer<MinecraftInventory>> closeConsumerList = new ObjectArrayList<>();
    private final WrapperPlayServerOpenWindow wrapperWindow;

    @Setter private WrapperPlayServerWindowItems wrapperItems;

    public MinecraftInventory(int size,
                              Int2ObjectOpenHashMap<BiConsumer<ItemStack, MinecraftInventory>> clickConsumerMap,
                              List<Consumer<MinecraftInventory>> closeConsumerList,
                              WrapperPlayServerOpenWindow wrapperWindow,
                              WrapperPlayServerWindowItems wrapperItems) {
        this.size = size;
        this.clickConsumerMap.putAll(clickConsumerMap);
        this.closeConsumerList.addAll(closeConsumerList);
        this.wrapperWindow = wrapperWindow;
        this.wrapperItems = wrapperItems;
    }

    public static class Builder {

        private final Int2ObjectOpenHashMap<ItemStack> itemMap = new Int2ObjectOpenHashMap<>();
        private final Int2ObjectOpenHashMap<BiConsumer<ItemStack, MinecraftInventory>> clickConsumerMap = new Int2ObjectOpenHashMap<>();
        private final List<Consumer<MinecraftInventory>> closeConsumerList = new ObjectArrayList<>();
        private Component name = Component.empty();
        private int size;

        public Builder name(Component name) {
            this.name = name;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder addItem(int index, ItemStack itemStack) {
            itemMap.put(index, itemStack);
            return this;
        }

        public Builder addClickHandler(int index, BiConsumer<ItemStack, MinecraftInventory> consumer) {
            clickConsumerMap.put(index, consumer);
            return this;
        }

        public Builder addCloseConsumer(Consumer<MinecraftInventory> consumer) {
            closeConsumerList.add(consumer);
            return this;
        }

        public MinecraftInventory build(boolean modern) {
            WrapperPlayServerOpenWindow wrapperWindow = modern
                    ? new WrapperPlayServerOpenWindow(126, size >= 24 ? 5 : size, name)
                    : new WrapperPlayServerOpenWindow(126, "chest", name, size, 0);

            List<ItemStack> items = new ObjectArrayList<>(size);
            for (int i = 0; i < size; i++) {
                items.add(itemMap.getOrDefault(i, ItemStack.EMPTY));
            }

            WrapperPlayServerWindowItems wrapperItems = new WrapperPlayServerWindowItems(126, 0, items, null);

            return new MinecraftInventory(size, clickConsumerMap, closeConsumerList, wrapperWindow, wrapperItems);
        }
    }
}
