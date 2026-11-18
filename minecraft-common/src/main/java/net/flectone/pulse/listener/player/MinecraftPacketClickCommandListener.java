package net.flectone.pulse.listener.player;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCustomClickAction;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPacketClickCommandListener implements PacketListener {

    private static final ResourceLocation KEY = new ResourceLocation("flectonepulse", "command");
    private static final String COMMAND = "command";

    private final PlatformServerAdapter platformServerAdapter;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CUSTOM_CLICK_ACTION) return;

        UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;

        NBT payload;
        try {
            WrapperPlayClientCustomClickAction wrapper = new WrapperPlayClientCustomClickAction(event);
            if (!KEY.equals(wrapper.getId())) return;

            payload = wrapper.getPayload();
        } catch (Exception _) {
            return;
        }

        if (!(payload instanceof NBTCompound compound)) return;

        NBTString commandTag = compound.getStringTagOrNull(COMMAND);
        if (commandTag == null) return;

        String command = commandTag.getValue();
        if (command.isEmpty()) return;
        if (command.charAt(0) == '/') {
            command = command.substring(1);
        }

        platformServerAdapter.dispatchCommand(uuid, command);
    }

}
