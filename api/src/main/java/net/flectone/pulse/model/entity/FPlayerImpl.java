package net.flectone.pulse.model.entity;

import lombok.Builder;
import lombok.With;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * A player
 *
 * @param name the player name
 * @param uuid the player id
 * @param type the entity type
 * @param id the database id
 * @param online whether they are connected right now
 * @param ip their address, or null if it is not known
 * @param showEntityName the name shown on hover, or null to use the plain name
 */
@Builder(toBuilder = true)
@With
public record FPlayerImpl(
        String name,
        UUID uuid,
        String type,
        Integer id,
        boolean online,
        @Nullable String ip,
        @Nullable Component showEntityName
) implements FPlayer {

    public FPlayerImpl {
        if (name == null) name = FEntity.UNKNOWN_NAME;
        if (uuid == null) uuid = FEntity.UNKNOWN_UUID;
        if (type == null) type = PLAYER_TYPE;
        if (id == null) id = UNKNOWN_ID;
    }

    @Override
    public boolean isConsole() {
        return id == CONSOLE_ID;
    }

    @Override
    public boolean isIntegration() {
        return type.equals(INTEGRATION_TYPE);
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof FPlayer fPlayer)) return false;
        if (!Objects.equals(this.type, fPlayer.type())) return false;
        if (!Objects.equals(this.uuid, fPlayer.uuid())) return false;

        return Objects.equals(this.id, fPlayer.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, id, type);
    }

}