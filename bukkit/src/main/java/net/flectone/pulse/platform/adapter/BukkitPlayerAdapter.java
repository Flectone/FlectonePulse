package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.platform.provider.AttributesProvider;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.provider.PassengersProvider;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitPlayerAdapter implements PlatformPlayerAdapter {

    private final Injector injector;
    private final PacketProvider packetProvider;
    private final AttributesProvider attributesProvider;
    private final PassengersProvider passengersProvider;
    private final ReflectionResolver reflectionResolver;

    private MethodHandle handleMethod;
    private MethodHandle gameProfileMethod;
    private MethodHandle propertiesMethod;
    private boolean gameProfileMethodsInitialized;

    private void initGameProfileMethods(Player player) {
        gameProfileMethodsInitialized = true;

        try {
            handleMethod = reflectionResolver.unreflectMethod(player.getClass().getMethod("getHandle"));
            if (handleMethod == null) return;

            Object entityPlayer = handleMethod.invoke(player);
            gameProfileMethod = reflectionResolver.unreflectMethod(entityPlayer.getClass().getMethod("getGameProfile"));
            if (gameProfileMethod == null) return;

            Object gameProfile = gameProfileMethod.invoke(entityPlayer);
            try {
                propertiesMethod = reflectionResolver.unreflectMethod(gameProfile.getClass().getMethod("properties"));
            } catch (NoSuchMethodException e) {
                propertiesMethod = reflectionResolver.unreflectMethod(gameProfile.getClass().getMethod("getProperties"));
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public @Nullable Object convertToPlatformPlayer(@NotNull FPlayer fPlayer) {
        return convertToPlatformPlayer(fPlayer.getUuid());
    }

    @Override
    public @Nullable Object convertToPlatformPlayer(@NotNull UUID uuid) {
        return Bukkit.getPlayer(uuid);
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
    public @NotNull Class<?> getPlayerClass() {
        return Player.class;
    }

    @Override
    public @Nullable UUID getUUID(@NotNull Object player) {
        return player instanceof Entity entity ? entity.getUniqueId() : null;
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
        if (player != null) {
            return packetProvider.getHostAddress(player.getAddress());
        }

        User user = packetProvider.getUser(fPlayer);
        if (user == null) return null;

        return packetProvider.getHostAddress(user.getAddress());
    }

    @Override
    public @NotNull String getEntityTranslationKey(@Nullable Object platformPlayer) {
        if (platformPlayer instanceof Entity entity) {
            return entity.getType().getTranslationKey();
        }

        return "";
    }

    @Override
    public @NotNull GameMode getGamemode(@NotNull FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        return player != null
                ? SpigotConversionUtil.fromBukkitGameMode(player.getGameMode())
                : GameMode.SURVIVAL;
    }

    @Override
    public PlayerHeadObjectContents.ProfileProperty getTexture(@NotNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;

        if (!gameProfileMethodsInitialized) {
            initGameProfileMethods(player);
        }

        try {
            Object entityPlayer = handleMethod.invoke(player);
            GameProfile profile = (GameProfile) gameProfileMethod.invoke(entityPlayer);
            PropertyMap properties = (PropertyMap) propertiesMethod.invoke(profile);

            Collection<Property> textures = properties.get("textures");
            if (textures == null || textures.isEmpty()) return null;

            Property textureProperty = textures.iterator().next();
            return PlayerHeadObjectContents.property(
                    "textures",
                    textureProperty.getValue(),
                    textureProperty.getSignature()
            );
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public boolean hasPlayedBefore(@NotNull FPlayer fPlayer) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(fPlayer.getUuid());
        return offlinePlayer.hasPlayedBefore();
    }

    @Override
    public boolean hasPotionEffect(@NotNull FEntity fEntity, @NotNull PotionType potionType) {
        Player player = Bukkit.getPlayer(fEntity.getUuid());
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
    public boolean isOperator(@NotNull FPlayer fPlayer) {
        return Bukkit.getOperators().stream().anyMatch(offlinePlayer -> offlinePlayer.getUniqueId().equals(fPlayer.getUuid()));
    }

    @Override
    public boolean isSneaking(@NotNull FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        return player.isSneaking();
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

        return player.getStatistic(Statistic.PLAY_ONE_MINUTE) * 50L;
    }

    @Override
    public @NotNull Component getPlayerListHeader(@NotNull FPlayer fPlayer) {
        HeaderModule headerModule = injector.getInstance(HeaderModule.class);

        String header;
        if (!headerModule.isModuleDisabledFor(fPlayer)) {
            header = headerModule.getCurrentMessage(fPlayer);
            if (header != null) {
                return injector.getInstance(MessagePipeline.class).builder(fPlayer, header).build();
            }
        }

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return Component.empty();

        header = player.getPlayerListHeader();
        if (header == null) return Component.empty();

        return LegacyComponentSerializer.legacySection().deserialize(header);
    }

    @Override
    public @NotNull Component getPlayerListFooter(@NotNull FPlayer fPlayer) {
        FooterModule footerModule = injector.getInstance(FooterModule.class);

        String footer;
        if (!footerModule.isModuleDisabledFor(fPlayer)) {
            footer = footerModule.getCurrentMessage(fPlayer);
            if (footer != null) {
                return injector.getInstance(MessagePipeline.class).builder(fPlayer, footer).build();
            }
        }

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return Component.empty();

        footer = player.getPlayerListFooter();
        if (footer == null) return Component.empty();

        return LegacyComponentSerializer.legacySection().deserialize(footer);
    }

    @Override
    public int getObjectiveScore(@NotNull UUID uuid, @Nullable ObjectiveModule.Mode objectiveValueType) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return 0;
        if (objectiveValueType == null) return 0;

        return switch (objectiveValueType) {
            case HEALTH -> (int) Math.round(player.getHealth() * 10.0) / 10;
            case LEVEL -> player.getLevel();
            case FOOD -> player.getFoodLevel();
            case PING -> packetProvider.getPing(player);
            case ARMOR -> (int) attributesProvider.getArmorValue(player);
            case ATTACK -> (int) attributesProvider.getAttackDamage(player);
        };
    }

    @Override
    public Statistics getStatistics(@NotNull FEntity fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        return new Statistics(
                Math.round(player.getHealth() * 10.0) / 10.0,
                attributesProvider.getArmorValue(player),
                player.getLevel(),
                player.getFoodLevel(),
                attributesProvider.getAttackDamage(player)
        );
    }

    @Override
    public double distance(@NotNull FPlayer first, @NotNull FPlayer second) {
        if (first.equals(second)) return 0.0;

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

        PlayerInventory playerInventory = player.getInventory();
        try {
            PlayerInventory.class.getMethod("getItemInMainHand");
            return playerInventory.getItemInMainHand().getType() == Material.AIR
                    ? playerInventory.getItemInOffHand()
                    : playerInventory.getItemInMainHand();

        } catch (NoSuchMethodException e) {
            return playerInventory.getItemInHand();
        }
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
    public @NotNull Set<UUID> findPlayersWhoCanSee(FPlayer fPlayer, double x, double y, double z) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return Collections.emptySet();

        World world = player.getWorld();
        Location location = player.getLocation();

        return world.getNearbyEntities(location, x, y, z)
                .stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .filter(target -> target.canSee(player))
                .map(Entity::getUniqueId)
                .collect(Collectors.toSet());
    }

    @Override
    public @NotNull List<Integer> getPassengers(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return Collections.emptyList();

        return passengersProvider.getPassengers(player);
    }

    @Override
    public @NotNull List<PlayedTimePlayer> getPlayedTimePlayers() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(offlinePlayer -> offlinePlayer.getName() != null)
                .map(offlinePlayer -> new PlayedTimePlayer(offlinePlayer.getName(), offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE) * 50L))
                .toList();
    }
}
