package net.flectone.pulse.model.inventory;

import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.MultiActionDialog;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class MinecraftDialog {

    private final Map<String, BiConsumer<MinecraftDialog, NBT>> clickConsumerMap = new Object2ObjectOpenHashMap<>();
    private final List<Consumer<MinecraftDialog>> closeConsumerList = new ObjectArrayList<>();

    @Setter private WrapperPlayServerShowDialog wrapperDialog;

    public MinecraftDialog(Map<String, BiConsumer<MinecraftDialog, NBT>> clickConsumerMap,
                           List<Consumer<MinecraftDialog>> closeConsumerList,
                           WrapperPlayServerShowDialog wrapperDialog) {
        this.clickConsumerMap.putAll(clickConsumerMap);
        this.closeConsumerList.addAll(closeConsumerList);
        this.wrapperDialog = wrapperDialog;
    }

    public static class Builder {

        private final CommonDialogData commonDialogData;
        private final int columns;
        private final Int2ObjectOpenHashMap<ActionButton> buttonMap = new Int2ObjectOpenHashMap<>();
        private final Map<String, BiConsumer<MinecraftDialog, NBT>> clickConsumerMap = new Object2ObjectOpenHashMap<>();
        private final List<Consumer<MinecraftDialog>> closeConsumerList = new ObjectArrayList<>();

        public Builder(CommonDialogData commonDialogData, int columns) {
            this.commonDialogData = commonDialogData;
            this.columns = columns;
        }

        public Builder addButton(int slot, ActionButton actionButton) {
            buttonMap.put(slot, actionButton);
            return this;
        }

        public Builder addClickHandler(String id, Consumer<MinecraftDialog> consumer) {
            clickConsumerMap.put(id, (dialog, _) -> consumer.accept(dialog));
            return this;
        }

        public Builder addClickHandler(String id, BiConsumer<MinecraftDialog, NBT> consumer) {
            clickConsumerMap.put(id, consumer);
            return this;
        }

        public Builder addCloseConsumer(Consumer<MinecraftDialog> consumer) {
            closeConsumerList.add(consumer);
            return this;
        }

        public MinecraftDialog build() {
            List<ActionButton> buttons = buttonMap.int2ObjectEntrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .toList();

            MultiActionDialog multiActionDialog = new MultiActionDialog(commonDialogData, buttons, null, columns);
            WrapperPlayServerShowDialog wrapperPlayServerShowDialog = new WrapperPlayServerShowDialog(multiActionDialog);

            return new MinecraftDialog(clickConsumerMap, closeConsumerList, wrapperPlayServerShowDialog);
        }
    }

}
