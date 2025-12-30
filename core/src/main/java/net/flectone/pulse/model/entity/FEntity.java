package net.flectone.pulse.model.entity;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Getter
public class FEntity {

    public static final UUID UNKNOWN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final String UNKNOWN_NAME = "UNKNOWN_FLECTONEPULSE";
    public static final String UNKNOWN_TYPE = "UNKNOWN";

    private final String name;

    @Setter
    @Nullable
    private Component showEntityName;

    private final UUID uuid;
    private final String type;

    public FEntity(String name, UUID uuid, String type) {
        this.name = name == null ? UNKNOWN_NAME : name;
        this.type = type;
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        FEntity fEntity = (FEntity) object;
        return this.uuid.equals(fEntity.getUuid());
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public boolean isUnknown() {
        return this.uuid.equals(UNKNOWN_UUID);
    }
}
