package net.flectone.pulse.config.setting;

import net.flectone.pulse.model.util.Cooldown;

/**
 * Configuration interface for settings that include a cooldown.
 *
 * @author TheFaser
 * @since 1.7.1
 */
public interface CooldownConfigSetting {

    /**
     * Gets the cooldown configuration.
     *
     * @return the cooldown configuration
     * @see Cooldown
     */
    Cooldown cooldown();

}