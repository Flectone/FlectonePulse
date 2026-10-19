package net.flectone.pulse.platform.render;

import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.HeadText;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Service for managing head text displays.
 *
 * @see HeadText
 *
 * @author TheFaser
 * @since 1.15.0
 */
public interface HeadTextRender {

    /**
     * Reloads the service and remove all head texts.
     */
    void reload();

    /**
     * Removes all head texts.
     */
    void removeAll();

    /**
     * Renders the head text for all viewers near the holder.
     *
     * @param moduleName the module that owns the head text
     * @param holder the player who holds the head text
     * @param viewers the players who may see the head text
     * @param component the component resolver for each viewer
     * @param headText the head text settings
     */
    void render(@NonNull ModuleName moduleName, @NonNull FPlayer holder, @NonNull Set<FPlayer> viewers, @NonNull Function<FPlayer, Component> component, @NonNull HeadText headText);

    /**
     * Renders the head text for a single viewer.
     *
     * @param moduleName the module that owns the head text
     * @param holder the player who holds the head text
     * @param viewer the player who may see the head text
     * @param component the component resolver for the viewer
     * @param headText the head text settings
     */
    void render(@NonNull ModuleName moduleName, @NonNull FPlayer holder, @NonNull FPlayer viewer, @NonNull Function<FPlayer, Component> component, @NonNull HeadText headText);

    /**
     * Renders the head text for a single viewer.
     *
     * @param moduleName the module that owns the head text
     * @param holder the player who holds the head text
     * @param viewer the player who may see the head text
     * @param component the component resolver for the viewer
     * @param headText the head text settings
     * @param holderCoordinates the holder's current coordinates
     * @param holderWorld the holder's current world name
     */
    void render(@NonNull ModuleName moduleName, @NonNull FPlayer holder, @NonNull FPlayer viewer, @NonNull Function<FPlayer, Component> component, @NonNull HeadText headText, PlatformPlayerAdapter.@NonNull Coordinates holderCoordinates, @NonNull String holderWorld);

    /**
     * Removes the head text for the specified holder from all viewers.
     *
     * @param holder the UUID of the holder
     */
    void remove(@NonNull UUID holder);

    /**
     * Removes the head text entities for the specified holder and viewer that are no longer within range.
     *
     * @param holder the UUID of the holder
     * @param viewer the UUID of the viewer
     */
    void remove(@NonNull UUID holder, @NonNull UUID viewer);

    /**
     * Sets platform-specific passenger entity IDs to ride the holder alongside head text displays.
     * Passing null clears the stored passengers.
     *
     * @param holder the UUID of the holder
     * @param entities the passenger entity IDs, or null to clear them
     */
    void setPassengers(@NonNull UUID holder, int[] entities);

    /**
     * Checks if the specified UUID is a valid holder.
     * A holder is valid when alive, not in spectator mode, and not invisible.
     *
     * @param holder the UUID to check
     * @return true if the holder is valid, false otherwise
     */
    boolean isCorrectHolder(@NonNull UUID holder);

}