package net.flectone.pulse.service;

import net.flectone.pulse.model.entity.HeadText;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * Service for managing head text displays.
 * Provides methods for showing, removing, and validating head texts by holder UUID.
 *
 * @see HeadText
 *
 * @author TheFaser
 * @since 1.15.0
 */
public interface HeadTextService {

    /**
     * Reloads the service and re-applies all head texts.
     */
    void reload();

    /**
     * Shows a head text for players
     *
     * @param entity the head text entity
     */
    void show(@NonNull HeadText entity);

    /**
     * Removes the head text for the specified holder.
     *
     * @param holder the UUID of the holder
     */
    void remove(@NonNull UUID holder);

    /**
     * Removes all head texts.
     */
    void removeAll();

    /**
     * Checks if the specified UUID is a valid holder.
     *
     * @param holder the UUID to check
     * @return true if the holder is valid, false otherwise
     */
    boolean isCorrectHolder(@NonNull UUID holder);

}