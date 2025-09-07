package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class FabricPlayerAdapter implements PlatformPlayerAdapter {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final PacketProvider packetProvider;
    private final Injector injector;

    @Inject
    public FabricPlayerAdapter(FabricFlectonePulse fabricFlectonePulse,
                               PacketProvider packetProvider,
                               Injector injector) {
        this.fabricFlectonePulse = fabricFlectonePulse;
        this.packetProvider = packetProvider;
        this.injector = injector;
    }

    @Override
    public int getEntityId(@NotNull UUID uuid) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return 0;

        for (ServerWorld world : minecraftServer.getWorlds()) {
            Entity entity = world.getEntity(uuid);
            if (entity != null) {
                return entity.getId();
            }
        }

        return 0;
    }

    @Override
    public @Nullable UUID getPlayerByEntityId(int entityId) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return null;

        return minecraftServer.getPlayerManager().getPlayerList()
                .stream()
                .filter(serverPlayerEntity -> serverPlayerEntity.getId() == entityId)
                .findAny()
                .map(Entity::getUuid)
                .orElse(null);
    }

    @Override
    public @Nullable UUID getUUID(@NotNull Object platformPlayer) {
        return switch (platformPlayer) {
            case ServerPlayerEntity player -> player.getUuid();
            case ServerCommandSource commandSource -> {
                ServerPlayerEntity player = commandSource.getPlayer();
                yield player == null ? null : player.getUuid();
            }
            default -> null;
        };
    }

    @Override
    public @Nullable Object convertToPlatformPlayer(@NotNull FPlayer fPlayer) {
        return getPlayer(fPlayer.getUuid());
    }

    @Override
    public @Nullable Object convertToPlatformPlayer(@NotNull UUID uuid) {
        return getPlayer(uuid);
    }

    @Override
    public @NotNull String getName(@NotNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return "";

        return player.getName().getString();
    }

    @Override
    public @NotNull String getName(@NotNull Object platformPlayer) {
        return switch (platformPlayer) {
            case ServerPlayerEntity player -> player.getName().getString();
            case ServerCommandSource commandSource -> commandSource.getName();
            default -> "";
        };
    }

    @Override
    public @NotNull String getWorldName(@NotNull FPlayer fPlayer) {
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        if (player == null) return "";

        return player.getWorld().getRegistryKey().getValue().getPath();
    }

    @Override
    public @NotNull String getWorldEnvironment(@NotNull FPlayer fPlayer) {
        return getWorldName(fPlayer);
    }

    @Override
    public @Nullable String getIp(@NotNull FPlayer fPlayer) {
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        if (player != null) return player.getIp();

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
    public @NotNull String getTranslationKey(@NotNull UUID uuid) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return "";

        for (ServerWorld serverWorld : minecraftServer.getWorlds()) {
            Entity entity = serverWorld.getEntity(uuid);
            if (entity == null) continue;

            return getEntityTranslationKey(entity);
        }

        return "";
    }

    @Override
    public @NotNull GameMode getGamemode(@NotNull FPlayer fPlayer) {
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        if (player == null) return GameMode.SURVIVAL;

        return GameMode.getById(player.getGameMode().getIndex());
    }

    @Override
    public @NotNull Component getPlayerListHeader(@NotNull FPlayer fPlayer) {
        HeaderModule headerModule = injector.getInstance(HeaderModule.class);

        if (!headerModule.isModuleDisabledFor(fPlayer)) {
            String header = headerModule.getCurrentMessage(fPlayer);
            if (header != null) {
                return injector.getInstance(MessagePipeline.class).builder(fPlayer, header).build();
            }
        }

        return Component.empty();
    }

    @Override
    public @NotNull Component getPlayerListFooter(@NotNull FPlayer fPlayer) {
        FooterModule footerModule = injector.getInstance(FooterModule.class);

        if (!footerModule.isModuleDisabledFor(fPlayer)) {
            String footer = footerModule.getCurrentMessage(fPlayer);
            if (footer != null) {
                return injector.getInstance(MessagePipeline.class).builder(fPlayer, footer).build();
            }
        }

        return Component.empty();
    }

    @Override
    public int getObjectiveScore(@NotNull UUID uuid, @Nullable ObjectiveModule.Mode mode) {
        if (mode == null) return 0;

        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return 0;

        return switch (mode) {
            case HEALTH -> (int) Math.round(player.getHealth() * 10.0) / 10;
            case LEVEL -> player.experienceLevel;
            case FOOD -> player.getHungerManager().getFoodLevel();
            case PING -> packetProvider.getPlayerManager().getPing(player);
            case ARMOR -> player.getArmor();
            case ATTACK -> player.getMainHandStack().getDamage();
        };
    }

    @Override
    public @Nullable Statistics getStatistics(@NotNull FEntity fEntity) {
        ServerPlayerEntity player = getPlayer(fEntity.getUuid());
        if (player == null) return null;

        return new Statistics(
                Math.round(player.getHealth() * 10.0),
                player.getArmor(),
                player.experienceLevel,
                player.getHungerManager().getFoodLevel(),
                player.getMainHandStack().getDamage()
        );
    }

    @Override
    public @Nullable Coordinates getCoordinates(@NotNull FEntity fEntity) {
        ServerPlayerEntity player = getPlayer(fEntity.getUuid());
        if (player == null) return null;

        return new Coordinates(player.getBlockX(), player.getBlockY(), player.getBlockZ());
    }

    @Override
    public @Nullable Location getLocation(@NotNull FPlayer fPlayer) {
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        return new Location(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
    }

    @Override
    public double distance(@NotNull FPlayer first, @NotNull FPlayer second) {
        if (first.equals(second)) return 0.0;

        ServerPlayerEntity firstPlayer = getPlayer(first.getUuid());
        if (firstPlayer == null) return -1.0;

        ServerPlayerEntity secondPlayer = getPlayer(second.getUuid());
        if (secondPlayer == null) return -1.0;
        if (!firstPlayer.getWorld().equals(secondPlayer.getWorld())) return -1.0;

        return firstPlayer.distanceTo(secondPlayer);
    }

    @Override
    public boolean isConsole(@NotNull Object platformPlayer) {
        return platformPlayer instanceof ServerCommandSource source
                && source.getEntity() == null
                && source.getServer().isDedicated();
    }

    @Override
    public boolean hasPlayedBefore(@NotNull FPlayer fPlayer) {
        return true;
    }

    @Override
    public boolean hasPotionEffect(@NotNull FEntity fPlayer, @NotNull PotionType potionType) {
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        ClientVersion clientVersion = packetProvider.getServerVersion().toClientVersion();
        Optional<RegistryEntry.Reference<StatusEffect>> statusEffect = Registries.STATUS_EFFECT.getEntry(potionType.getId(clientVersion));
        return statusEffect.filter(player::hasStatusEffect).isPresent();
    }

    @Override
    public boolean isOnline(@NotNull FPlayer fPlayer) {
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        return player != null;
    }

    @Override
    public long getFirstPlayed(@NotNull FPlayer fPlayer) {
        return 0;
    }

    @Override
    public long getLastPlayed(@NotNull FPlayer fPlayer) {
        return 0;
    }

    @Override
    public long getAllTimePlayed(@NotNull FPlayer fPlayer) {
        return 0;
    }

    @Override
    public void updateInventory(@NotNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return;

        player.getInventory().updateItems();
    }

    @Override
    public @Nullable Object getItem(@NotNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return null;

        return player.getMainHandStack();
    }

    @Override
    public @NotNull List<UUID> getOnlinePlayers() {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return Collections.emptyList();

        return minecraftServer.getPlayerManager().getPlayerList()
                .stream()
                .map(Entity::getUuid)
                .toList();
    }

    @Override
    public @NotNull Set<UUID> findPlayersWhoCanSee(FPlayer fPlayer, double x, double y, double z) {
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        if (player == null) return Collections.emptySet();

        Vec3d position = new Vec3d(player.getX(), player.getY(), player.getZ());
        Box searchBox = new Box(position.subtract(x, y, z), position.add(x, y, z));
        return player.getWorld().getEntitiesByClass(ServerPlayerEntity.class, searchBox, entity -> true)
                .stream()
                .filter(target -> {
                    HitResult hitResult = target.getWorld().raycast(
                            new RaycastContext(
                                    target.getCameraPosVec(1.0F),
                                    player.getCameraPosVec(1.0F),
                                    RaycastContext.ShapeType.VISUAL,
                                    RaycastContext.FluidHandling.SOURCE_ONLY,
                                    target
                            )
                    );

                    return hitResult.getType() == HitResult.Type.MISS;
                })
                .map(Entity::getUuid)
                .collect(Collectors.toSet());
    }

    @Override
    public @NotNull List<Integer> getPassengers(FPlayer fPlayer) {
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        if (player == null) return Collections.emptyList();

        return player.getPassengerList()
                .stream()
                .map(Entity::getId)
                .toList();
    }

    @Override
    public @NotNull List<PlayedTimePlayer> getPlayedTimePlayers() {
        return Collections.emptyList();
    }

    @Nullable
    public ServerPlayerEntity getPlayer(UUID uuid) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return null;

        PlayerManager playerManager = minecraftServer.getPlayerManager();
        if (playerManager == null) return null;

        return playerManager.getPlayer(uuid);
    }
}
