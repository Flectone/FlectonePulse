package net.flectone.pulse.adapter;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.world.Location;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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


    // Player identification methods
    int getEntityId(@NotNull UUID uuid);
    @Nullable UUID getPlayerByEntityId(int entityId);
    @Nullable UUID getUUID(@NotNull Object platformPlayer);
    @Nullable Object convertToPlatformPlayer(@NotNull FPlayer fPlayer);

    // Player information methods
    @NotNull String getName(@NotNull UUID uuid);
    @NotNull String getName(@NotNull Object platformPlayer);
    @NotNull String getWorldName(@NotNull FPlayer fPlayer);
    @NotNull String getWorldEnvironment(@NotNull FPlayer fPlayer);
    @Nullable String getIp(@NotNull FPlayer fPlayer);
    @NotNull GameMode getGamemode(@NotNull FPlayer fPlayer);

    // Player list methods
    @NotNull Component getPlayerListHeader(@NotNull FPlayer fPlayer);
    @NotNull Component getPlayerListFooter(@NotNull FPlayer fPlayer);

    // Objective and statistic methods
    int getObjectiveScore(@NotNull UUID uuid, @Nullable ObjectiveMode objectiveMode);
    @Nullable Statistics getStatistics(@NotNull FEntity fEntity);

    // Position and measurement methods
    @Nullable Coordinates getCoordinates(@NotNull FEntity fEntity);
    @Nullable Location getLocation(@NotNull FPlayer fPlayer);
    double distance(@NotNull FPlayer first, @NotNull FPlayer second);

    // Player state methods
    boolean isConsole(@NotNull Object platformPlayer);
    boolean hasPlayedBefore(@NotNull FPlayer fPlayer);
    boolean hasPotionEffect(@NotNull FPlayer fPlayer, @NotNull PotionType potionType);
    boolean isOnline(@NotNull FPlayer fPlayer);
    long getFirstPlayed(@NotNull FPlayer fPlayer);
    long getLastPlayed(@NotNull FPlayer fPlayer);
    long getAllTimePlayed(@NotNull FPlayer fPlayer);

    // Inventory operations
    void updateInventory(@NotNull UUID uuid);
    @Nullable Object getItem(@NotNull UUID uuid);

    // Player management
    void clear(@NotNull FPlayer fPlayer);
    void onJoin(@NotNull FPlayer fPlayer, boolean silent);
    void onQuit(@NotNull FPlayer fPlayer);
    @NotNull List<UUID> getOnlinePlayers();
    @NotNull List<UUID> getNearbyEntities(FPlayer fPlayer, double x, double y, double z);
    @NotNull List<Integer> getPassengers(FPlayer fPlayer);
}
