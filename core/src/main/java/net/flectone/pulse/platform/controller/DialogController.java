package net.flectone.pulse.platform.controller;

import com.github.retrooper.packetevents.protocol.dialog.MultiActionDialog;
import com.github.retrooper.packetevents.protocol.dialog.action.DynamicCustomAction;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerClearDialog;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.model.dialog.Dialog;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.sender.PacketSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DialogController {

    private final Map<UUID, Dialog> dialogMap = new ConcurrentHashMap<>();

    private final PacketSender packetSender;

    @Inject
    public DialogController(PacketSender packetSender) {
        this.packetSender = packetSender;
    }

    public Dialog get(UUID uuid) {
        return dialogMap.get(uuid);
    }

    public void close(UUID uuid) {
        Dialog dialog = dialogMap.get(uuid);
        if (dialog == null) return;

        dialog.getCloseConsumerList().forEach(closeConsumer -> closeConsumer.accept(dialog));
        dialogMap.remove(uuid);
    }

    public void closeAll() {
        WrapperPlayServerClearDialog wrapper = new WrapperPlayServerClearDialog();
        dialogMap.keySet().forEach(uuid -> packetSender.send(uuid, wrapper));
        dialogMap.clear();
    }

    public void open(FPlayer fPlayer, Dialog dialog, boolean reopen) {
        if (reopen) {
            Dialog oldDialog = dialogMap.get(fPlayer.getUuid());
            if (oldDialog != null) {
                oldDialog.getCloseConsumerList().forEach(closeConsumer -> closeConsumer.accept(oldDialog));
            }
        }

        dialogMap.put(fPlayer.getUuid(), dialog);

        packetSender.send(fPlayer, dialog.getWrapperDialog());
    }

    public void click(Dialog dialog, String key, NBT payload) {
        if (!dialog.getClickConsumerMap().containsKey(key)) return;

        dialog.getClickConsumerMap().get(key).accept(dialog, payload);
    }


    @Async
    public void process(UUID uuid, String key, NBT payload) {
        Dialog dialog = get(uuid);
        if (dialog == null) return;

        click(dialog, key, payload);
    }

    public void changeButton(FPlayer fPlayer, Dialog dialog, String id, ActionButton actionButton) {
        WrapperPlayServerShowDialog showDialog = dialog.getWrapperDialog();
        if (showDialog == null || !(showDialog.getDialog() instanceof MultiActionDialog multiActionDialog)) return;

        List<ActionButton> actionButtons = new ArrayList<>(multiActionDialog.getActions());
        for (int i = 0; i < actionButtons.size(); i++) {
            ActionButton button = actionButtons.get(i);
            if (button.getAction() instanceof DynamicCustomAction dynamicCustomAction
                    && dynamicCustomAction.getId().getKey().equals(id)) {
                actionButtons.set(i, actionButton);
                break;
            }
        }

        MultiActionDialog newMultiActionDialog = new MultiActionDialog(
                multiActionDialog.getCommon(),
                actionButtons,
                multiActionDialog.getExitAction(),
                multiActionDialog.getColumns()
        );

        dialog.setWrapperDialog(new WrapperPlayServerShowDialog(newMultiActionDialog));

        open(fPlayer, dialog, true);
    }

}
