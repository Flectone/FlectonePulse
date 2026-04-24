package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.inventory.MinecraftInventory;
import net.flectone.pulse.platform.controller.MinecraftInventoryController;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPacketInventoryListener implements PacketListener {

    private final MinecraftInventoryController inventoryController;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.CLOSE_WINDOW) {
            inventoryController.close(event.getUser().getUUID());
            return;
        }

        if (packetType == PacketType.Play.Client.CLICK_WINDOW) {
            User user = event.getUser();

            MinecraftInventory inventory = inventoryController.get(user.getUUID());
            if (inventory == null) return;

            event.setCancelled(true);

            inventoryController.process(user.getUUID(), new WrapperPlayClientClickWindow(event));
        }
    }
}