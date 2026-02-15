package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.platform.provider.AttributesProvider;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.provider.PassengersProvider;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitPlayerAdapter implements PlatformPlayerAdapter {

    private final Injector injector;
    private final PacketProvider packetProvider;
    private final PacketSender packetSender;
    private final AttributesProvider attributesProvider;
    private final PassengersProvider passengersProvider;
    private final ReflectionResolver reflectionResolver;
    private final MessagePipeline messagePipeline;

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
        } catch (Throwable ignored) {
            // nothing
        }
    }

    @Override
    public @Nullable Object convertToPlatformPlayer(@NonNull UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public int getEntityId(@NonNull UUID uuid) {
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
    public @NonNull Class<?> getPlayerClass() {
        return Player.class;
    }

    @Override
    public @Nullable UUID getUUID(@NonNull Object player) {
        return player instanceof Entity entity ? entity.getUniqueId() : null;
    }

    @Override
    public @NonNull String getName(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? player.getName() : "";
    }

    @Override
    public @NonNull String getName(@NonNull Object player) {
        return player instanceof CommandSender commandSender ? commandSender.getName() : "";
    }

    @Override
    public int getPing(FPlayer fPlayer) {
        Object platformPlayer = convertToPlatformPlayer(fPlayer);
        if (platformPlayer == null) return 0;

        return packetProvider.getPing(platformPlayer);
    }

    @Override
    public @NonNull String getWorldName(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? player.getWorld().getName() : "";
    }

    @Override
    public @NonNull String getWorldEnvironment(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? player.getWorld().getEnvironment().toString().toLowerCase() : "";
    }

    @Override
    public @Nullable String getIp(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return packetProvider.getHostAddress(player.getAddress());
        }

        return packetProvider.getHostAddress(uuid);
    }

    @Override
    public @NonNull String getEntityTranslationKey(@Nullable Object platformPlayer) {
        if (platformPlayer instanceof Entity entity) {
            return entity.getType().getTranslationKey();
        }

        return "";
    }

    @Override
    public @NonNull String getGamemode(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null
                ? SpigotConversionUtil.fromBukkitGameMode(player.getGameMode()).name()
                : GameMode.SURVIVAL.name();
    }

    @Override
    public PlayerHeadObjectContents.ProfileProperty getTexture(@NonNull UUID uuid) {
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
    public boolean hasPlayedBefore(@NonNull UUID uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer.hasPlayedBefore();
    }

    @Override
    public boolean hasPotionEffect(@NonNull UUID uuid, @NonNull String potionType) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;

        PotionType potion = PotionTypes.getByName(potionType.toLowerCase());
        if (potion == null) return false;

        return player.hasPotionEffect(SpigotConversionUtil.toBukkitPotionEffectType(potion));
    }

    @Override
    public boolean isOnline(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;

        return player.isOnline();
    }

    @Override
    public boolean isConsole(@NonNull Object player) {
        return switch (player) {
            case ProxiedCommandSender proxiedCommandSender -> isConsoleSender(proxiedCommandSender.getCallee());
            case CommandSender commandSender -> isConsoleSender(commandSender);
            default -> false;
        };
    }

    private boolean isConsoleSender(CommandSender commandSender) {
        return commandSender instanceof ConsoleCommandSender
                || commandSender instanceof RemoteConsoleCommandSender
                || commandSender instanceof BlockCommandSender; // we cannot check whether block is a console or not, but its execution must mean a console
    }

    @Override
    public boolean isOperator(@NonNull UUID uuid) {
        return Bukkit.getOperators().stream().anyMatch(offlinePlayer -> offlinePlayer.getUniqueId().equals(uuid));
    }

    @Override
    public boolean isSneaking(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;

        return player.isSneaking();
    }

    @Override
    public long getFirstPlayed(@NonNull UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        return player.getFirstPlayed();
    }

    @Override
    public long getLastPlayed(@NonNull UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        return player.getLastPlayed();
    }

    @Override
    public long getAllTimePlayed(@NonNull UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        return player.getStatistic(Statistic.PLAY_ONE_MINUTE) * 50L;
    }

    @Override
    public @NonNull Component getPlayerListHeader(@NonNull FPlayer fPlayer) {
        HeaderModule headerModule = injector.getInstance(HeaderModule.class);

        String header;
        if (!headerModule.isModuleDisabledFor(fPlayer)) {
            header = headerModule.getCurrentMessage(fPlayer);
            if (header != null) {
                MessageContext messageContext = messagePipeline.createContext(fPlayer, header);
                return messagePipeline.build(messageContext);
            }
        }

        Player player = Bukkit.getPlayer(fPlayer.uuid());
        if (player == null) return Component.empty();

        header = player.getPlayerListHeader();
        if (header == null) return Component.empty();

        return LegacyComponentSerializer.legacySection().deserialize(header);
    }

    @Override
    public @NonNull Component getPlayerListFooter(@NonNull FPlayer fPlayer) {
        FooterModule footerModule = injector.getInstance(FooterModule.class);

        String footer;
        if (!footerModule.isModuleDisabledFor(fPlayer)) {
            footer = footerModule.getCurrentMessage(fPlayer);
            if (footer != null) {
                MessageContext messageContext = messagePipeline.createContext(fPlayer, footer);
                return messagePipeline.build(messageContext);
            }
        }

        Player player = Bukkit.getPlayer(fPlayer.uuid());
        if (player == null) return Component.empty();

        footer = player.getPlayerListFooter();
        if (footer == null) return Component.empty();

        return LegacyComponentSerializer.legacySection().deserialize(footer);
    }

    @Override
    public Statistics getStatistics(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
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
    public double distance(@NonNull UUID first, @NonNull UUID second) {
        if (first.equals(second)) return 0.0;

        Player firstPlayer = Bukkit.getPlayer(first);
        if (firstPlayer == null) return -1.0;

        Player secondPlayer = Bukkit.getPlayer(second);
        if (secondPlayer == null) return -1.0;

        World world = firstPlayer.getLocation().getWorld();
        if (world == null) return -1.0;
        if (!world.equals(secondPlayer.getLocation().getWorld())) return -1.0;

        return firstPlayer.getLocation().distance(secondPlayer.getLocation());
    }

    @Override
    public Coordinates getCoordinates(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;

        Location location = player.getLocation();

        return new Coordinates(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public Object getItem(@NonNull UUID uuid) {
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
    public void updateInventory(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        player.updateInventory();
    }

    @Override
    public @NonNull List<UUID> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Entity::getUniqueId)
                .toList();
    }

    @Override
    public @NonNull Set<UUID> findPlayersWhoCanSee(@NonNull UUID uuid, double x, double y, double z) {
        Player player = Bukkit.getPlayer(uuid);
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
    public @NonNull List<Integer> getPassengers(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return Collections.emptyList();

        return passengersProvider.getPassengers(player);
    }

    @Override
    public @NonNull List<PlayedTimePlayer> getPlayedTimePlayers() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(offlinePlayer -> offlinePlayer.getName() != null)
                .map(offlinePlayer -> new PlayedTimePlayer(offlinePlayer.getName(), offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE) * 50L))
                .toList();
    }

    @Override
    public void kick(FPlayer fPlayer, Component reason) {
        packetSender.send(fPlayer, new WrapperPlayServerDisconnect(reason));
    }
}
