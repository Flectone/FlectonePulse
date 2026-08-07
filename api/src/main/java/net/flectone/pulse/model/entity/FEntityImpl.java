package net.flectone.pulse.model.entity;

import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A non-player message sender.
 *
 * @param name the display name
 * @param uuid the entity id
 * @param type the entity type
 * @param showEntityName the name shown on hover, or null to use the plain name
 */
@Builder
public record FEntityImpl(
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