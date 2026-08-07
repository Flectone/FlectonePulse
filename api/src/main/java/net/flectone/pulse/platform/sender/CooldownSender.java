package net.flectone.pulse.platform.sender;

import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.value.Cooldown;
import net.flectone.pulse.model.value.Pair;

import java.util.Optional;

/**
 * Sends cooldown messages to players and checks cooldown bypass permissions.
 *
 * @author TheFaser
 * @since 1.6.0
 */
public interface CooldownSender {

    /**
     * Checks if an entity is on cooldown and sends a cooldown message if applicable.
     * Only sends messages to players, not other entities.
     *
     * @param entity the entity to check
     * @param optionalCooldown optional pair of cooldown and permission settings
     * @param cooldownOwner name of the owner that checks cooldown
     * @return true if cooldown message was sent, false otherwise
     */
    boolean sendIfCooldown(FEntity entity, Optional<Pair<Cooldown, PermissionSetting>> optionalCooldown, String cooldownOwner);

    /**
     * Checks if a player is on cooldown and sends a formatted cooldown message.
     *
     * @param entity the entity to check
     * @param cooldownPermission pair of cooldown settings and bypass permission
     * @param cooldownOwner name of the owner that checks cooldown
     * @return true if cooldown message was sent, false otherwise
     */
    boolean sendIfCooldown(FEntity entity, Pair<Cooldown, PermissionSetting> cooldownPermission, String cooldownOwner);

}
