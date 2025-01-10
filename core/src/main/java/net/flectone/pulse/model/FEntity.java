package net.flectone.pulse.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class FEntity {

    protected static UUID UNKNOWN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    protected static String UNKNOWN_NAME = "UNKNOWN_FLECTONEPULSE";

    private final String name;
    private final UUID uuid;
    private final String type;

    @Setter private int entityId = -1;

    public FEntity(String name, UUID uuid, String type) {
        this.name = name == null ? UNKNOWN_NAME : name;
        this.type = type;
        this.uuid = uuid;
    }

    public boolean equals(FEntity fEntity) {
        return this.uuid.equals(fEntity.getUuid());
    }
}
