package net.flectone.pulse.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class FEntity {

    protected static UUID unknownUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    protected static String unknownName = "UNKNOWN_FLECTONEPULSE";

    private final String name;
    private final UUID uuid;
    private final String type;

    public FEntity(String name, UUID uuid, String type) {
        this.name = name == null ? unknownName : name;
        this.type = type;
        this.uuid = uuid;
    }

    public boolean equals(FEntity fEntity) {
        return this.uuid.equals(fEntity.getUuid());
    }

    public boolean isUnknown() {
        return this.uuid.equals(unknownUUID);
    }
}
