package net.flectone.pulse.config.setting;

/**
 * Configuration interface for enable/disable settings.
 *
 * @author TheFaser
 * @since 1.7.1
 */
public interface EnableSetting {

    /**
     * Gets the enable state of the feature.
     *
     * @return true if enabled, false if disabled
     */
    Boolean enable();

}