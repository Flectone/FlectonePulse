package net.flectone.pulse.model;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.util.PacketEventsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public abstract class FPacketEntity {

    protected int id;
    protected UUID uuid;
    @Setter protected boolean alive;

    protected List<FPlayer> viewers = new ArrayList<>();

    public void sendPacketToViewers(PacketWrapper<?> packet) {
        viewers.forEach(fPlayer -> PacketEventsUtil.sendStaticPacket(fPlayer, packet));
    }
}
