package net.flectone.pulse.config.setting;

/**
 * Configuration interface for violation settings.
 * <p>
 * Defines the threshold and reset timing for violation tracking.
 *
 * @author TheFaser
 * @since 1.9.4
 */
public interface ViolationSetting {

    /**
     * Gets the maximum number of violations allowed before triggering an action.
     *
     * @return the violation limit threshold
     */
    Integer violationLimit();

    /**
     * Gets the time period (in ticks) after which violation should be reset.
     *
     * @return the violation reset time in seconds
     */
    Long violationResetTime();

}

