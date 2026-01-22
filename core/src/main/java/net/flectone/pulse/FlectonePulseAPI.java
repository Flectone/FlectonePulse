package net.flectone.pulse;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;

/**
 * API entry point for FlectonePulse plugin integration.
 * Provides static access to the main {@link FlectonePulse} instance and lifecycle management.
 *
 * @see FlectonePulse
 * @since 0.1.0
 */
@Singleton
public class FlectonePulseAPI  {

    /**
     * The main instance of the FlectonePulse.
     * Provides access to dependency injection and functionality.
     *
     * @see FlectonePulse
     */
    @Getter 
    private static FlectonePulse instance;

    /**
     * Constructs the API wrapper with dependency injection.
     * This constructor is called internally by Google Guice.
     *
     * @param instance the main FlectonePulse implementation instance
     */
    @Inject
    public FlectonePulseAPI(FlectonePulse instance) {
        FlectonePulseAPI.instance = instance;
    }

}
