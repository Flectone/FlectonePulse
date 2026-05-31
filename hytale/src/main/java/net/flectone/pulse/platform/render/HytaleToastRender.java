package net.flectone.pulse.platform.render;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Toast;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.processing.serializer.HytaleComponentSerializer;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleToastRender implements ToastRender {

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final HytaleComponentSerializer componentSerializer;

    @Override
    public void render(FPlayer fPlayer, Component title, Component description, Toast toast) {
        Object object = platformPlayerAdapter.convertToPlatformPlayer(fPlayer);
        if (!(object instanceof PlayerRef playerRef)) return;

        PacketHandler packetHandler = playerRef.getPacketHandler();

        ItemWithAllMetadata icon = new ItemStack(toast.icon(), 1).toPacket();
        NotificationUtil.sendNotification(packetHandler,
                componentSerializer.toHytale(title),
                Component.IS_NOT_EMPTY.test(description) ? componentSerializer.toHytale(description) : null,
                icon
        );
    }

}
