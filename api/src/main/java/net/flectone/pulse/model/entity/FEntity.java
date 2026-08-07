package net.flectone.pulse.model.entity;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Anything that can send a message, such as a player, the console or a command block
 * @author TheFaser
 */
public interface FEntity {

    /**
     * Starts building an entity.
     *
     * @return a new builder
     */
    static FEntityImpl.FEntityImplBuilder builder() {
        return FEntityImpl.builder();
    }

    /**
     * The stand-in used when the real sender cannot be determined.
     *
     * @return the unknown entity
     */
    static FEntity unknown() {
        return FEntityImpl.builder().build();
    }

    /**
     * The id carried by the unknown entity.
     */
    UUID UNKNOWN_UUID = new UUID(0, 0);

    /**
     * The name carried by the unknown entity.
     */
    String UNKNOWN_NAME = "UNKNOWN_FLECTONEPULSE";

    /**
     * The type carried by the unknown entity.
     */
    String UNKNOWN_TYPE = "UNKNOWN";

    /**
     * The display name.
     *
     * @return the name
     */
    String name();

    /**
     * The entity id.
     *
     * @return the id
     */
    UUID uuid();

    /**
     * The entity type.
     *
     * @return the type
     */
    String type();

    @Nullable Component showEntityName();

    /**
     * Whether this is the stand-in returned by {@link #unknown()}.
     *
     * @return true if the entity is unknown
     */
    default boolean isUnknown() {
        return uuid().equals(UNKNOWN_UUID);
    }


}
