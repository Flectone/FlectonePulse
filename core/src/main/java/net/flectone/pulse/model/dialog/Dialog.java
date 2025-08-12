package net.flectone.pulse.model.dialog;

import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.MultiActionDialog;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class Dialog {

    private final Map<String, Consumer<Dialog>> clickConsumerMap = new HashMap<>();
    private final List<Consumer<Dialog>> closeConsumerList = new ArrayList<>();

    @Setter private WrapperPlayServerShowDialog wrapperDialog;

    public Dialog(Map<String, Consumer<Dialog>> clickConsumerMap,
                  List<Consumer<Dialog>> closeConsumerList,
                  WrapperPlayServerShowDialog wrapperDialog) {
        this.clickConsumerMap.putAll(clickConsumerMap);
        this.closeConsumerList.addAll(closeConsumerList);
        this.wrapperDialog = wrapperDialog;
    }

    public static class Builder {

        private final CommonDialogData commonDialogData;
        private final int columns;
        private final Map<Integer, ActionButton> buttonMap = new HashMap<>();
        private final Map<String, Consumer<Dialog>> clickConsumerMap = new HashMap<>();
        private final List<Consumer<Dialog>> closeConsumerList = new ArrayList<>();


        public Builder(CommonDialogData commonDialogData, int columns) {
            this.commonDialogData = commonDialogData;
            this.columns = columns;
        }

        public Builder addButton(int slot, ActionButton actionButton) {
            buttonMap.put(slot, actionButton);
            return this;
        }

        public Builder addClickHandler(String id, Consumer<Dialog> consumer) {
            clickConsumerMap.put(id, consumer);
            return this;
        }

        public Builder addCloseConsumer(Consumer<Dialog> consumer) {
            closeConsumerList.add(consumer);
            return this;
        }

        public Dialog build() {
            List<ActionButton> buttons = buttonMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .toList();

            MultiActionDialog multiActionDialog = new MultiActionDialog(commonDialogData, buttons, null, columns);
            WrapperPlayServerShowDialog wrapperPlayServerShowDialog = new WrapperPlayServerShowDialog(multiActionDialog);

            return new Dialog(clickConsumerMap, closeConsumerList, wrapperPlayServerShowDialog);
        }
    }

}
