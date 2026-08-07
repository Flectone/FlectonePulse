package net.flectone.pulse.processing.resolver;

import net.flectone.pulse.model.entity.FEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Looks up player names and uuids, both from the running server and from Mojang.
 * @author TheFaser
 */
public interface ProfileResolver {

    /**
     * The display name of an entity, falling back to its type for anything that is not a player.
     *
     * @param entity the entity to name
     * @return the resolved name
     */
    @NonNull String resolveName(FEntity entity);

    /**
     * The name registered with Mojang for a uuid.
     *
     * @param uuid the account id
     * @return the resolved name, or an empty string if the lookup failed
     */
    @NonNull String resolveOnlineName(@NonNull UUID uuid);

    /**
     * The Mojang account id for a name.
     *
     * @param name the player name
     * @return the account id, or null if the lookup failed
     */
    @Nullable UUID resolveOnlineUUID(@NonNull String name);

    /**
     * The offline-mode id a server derives from a name, without contacting Mojang.
     *
     * @param name the player name
     * @return the derived id
     */
    @NonNull UUID resolveOfflineUUID(String name);

}
