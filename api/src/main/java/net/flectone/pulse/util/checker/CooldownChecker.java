package net.flectone.pulse.util.checker;

import lombok.NonNull;
import net.flectone.pulse.model.util.Cooldown;

import java.util.UUID;

/**
 * Decides whether a player is still inside a cooldown window.
 * @author TheFaser
 */
public interface CooldownChecker {

    /**
     * Checks the cooldown and, when it has run out, starts a new window.
     *
     * @param playerUUID the player to check
     * @param cooldown the cooldown settings
     * @param cooldownOwner what the cooldown applies to, so separate actions do not share one timer
     * @return true if the player must still wait
     */
    boolean check(UUID playerUUID, Cooldown cooldown, @NonNull String cooldownOwner);

}
