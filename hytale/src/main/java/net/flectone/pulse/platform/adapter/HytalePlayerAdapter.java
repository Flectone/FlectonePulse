package net.flectone.pulse.platform.adapter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.connection.PongType;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.quic.QuicStreamChannel;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.listener.HytaleBaseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytalePlayerAdapter implements PlatformPlayerAdapter {

    private final Provider<HytaleBaseListener> hytaleBaseListenerProvider;

    @Override
    public int getEntityId(@NonNull UUID uuid) {
        return 0;
    }

    @Override
    public @Nullable UUID getPlayerByEntityId(int entityId) {
        return null;
    }

    @Override
    public @Nullable UUID getUUID(@NonNull Object platformPlayer) {
        if (platformPlayer instanceof CommandSender commandSender) {
            return commandSender.getUuid();
        }

        return null;
    }

    @Override
    public @Nullable Class<?> getPlayerClass() {
        return Player.class;
    }

    @Override
    public @Nullable Object convertToPlatformPlayer(@NonNull UUID uuid) {
        return getPlayer(uuid);
    }

    @Override
    public @NonNull String getName(@NonNull UUID uuid) {
        PlayerRef player = getPlayer(uuid);
        return player == null ? "" : player.getUsername();
    }

    @Override
    public @NonNull String getName(@NonNull Object platformPlayer) {
        if (platformPlayer instanceof CommandSender commandSender) {
            return commandSender.getDisplayName();
        }

        return "";
    }

    @Override
    public int getPing(FPlayer fPlayer) {
        PlayerRef player = getPlayer(fPlayer.getUuid());
        if (player == null) return 0;

        PacketHandler.PingInfo pingInfo = player.getPacketHandler().getPingInfo(PongType.Direct);
        HistoricMetric metric = pingInfo.getPingMetricSet();
        double avgValue = metric.getLastValue();
        return Math.toIntExact((long) (avgValue / 1000.0F));
    }

    @Override
    public @NonNull String getWorldName(@NonNull UUID uuid) {
        Universe universe = Universe.get();
        if (universe == null) return "";

        PlayerRef player = getPlayer(uuid);
        if (player == null) return "";

        UUID worldUUID = player.getWorldUuid();
        if (worldUUID == null) return "";

        World world = universe.getWorld(worldUUID);
        if (world == null) return "";

        String worldName = world.getName().toLowerCase();
        if (worldName.contains("forgotten_temple")) return "forgotten_temple";
        if (worldName.contains("portals")) return "portals";

        return worldName;
    }

    @Override
    public @NonNull String getWorldEnvironment(@NonNull UUID uuid) {
        return getWorldName(uuid);
    }

    @Override
    public @Nullable String getIp(@NonNull UUID uuid) {
        PlayerRef player = getPlayer(uuid);
        if (player == null) return "";

        SocketAddress socketAddress;

        Channel channel = player.getPacketHandler().getChannel();
        if (channel instanceof QuicStreamChannel quicStreamChannel) {
            socketAddress = quicStreamChannel.parent().remoteSocketAddress();
        } else {
            socketAddress = channel.remoteAddress();
        }

        if (socketAddress instanceof InetSocketAddress inetSocketAddress) {
            return inetSocketAddress.getAddress().getHostAddress();
        }

        return "";
    }

    @Override
    public @NonNull String getEntityTranslationKey(@Nullable Object platformPlayer) {
        return "";
    }

    @Override
    public PlayerHeadObjectContents.@Nullable ProfileProperty getTexture(@NonNull UUID uuid) {
        return null;
    }

    @Override
    public @NonNull String getGamemode(@NonNull UUID uuid) {
        PlayerRef playerRef = getPlayer(uuid);
        if (playerRef == null) return GameMode.Adventure.name().toUpperCase();

        Ref<EntityStore> entityStoreRef = playerRef.getReference();
        if (entityStoreRef == null) return GameMode.Adventure.name().toUpperCase();

        Player player = entityStoreRef.getStore().getComponent(playerRef.getReference(), Player.getComponentType());
        if (player == null) return GameMode.Adventure.name().toUpperCase();

        GameMode gameMode = player.getGameMode();
        if (gameMode == null) return GameMode.Adventure.name().toUpperCase();

        return gameMode.name().toUpperCase();
    }

    @Override
    public @NonNull Component getPlayerListHeader(@NonNull FPlayer fPlayer) {
        return Component.empty();
    }

    @Override
    public @NonNull Component getPlayerListFooter(@NonNull FPlayer fPlayer) {
        return Component.empty();
    }

    @Override
    public @Nullable Statistics getStatistics(@NonNull UUID uuid) {
        PlayerRef playerRef = getPlayer(uuid);
        if (playerRef == null) return null;

        Universe universe = Universe.get();
        if (universe == null) return null;

        UUID worldUUID = playerRef.getWorldUuid();
        if (worldUUID == null) return null;

        World world = universe.getWorld(worldUUID);
        if (world == null) return null;

        CompletableFuture<Statistics> completableFuture = new CompletableFuture<>();

        world.execute(() -> {
            if (playerRef.getReference() == null) {
                completableFuture.complete(null);
                return;
            }

            double health = 0;
            double armor = 0;
            double mana = 0;
            double stamina = 0;
            double damage = 0;

            EntityStatMap statMap = playerRef.getReference().getStore().getComponent(playerRef.getReference(), EntityStatMap.getComponentType());
            if (statMap != null) {
                EntityStatValue healthValue = statMap.get(DefaultEntityStatTypes.getHealth());
                if (healthValue != null) {
                    health = healthValue.get();
                }

                EntityStatValue manaValue = statMap.get(DefaultEntityStatTypes.getMana());
                if (manaValue != null) {
                    mana = manaValue.get();
                }

                EntityStatValue staminaValue = statMap.get(DefaultEntityStatTypes.getStamina());
                if (staminaValue != null) {
                    stamina = staminaValue.get();
                }
            }

            completableFuture.complete(new Statistics(
                    health,
                    armor,
                    mana,
                    stamina,
                    damage
            ));
        });

        return completableFuture.join();
    }

    @Override
    public @Nullable Coordinates getCoordinates(@NonNull UUID uuid) {
        PlayerRef playerRef = getPlayer(uuid);
        if (playerRef == null) return null;

        Vector3d position = playerRef.getTransform().getPosition();
        Vector3f headRotation = playerRef.getHeadRotation();

        return new Coordinates(position.getX(), position.getY(), position.getZ(), headRotation.getYaw(), headRotation.getPitch());
    }

    @Override
    public double distance(@NonNull UUID first, @NonNull UUID second) {
        PlayerRef playerOne = getPlayer(first);
        if (playerOne == null) return -1;

        PlayerRef playerTwo = getPlayer(second);
        if (playerTwo == null) return -1;

        UUID worldOne = playerOne.getWorldUuid();
        UUID worldTwo = playerTwo.getWorldUuid();
        if (!Objects.equals(worldOne, worldTwo)) return -1;

        Vector3d positionOne = playerOne.getTransform().getPosition();
        Vector3d positionTwo = playerTwo.getTransform().getPosition();
        return Math.sqrt(square(positionOne.x - positionTwo.x) + square(positionOne.y - positionTwo.y) + square(positionOne.z - positionTwo.z));
    }

    private double square(double num) {
        return num * num;
    }

    @Override
    public boolean isConsole(@NonNull Object platformPlayer) {
        return platformPlayer instanceof ConsoleSender;
    }

    @Override
    public boolean isOperator(@NonNull UUID uuid) {
        PlayerRef player = getPlayer(uuid);
        if (player == null) return false;

        return PermissionsModule.get().getGroupsForUser(uuid).contains(HytalePermissionsProvider.OP_GROUP);
    }

    @Override
    public boolean isSneaking(@NonNull UUID uuid) {
        PlayerRef player = getPlayer(uuid);
        if (player == null) return false;

        return false;
    }

    @Override
    public boolean hasPlayedBefore(@NonNull UUID uuid) {
        return true;
    }

    @Override
    public boolean hasPotionEffect(@NonNull UUID uuid, @NonNull String potionType) {
        return false;
    }

    @Override
    public boolean isOnline(@NonNull UUID uuid) {
        PlayerRef player = getPlayer(uuid);
        return player != null;
    }

    @Override
    public long getFirstPlayed(@NonNull UUID uuid) {
        return 0;
    }

    @Override
    public long getLastPlayed(@NonNull UUID uuid) {
        return 0;
    }

    @Override
    public long getAllTimePlayed(@NonNull UUID uuid) {
        return 0;
    }

    @Override
    public void updateInventory(@NonNull UUID uuid) {
        // nothing
    }

    @Override
    public @Nullable Object getItem(@NonNull UUID uuid) {
        PlayerRef playerRef = getPlayer(uuid);
        if (playerRef == null) return null;

        Universe universe = Universe.get();
        if (universe == null) return null;

        UUID worldUUID = playerRef.getWorldUuid();
        if (worldUUID == null) return null;

        World world = universe.getWorld(worldUUID);
        if (world == null) return null;

        CompletableFuture<Object> completableFuture = new CompletableFuture<>();

        world.execute(() -> {
            if (playerRef.getReference() == null) {
                completableFuture.complete(null);
                return;
            }

            Player player = playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
            if (player == null) {
                completableFuture.complete(null);
                return;
            }

            Inventory inventory = player.getInventory();
            ItemContainer hotbar = inventory.getHotbar();

            short selectedSlot = player.getInventory().getActiveHotbarSlot();

            completableFuture.complete(hotbar.getItemStack(selectedSlot));
        });

        return completableFuture.join();
    }

    @Override
    public @NonNull List<UUID> getOnlinePlayers() {
        Universe universe = Universe.get();
        if (universe == null) return Collections.emptyList();

        return universe.getPlayers().stream()
                .map(PlayerRef::getUuid)
                .toList();
    }

    @Override
    public @NonNull Set<UUID> findPlayersWhoCanSee(UUID senderUuid, double viewDistanceX, double viewDistanceY, double viewDistanceZ) {
        PlayerRef senderRef = getPlayer(senderUuid);
        if (senderRef == null) return Collections.emptySet();

        Universe universe = Universe.get();
        if (universe == null) return Collections.emptySet();

        UUID worldUUID = senderRef.getWorldUuid();
        if (worldUUID == null) return Collections.emptySet();

        World world = universe.getWorld(worldUUID);
        if (world == null) return Collections.emptySet();

        Vector3d senderPos = senderRef.getTransform().getPosition();

        return world.getPlayerRefs().stream().filter(targetRef -> {
            Vector3d targetPos = targetRef.getTransform().getPosition();

            double dx = Math.abs(senderPos.getX() - targetPos.getX());
            double dy = Math.abs(senderPos.getY() - targetPos.getY());
            double dz = Math.abs(senderPos.getZ() - targetPos.getZ());

            return dx <= viewDistanceX && dy <= viewDistanceY && dz <= viewDistanceZ
                    && hasLineOfSight(senderRef, targetRef, world);
        }).map(PlayerRef::getUuid).collect(Collectors.toSet());
    }

    private boolean hasLineOfSight(PlayerRef from, PlayerRef to, World world) {
        Vector3d fromPos = from.getTransform().getPosition();
        Vector3d toPos = to.getTransform().getPosition();

        double dx = toPos.getX() - fromPos.getX();
        double dy = toPos.getY() - fromPos.getY();
        double dz = toPos.getZ() - fromPos.getZ();
        double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);

        if (distance <= 0) return true;

        dx /= distance;
        dy /= distance;
        dz /= distance;

        Vector3i targetBlock = TargetUtil.getTargetBlock(
                world,
                (blockId, fluidId) -> blockId != 0,
                fromPos.getX(), fromPos.getY(), fromPos.getZ(),
                dx, dy, dz,
                distance
        );

        return targetBlock == null;
    }

    @Override
    public @NonNull List<Integer> getPassengers(UUID uuid) {
        return Collections.emptyList();
    }

    @Override
    public @NonNull List<PlayedTimePlayer> getPlayedTimePlayers() {
        return Collections.emptyList();
    }

    @Override
    public void kick(FPlayer fPlayer, Component reason) {
        PlayerRef playerRef = getPlayer(fPlayer.getUuid());
        if (playerRef == null) return;

        playerRef.getPacketHandler().disconnect(PlainTextComponentSerializer.plainText().serialize(reason));
    }

    @Nullable
    public PlayerRef getPlayer(UUID uuid) {
        Universe universe = Universe.get();
        if (universe == null) return null;

        return universe.getPlayer(uuid);
    }

}
