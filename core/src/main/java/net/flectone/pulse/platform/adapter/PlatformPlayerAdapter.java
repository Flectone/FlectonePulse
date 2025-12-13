package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.world.Location;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PlatformPlayerAdapter {

    /**
     * Record representing coordinates in the world
     *
     * @param x X-axis coordinate
     * @param y Y-axis coordinate
     * @param z Z-axis coordinate
     */
    record Coordinates(double x, double y, double z) {}

    /**
     * Record representing player statistics
     *
     * @param health Current health points
     * @param armor  Current armor points
     * @param level  Experience level
     * @param food   Food level
     * @param damage Damage dealt
     */
    record Statistics(double health, double armor, double level, double food, double damage) {}

    /**
     * Represent total playtime of the player when they are offline
     *
     * @param name Name of the player
     * @param playedTime Total playtime of the player
     */
    record PlayedTimePlayer(String name, long playedTime) {}


    // Player identification methods
    int getEntityId(@NotNull UUID uuid);
    @Nullable UUID getPlayerByEntityId(int entityId);
    @Nullable UUID getUUID(@NotNull Object platformPlayer);
    @Nullable Class<?> getPlayerClass();
    @Nullable Object convertToPlatformPlayer(@NotNull FPlayer fPlayer);
    @Nullable Object convertToPlatformPlayer(@NotNull UUID uuid);

    // Player information methods
    @NotNull String getName(@NotNull UUID uuid);
    @NotNull String getName(@NotNull Object platformPlayer);
    @NotNull String getWorldName(@NotNull FPlayer fPlayer);
    @NotNull String getWorldEnvironment(@NotNull FPlayer fPlayer);
    @Nullable String getIp(@NotNull FPlayer fPlayer);
    @NotNull String getEntityTranslationKey(@Nullable Object platformPlayer);
    @Nullable PlayerHeadObjectContents.ProfileProperty getTexture(@NotNull UUID uuid);
    @NotNull GameMode getGamemode(@NotNull FPlayer fPlayer);

    // Player list methods
    @NotNull Component getPlayerListHeader(@NotNull FPlayer fPlayer);
    @NotNull Component getPlayerListFooter(@NotNull FPlayer fPlayer);

    // Objective and statistic methods
    int getObjectiveScore(@NotNull UUID uuid, @Nullable ObjectiveModule.Mode mode);
    @Nullable Statistics getStatistics(@NotNull FEntity fEntity);

    // Position and measurement methods
    @Nullable Coordinates getCoordinates(@NotNull FEntity fEntity);
    @Nullable Location getLocation(@NotNull FPlayer fPlayer);
    double distance(@NotNull FPlayer first, @NotNull FPlayer second);

    // Player state methods
    boolean isConsole(@NotNull Object platformPlayer);
    boolean isOperator(@NotNull FPlayer fPlayer);
    boolean isSneaking(@NotNull FPlayer fPlayer);
    boolean hasPlayedBefore(@NotNull FPlayer fPlayer);
    boolean hasPotionEffect(@NotNull FEntity fPlayer, @NotNull PotionType potionType);
    boolean isOnline(@NotNull FPlayer fPlayer);
    long getFirstPlayed(@NotNull FPlayer fPlayer);
    long getLastPlayed(@NotNull FPlayer fPlayer);
    long getAllTimePlayed(@NotNull FPlayer fPlayer);

    // Inventory operations
    void updateInventory(@NotNull UUID uuid);
    @Nullable Object getItem(@NotNull UUID uuid);

    // Player management
    @NotNull List<UUID> getOnlinePlayers();
    @NotNull Set<UUID> findPlayersWhoCanSee(FPlayer fPlayer, double x, double y, double z);
    @NotNull List<Integer> getPassengers(FPlayer fPlayer);
    @NotNull List<PlayedTimePlayer> getPlayedTimePlayers();
}
