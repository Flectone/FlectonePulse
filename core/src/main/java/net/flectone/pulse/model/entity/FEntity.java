package net.flectone.pulse.model.entity;

import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface FEntity {

    UUID UNKNOWN_UUID = new UUID(0, 0);

    String UNKNOWN_NAME = "UNKNOWN_FLECTONEPULSE";

    String UNKNOWN_TYPE = "UNKNOWN";

    String name();

    UUID uuid();

    String type();

    @Nullable Component showEntityName();

    static FEntityImpl.FEntityImplBuilder builder() {
        return FEntityImpl.builder();
    }

    static FEntity unknown() {
        return FEntityImpl.builder().build();
    }

    default boolean isUnknown() {
        return uuid().equals(UNKNOWN_UUID);
    }

    @Builder
    record FEntityImpl(
            String name,
            UUID uuid,
            String type,
            @Nullable Component showEntityName
    ) implements FEntity {

        public FEntityImpl {
            if (uuid == null) uuid = UNKNOWN_UUID;
            if (name == null) name = UNKNOWN_NAME;
            if (type == null) type = UNKNOWN_TYPE;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof FEntity fEntity)) return false;

            return this.uuid.equals(fEntity.uuid());
        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }

    }
}
