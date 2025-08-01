package net.flectone.pulse.model.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class PacketEntity {

    private final int id;
    private final EntityType entityType;

    @Setter private boolean created;

    protected PacketEntity(int id, EntityType entityType) {
        this.id = id;
        this.entityType = entityType;
    }
}
