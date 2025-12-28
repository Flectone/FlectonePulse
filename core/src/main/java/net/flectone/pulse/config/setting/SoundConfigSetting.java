package net.flectone.pulse.config.setting;

import net.flectone.pulse.model.util.Sound;

/**
 * Configuration interface for settings that include sound.
 *
 * @author TheFaser
 * @since 1.7.1
 */
public interface SoundConfigSetting {

    /**
     * Gets the sound configuration.
     *
     * @return the sound configuration
     * @see Sound
     */
    Sound sound();

}