package net.flectone.pulse.adapter;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.brand.BrandModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
import net.flectone.pulse.module.message.sidebar.SidebarModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Singleton
public class BukkitPlayerAdapter implements PlatformPlayerAdapter {

    private final Injector injector;

    @Inject
    public BukkitPlayerAdapter(Injector injector) {
        this.injector = injector;
    }

    @Override
    public @Nullable Object convertToPlatformPlayer(@NotNull FPlayer fPlayer) {
        return Bukkit.getPlayer(fPlayer.getUuid());
    }

    @Override
    public int getEntityId(@NotNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? player.getEntityId() : 0;
    }

    @Override
    public @Nullable UUID getPlayerByEntityId(int entityId) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.getEntityId() == entityId)
                .findFirst()
                .map(Entity::getUniqueId)
                .orElse(null);
    }

    @Override
    public @Nullable UUID getUUID(@NotNull Object player) {
        return player instanceof Player onlinePlayer ? onlinePlayer.getUniqueId() : null;
    }

    @Override
    public @NotNull String getName(@NotNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? player.getName() : "";
    }

    @Override
    public @NotNull String getName(@NotNull Object player) {
        return player instanceof CommandSender commandSender ? commandSender.getName() : "";
    }

    @Override
    public @NotNull String getWorldName(@NotNull FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        return player != null ? player.getWorld().getName() : "";
    }

    @Override
    public @NotNull String getWorldEnvironment(@NotNull FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        return player != null ? player.getWorld().getEnvironment().toString().toLowerCase() : "";
    }

    @Override
    public @Nullable String getIp(@NotNull FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        InetSocketAddress address = player.getAddress();
        return address != null ? address.getHostString() : null;
    }

    @Override
    public @NotNull GameMode getGamemode(@NotNull FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        return player != null
                ? SpigotConversionUtil.fromBukkitGameMode(player.getGameMode())
                : GameMode.SURVIVAL;
    }

    @Override
    public boolean hasPlayedBefore(@NotNull FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return true;

        return player.hasPlayedBefore();
    }

    @Override
    public boolean hasPotionEffect(@NotNull FPlayer fPlayer, @NotNull PotionType potionType) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        return player.hasPotionEffect(SpigotConversionUtil.toBukkitPotionEffectType(potionType));
    }

    @Override
    public boolean isOnline(@NotNull FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        return player.isOnline();
    }

    @Override
    public boolean isConsole(@NotNull Object player) {
        return player instanceof ConsoleCommandSender;
    }

    @Override
    public long getFirstPlayed(@NotNull FPlayer fPlayer) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(fPlayer.getUuid());

        return player.getFirstPlayed();
    }

    @Override
    public long getLastPlayed(@NotNull FPlayer fPlayer) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(fPlayer.getUuid());

        return player.getLastPlayed();
    }

    @Override
    public long getAllTimePlayed(@NotNull FPlayer fPlayer) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(fPlayer.getUuid());

        return player.getStatistic(Statistic.PLAY_ONE_MINUTE) * 60L;
    }

    @Override
    public @NotNull Component getPlayerListHeader(@NotNull FPlayer fPlayer) {
        String header = injector.getInstance(HeaderModule.class).getCurrentMessage(fPlayer);
        if (header != null) {
            return injector.getInstance(MessagePipeline.class).builder(fPlayer, header).build();
        }

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return Component.empty();

        header = player.getPlayerListHeader();
        if (header == null) return Component.empty();

        return LegacyComponentSerializer.legacySection().deserialize(header);
    }

    @Override
    public @NotNull Component getPlayerListFooter(@NotNull FPlayer fPlayer) {
        String footer = injector.getInstance(FooterModule.class).getCurrentMessage(fPlayer);
        if (footer != null) {
            return injector.getInstance(MessagePipeline.class).builder(fPlayer, footer).build();
        }

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return Component.empty();

        footer = player.getPlayerListFooter();
        if (footer == null) return Component.empty();

        return LegacyComponentSerializer.legacySection().deserialize(footer);
    }

    @Override
    public int getObjectiveScore(@NotNull UUID uuid, @Nullable ObjectiveMode objectiveValueType) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return 0;
        if (objectiveValueType == null) return 0;

        return switch (objectiveValueType) {
            case HEALTH -> (int) Math.round(player.getHealth() * 10.0) / 10;
            case LEVEL -> player.getLevel();
            case FOOD -> player.getFoodLevel();
            case PING -> player.getPing();
            case ARMOR -> (int) getAttributeValue(player, Attribute.GENERIC_ARMOR);
            case ATTACK -> (int) getAttributeValue(player, Attribute.GENERIC_ATTACK_DAMAGE);
        };
    }

    private double getAttributeValue(@NotNull Player player, @NotNull Attribute attribute) {
        try {
            AttributeInstance instance = player.getAttribute(attribute);
            return instance != null ? Math.round(instance.getValue() * 10.0) / 10.0 : 0.0;
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0.0;
        }
    }

    @Override
    public Statistics getStatistics(@NotNull FEntity fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        return new Statistics(
                Math.round(player.getHealth() * 10.0) / 10.0,
                getAttributeValue(player, Attribute.GENERIC_ARMOR),
                player.getLevel(),
                player.getFoodLevel(),
                getAttributeValue(player, Attribute.GENERIC_ATTACK_DAMAGE)
        );
    }

    @Override
    public double distance(@NotNull FPlayer first, @NotNull FPlayer second) {
        Player firstPlayer = Bukkit.getPlayer(first.getUuid());
        if (firstPlayer == null) return -1.0;

        Player secondPlayer = Bukkit.getPlayer(second.getUuid());
        if (secondPlayer == null) return -1.0;

        World world = firstPlayer.getLocation().getWorld();
        if (world == null) return -1.0;
        if (!world.equals(secondPlayer.getLocation().getWorld())) return -1.0;

        return firstPlayer.getLocation().distance(secondPlayer.getLocation());
    }

    @Override
    public Coordinates getCoordinates(@NotNull FEntity fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        Location location = player.getLocation();

        return new Coordinates(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public com.github.retrooper.packetevents.protocol.world.@Nullable Location getLocation(@NotNull FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        return SpigotConversionUtil.fromBukkitLocation(player.getLocation());
    }

    @Override
    public Object getItem(@NotNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;

        return player.getInventory().getItemInMainHand().getType() == Material.AIR
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();
    }

    @Override
    public void updateInventory(@NotNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        player.updateInventory();
    }

    @Override
    public @NotNull List<UUID> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Entity::getUniqueId)
                .toList();
    }

    @Override
    public @NotNull List<UUID> getNearbyEntities(FPlayer fPlayer, double x, double y, double z) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return Collections.emptyList();

        World world = player.getWorld();
        Location location = player.getLocation();

        return world.getNearbyEntities(location, x, y, z)
                .stream()
                .filter(player::canSee)
                .map(Entity::getUniqueId)
                .toList();
    }

    @Override
    public @NotNull List<Integer> getPassengers(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return Collections.emptyList();

        List<Entity> passengers = player.getPassengers();
        if (passengers.isEmpty()) return Collections.emptyList();

        return passengers.stream()
                .map(Entity::getEntityId)
                .toList();
    }


    @Override
    public void clear(@NotNull FPlayer fPlayer) {
        injector.getInstance(AfkModule.class).remove("quit", fPlayer);
        injector.getInstance(ScoreboardModule.class).remove(fPlayer);
        injector.getInstance(BelownameModule.class).remove(fPlayer);
        injector.getInstance(TabnameModule.class).remove(fPlayer);
    }

    @Override
    public void update(@NotNull FPlayer fPlayer) {
        injector.getInstance(WorldModule.class).update(fPlayer);
        injector.getInstance(AfkModule.class).remove("", fPlayer);
        injector.getInstance(StreamModule.class).setStreamPrefix(fPlayer, fPlayer.isSetting(FPlayer.Setting.STREAM));

        injector.getInstance(TaskScheduler.class).runAsyncLater(() -> {
            injector.getInstance(ScoreboardModule.class).add(fPlayer);
            injector.getInstance(BelownameModule.class).add(fPlayer);
            injector.getInstance(TabnameModule.class).add(fPlayer);
            injector.getInstance(PlayerlistnameModule.class).update();
            injector.getInstance(SidebarModule.class).send(fPlayer);
        }, 10L);

        injector.getInstance(FooterModule.class).send(fPlayer);
        injector.getInstance(HeaderModule.class).send(fPlayer);
        injector.getInstance(BrandModule.class).send(fPlayer);
    }
}
