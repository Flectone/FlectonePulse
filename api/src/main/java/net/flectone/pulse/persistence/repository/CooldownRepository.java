package net.flectone.pulse.persistence.repository;

import net.flectone.pulse.model.value.Cooldown;

import java.util.UUID;

/**
 * Stores when each player's cooldowns run out. Entries live in memory and are mirrored to the
 * other servers so a cooldown cannot be dodged by switching server.
 * @author TheFaser
 */
public interface CooldownRepository {

    /**
     * How long a player must still wait, starting a new window when the old one has run out.
     *
     * @param playerUUID the player
     * @param cooldown the cooldown settings
     * @param cooldownOwner what the cooldown applies to
     * @return the remaining milliseconds, or 0 if the player may act now
     */
    long getTimeLeft(UUID playerUUID, Cooldown cooldown, String cooldownOwner);

    /**
     * Records an expiry time that arrived from another server.
     *
     * @param playerUUID the player
     * @param cooldownClass what the cooldown applies to
     * @param newExpireTime when it runs out, in milliseconds
     * @return true if the stored value changed
     */
    boolean updateCache(UUID playerUUID, String cooldownClass, long newExpireTime);

    /**
     * Tells the other servers about a cooldown that just started here.
     *
     * @param playerUUID the player
     * @param cooldownOwner what the cooldown applies to
     * @param newExpireTime when it runs out, in milliseconds
     */
    void syncProxy(UUID playerUUID, String cooldownOwner, long newExpireTime);

    /**
     * Identifies one cooldown by a player and the thing they are waiting on.
     *
     * @param playerUUID the player
     * @param cooldownClass what the cooldown applies to
     */
    record CooldownKey(UUID playerUUID, String cooldownClass) {
    }

}
