package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.InventoryManager;
import net.flectone.pulse.model.inventory.Inventory;

@Singleton
public class FInventoryPacketListener extends AbstractPacketListener {

    private final InventoryManager inventoryManager;

    @Inject
    public FInventoryPacketListener(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType != PacketType.Play.Client.CLOSE_WINDOW
                && packetType != PacketType.Play.Client.CLICK_WINDOW) return;

        User user = event.getUser();

        if (packetType == PacketType.Play.Client.CLOSE_WINDOW) {
            inventoryManager.close(user.getUUID());
            return;
        }

        Inventory inventory = inventoryManager.get(user.getUUID());
        if (inventory == null) return;

        event.setCancelled(true);

        inventoryManager.process(event.getUser().getUUID(), new WrapperPlayClientClickWindow(event));
    }
}