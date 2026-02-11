package net.flectone.pulse.platform.adapter;

import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Platform adapter for player-related operations in FlectonePulse.
 * Abstracts platform-specific player APIs for cross-platform compatibility.
 *
 * @author TheFaser
 * @since 0.8.1
 */
public interface PlatformPlayerAdapter {

    /**
     * Player coordinates in the world.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     */
    record Coordinates(double x, double y, double z, float yaw, float pitch) {}

    /**
     * Player statistics.
     *
     * @param health the player health
     * @param armor the player armor value
     * @param level the player experience level
     * @param food the player food level
     * @param damage the player damage value
     */
    record Statistics(double health, double armor, double level, double food, double damage) {}

    /**
     * Player with played time information.
     *
     * @param name the player name
     * @param playedTime the total played time in milliseconds
     */
    record PlayedTimePlayer(String name, long playedTime) {}

    /**
     * Gets the entity ID of a player.
     *
     * @param uuid the player UUID
     * @return the entity ID
     */
    int getEntityId(@NonNull UUID uuid);

    default int getEntityId(@NonNull FEntity entity) {
        return getEntityId(entity.uuid());
    }

    /**
     * Gets the player UUID by entity ID.
     *
     * @param entityId the entity ID
     * @return the player UUID, or null if not found
     */
    @Nullable UUID getPlayerByEntityId(int entityId);

    /**
     * Gets the UUID from a platform player object.
     *
     * @param platformPlayer the platform player object
     * @return the player UUID, or null if conversion fails
     */
    @Nullable UUID getUUID(@NonNull Object platformPlayer);

    /**
     * Gets the platform player class.
     *
     * @return the player class, or null if not available
     */
    @Nullable Class<?> getPlayerClass();

    /**
     * Converts a UUID to a platform player object.
     *
     * @param uuid the player UUID
     * @return the platform player object, or null if not found
     */
    @Nullable Object convertToPlatformPlayer(@NonNull UUID uuid);

    default @Nullable Object convertToPlatformPlayer(@NonNull FEntity entity) {
        return convertToPlatformPlayer(entity.uuid());
    }

    /**
     * Gets the player name by UUID.
     *
     * @param uuid the player UUID
     * @return the player name
     */
    @NonNull String getName(@NonNull UUID uuid);

    default @NonNull String getName(@NonNull FEntity entity) {
        return getName(entity.uuid());
    }

    /**
     * Gets the player name from a platform player object.
     *
     * @param platformPlayer the platform player object
     * @return the player name
     */
    @NonNull String getName(@NonNull Object platformPlayer);

    int getPing(FPlayer fPlayer);

    /**
     * Gets the world name where the player is located.
     *
     * @param uuid the player UUID
     * @return the world name
     */
    @NonNull String getWorldName(@NonNull UUID uuid);

    default @NonNull String getWorldName(@NonNull FEntity entity) {
        return getWorldName(entity.uuid());
    }

    /**
     * Gets the world environment where the player is located.
     *
     * @param uuid the player UUID
     * @return the world environment
     */
    @NonNull String getWorldEnvironment(@NonNull UUID uuid);

    default @NonNull String getWorldEnvironment(@NonNull FEntity entity) {
        return getWorldEnvironment(entity.uuid());
    }

    /**
     * Gets the player IP address.
     *
     * @param uuid the player UUID
     * @return the IP address, or null if not available
     */
    @Nullable String getIp(@NonNull UUID uuid);

    default @Nullable String getIp(@NonNull FEntity entity) {
        return getIp(entity.uuid());
    }

    /**
     * Gets the entity translation key for a player.
     *
     * @param platformPlayer the platform player object
     * @return the translation key
     */
    @NonNull String getEntityTranslationKey(@Nullable Object platformPlayer);

    /**
     * Gets the player head texture properties.
     *
     * @param uuid the player UUID
     * @return the texture properties, or null if not available
     */
    PlayerHeadObjectContents.@Nullable ProfileProperty getTexture(@NonNull UUID uuid);

    default PlayerHeadObjectContents.@Nullable ProfileProperty getTexture(@NonNull FEntity entity) {
        return getTexture(entity.uuid());
    }

    /**
     * Gets the player game mode.
     *
     * @param uuid the player UUID
     * @return the game mode
     */
    @NonNull String getGamemode(@NonNull UUID uuid);

    default @NonNull String getGamemode(@NonNull FEntity entity) {
        return getGamemode(entity.uuid());
    }

    /**
     * Gets the player list header.
     *
     * @param fPlayer the player
     * @return the header component
     */
    @NonNull Component getPlayerListHeader(@NonNull FPlayer fPlayer);

    /**
     * Gets the player list footer.
     *
     * @param fPlayer the player
     * @return the footer component
     */
    @NonNull Component getPlayerListFooter(@NonNull FPlayer fPlayer);

    /**
     * Gets player statistics.
     *
     * @param uuid the player UUID
     * @return the statistics, or null if not available
     */
    @Nullable Statistics getStatistics(@NonNull UUID uuid);

    default @Nullable Statistics getStatistics(@NonNull FEntity entity) {
        return getStatistics(entity.uuid());
    }

