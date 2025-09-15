package net.flectone.pulse.model.entity;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
public class FEntity {

    public static final UUID UNKNOWN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final String UNKNOWN_NAME = "UNKNOWN_FLECTONEPULSE";

    private final String name;

    @Nullable
    @Setter
    private Component showEntityName;

    private final UUID uuid;
    private final String type;

    public FEntity(String name, UUID uuid, String type) {
        this.name = name == null ? UNKNOWN_NAME : name;
        this.type = type;
        this.uuid = uuid;
    }

    public boolean equals(FEntity fEntity) {
        return this.uuid.equals(fEntity.getUuid());
    }

    public boolean isUnknown() {
        return this.uuid.equals(UNKNOWN_UUID);
    }
}
