package net.flectone.pulse.service;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * Remembers players that are being moved to another server by the proxy.
 *
 * @author TheFaser
 */
public interface PlayerSwitchService {

    /**
     * Marks or unmarks a player as switching to another server.
     *
     * @param uuid the player's UUID
     * @param switching true to mark the player as switching, false to clear the mark
     */
    void setSwitching(@NonNull UUID uuid, boolean switching);

    /**
     * Whether the player is currently marked as switching to another server.
     *
     * @param uuid the player's UUID
     * @return true if the player is switching
     */
    boolean isSwitching(@NonNull UUID uuid);

}