    /**
     * Gets player coordinates.
     *
     * @param uuid the player UUID
     * @return the coordinates, or null if not available
     */
    @Nullable Coordinates getCoordinates(@NonNull UUID uuid);

    default @Nullable Coordinates getCoordinates(@NonNull FEntity entity) {
        return getCoordinates(entity.uuid());
    }

    /**
     * Calculates distance between two players.
     *
     * @param first the first player UUID
     * @param second the second player UUID
     * @return the distance
     */
    double distance(@NonNull UUID first, @NonNull UUID second);

    default double distance(@NonNull FEntity first, @NonNull FEntity second) {
        return distance(first.uuid(), second.uuid());
    }

    /**
     * Checks if the object represents the console.
     *
     * @param platformPlayer the platform player object
     * @return true if console
     */
    boolean isConsole(@NonNull Object platformPlayer);

    /**
     * Checks if the player is an operator.
     *
     * @param uuid the player UUID
     * @return true if operator
     */
    boolean isOperator(@NonNull UUID uuid);

    default boolean isOperator(@NonNull FEntity entity) {
        return isOperator(entity.uuid());
    }

    /**
     * Checks if the player is sneaking.
     *
     * @param uuid the player UUID
     * @return true if sneaking
     */
    boolean isSneaking(@NonNull UUID uuid);

    default boolean isSneaking(@NonNull FEntity entity) {
        return isSneaking(entity.uuid());
    }

    /**
     * Checks if the player has played before.
     *
     * @param uuid the player UUID
     * @return true if has played before
     */
    boolean hasPlayedBefore(@NonNull UUID uuid);

    default boolean hasPlayedBefore(@NonNull FEntity entity) {
        return hasPlayedBefore(entity.uuid());
    }

    /**
     * Checks if the player has a potion effect.
     *
     * @param uuid the player UUID
     * @param potionType the potion type
     * @return true if has the potion effect
     */
    boolean hasPotionEffect(@NonNull UUID uuid, @NonNull String potionType);

    default boolean hasPotionEffect(@NonNull FEntity entity, @NonNull String potionType) {
        return hasPotionEffect(entity.uuid(), potionType);
    }

    /**
     * Checks if the player is online.
     *
     * @param uuid the player UUID
     * @return true if online
     */
    boolean isOnline(@NonNull UUID uuid);

    default boolean isOnline(@NonNull FEntity entity) {
        return isOnline(entity.uuid());
    }

    /**
     * Gets the first played timestamp.
     *
     * @param uuid the player UUID
     * @return the first played timestamp
     */
    long getFirstPlayed(@NonNull UUID uuid);

    default long getFirstPlayed(@NonNull FEntity entity) {
        return getFirstPlayed(entity.uuid());
    }

    /**
     * Gets the last played timestamp.
     *
     * @param uuid the player UUID
     * @return the last played timestamp
     */
    long getLastPlayed(@NonNull UUID uuid);

    default long getLastPlayed(@NonNull FEntity entity) {
        return getLastPlayed(entity.uuid());
    }

    /**
     * Gets the total played time.
     *
     * @param uuid the player UUID
     * @return the total played time in milliseconds
     */
    long getAllTimePlayed(@NonNull UUID uuid);

    default long getAllTimePlayed(@NonNull FEntity entity) {
        return getAllTimePlayed(entity.uuid());
    }

    /**
     * Updates the player's inventory.
     *
     * @param uuid the player UUID
     */
    void updateInventory(@NonNull UUID uuid);

    default void updateInventory(@NonNull FEntity entity) {
        updateInventory(entity.uuid());
    }

    /**
     * Gets the item in the player's hand.
     *
     * @param uuid the player UUID
     * @return the item, or null if empty
     */
    @Nullable Object getItem(@NonNull UUID uuid);

    default @Nullable Object getItem(@NonNull FEntity entity) {
        return getItem(entity.uuid());
    }

    /**
     * Gets all online player UUIDs.
     *
     * @return list of online player UUIDs
     */
    @NonNull List<UUID> getOnlinePlayers();

    /**
     * Finds players who can see a location.
     *
     * @param uuid the source player UUID
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return set of player UUIDs who can see the location
     */
    @NonNull Set<UUID> findPlayersWhoCanSee(UUID uuid, double x, double y, double z);

    default @NonNull Set<UUID> findPlayersWhoCanSee(@NonNull FEntity entity, double x, double y, double z) {
        return findPlayersWhoCanSee(entity.uuid(), x, y, z);
    }

    /**
     * Gets passengers of a player's vehicle.
     *
     * @param uuid the player UUID
     * @return list of passenger entity IDs
     */
    @NonNull List<Integer> getPassengers(UUID uuid);

    default @NonNull List<Integer> getPassengers(@NonNull FEntity entity) {
        return getPassengers(entity.uuid());
    }

    /**
     * Gets all players with their played time.
     *
     * @return list of players with played time
     */
    @NonNull List<PlayedTimePlayer> getPlayedTimePlayers();

    void kick(FPlayer fPlayer, Component reason);
}