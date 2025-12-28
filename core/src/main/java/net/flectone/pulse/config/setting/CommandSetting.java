package net.flectone.pulse.config.setting;

import java.util.List;

/**
 * Configuration interface for command settings.
 * Extends {@link EnableSetting}
 *
 * @author TheFaser
 * @since 1.7.1
 */
public interface CommandSetting extends CooldownConfigSetting, SoundConfigSetting, EnableSetting {

    /**
     * Gets the list of command aliases.
     *
     * @return list of command aliases, may be empty but not null
     */
    List<String> aliases();

}