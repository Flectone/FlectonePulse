package net.flectone.pulse.model.inventory;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class Inventory {

    private final int size;
    private final Map<Integer, BiConsumer<ItemStack, Inventory>> clickConsumerMap = new HashMap<>();
    private final List<Consumer<Inventory>> closeConsumerList = new ObjectArrayList<>();
    private final WrapperPlayServerOpenWindow wrapperWindow;

    @Setter private WrapperPlayServerWindowItems wrapperItems;

    public Inventory(int size,
                     Map<Integer, BiConsumer<ItemStack, Inventory>> clickConsumerMap,
                     List<Consumer<Inventory>> closeConsumerList,
                     WrapperPlayServerOpenWindow wrapperWindow,
                     WrapperPlayServerWindowItems wrapperItems) {
        this.size = size;
        this.clickConsumerMap.putAll(clickConsumerMap);
        this.closeConsumerList.addAll(closeConsumerList);
        this.wrapperWindow = wrapperWindow;
        this.wrapperItems = wrapperItems;
    }

    public static class Builder {

        private Component name = Component.empty();
        private int size;
        private final Map<Integer, ItemStack> itemMap = new HashMap<>();
        private final Map<Integer, BiConsumer<ItemStack, Inventory>> clickConsumerMap = new HashMap<>();
        private final List<Consumer<Inventory>> closeConsumerList = new ObjectArrayList<>();

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

        public Builder addClickHandler(int index, BiConsumer<ItemStack, Inventory> consumer) {
            clickConsumerMap.put(index, consumer);
            return this;
        }

        public Builder addCloseConsumer(Consumer<Inventory> consumer) {
            closeConsumerList.add(consumer);
            return this;
        }

        public Inventory build(boolean modern) {
            WrapperPlayServerOpenWindow wrapperWindow = modern
                    ? new WrapperPlayServerOpenWindow(126, size >= 24 ? 5 : size, name)
                    : new WrapperPlayServerOpenWindow(126, "chest", name, size, 0);

            List<ItemStack> items = new ObjectArrayList<>(size);
            for (int i = 0; i < size; i++) {
                items.add(itemMap.getOrDefault(i, ItemStack.EMPTY));
            }

            WrapperPlayServerWindowItems wrapperItems = new WrapperPlayServerWindowItems(126, 0, items, null);

            return new Inventory(size, clickConsumerMap, closeConsumerList, wrapperWindow, wrapperItems);
        }
    }
}
