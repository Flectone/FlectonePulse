package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.context.MessageContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPlayerAdapter implements PlatformPlayerAdapter {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final PacketProvider packetProvider;
    private final MessagePipeline messagePipeline;
    private final Injector injector;

    @Override
    public int getEntityId(@NonNull UUID uuid) {
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
    public @Nullable UUID getUUID(@NonNull Object platformPlayer) {
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
    public @Nullable Class<?> getPlayerClass() {
        return ServerPlayerEntity.class;
    }

    @Override
    public @Nullable Object convertToPlatformPlayer(@NonNull UUID uuid) {
        return getPlayer(uuid);
    }

    @Override
    public @NonNull String getName(@NonNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return "";

        return player.getName().getString();
    }

    @Override
    public @NonNull String getName(@NonNull Object platformPlayer) {
        return switch (platformPlayer) {
            case ServerPlayerEntity player -> player.getName().getString();
            case ServerCommandSource commandSource -> commandSource.getName();
            default -> "";
        };
    }

    @Override
    public @NonNull String getWorldName(@NonNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return "";

        return player.getEntityWorld().getRegistryKey().getValue().getPath();
    }

    @Override
    public @NonNull String getWorldEnvironment(@NonNull UUID uuid) {
        return getWorldName(uuid);
    }

    @Override
    public @Nullable String getIp(@NonNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player != null) return player.getIp();

        User user = packetProvider.getUser(uuid);
        if (user == null) return null;

        return packetProvider.getHostAddress(user.getAddress());
    }

    @Override
    public @NonNull String getEntityTranslationKey(@Nullable Object platformPlayer) {
        if (platformPlayer instanceof Entity entity) {
            return entity.getType().getTranslationKey();
        }

        return "";
    }

    @Override
    public PlayerHeadObjectContents.@Nullable ProfileProperty getTexture(@NonNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return null;

        GameProfile profile = player.getGameProfile();
        PropertyMap properties = profile.properties();

        Collection<Property> textures = properties.get("textures");
        if (textures.isEmpty()) return null;

        Property textureProperty = textures.iterator().next();

        return PlayerHeadObjectContents.property(
                "textures",
                textureProperty.value(),
                textureProperty.signature()
        );
    }

    @Override
    public @NonNull GameMode getGamemode(@NonNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return GameMode.SURVIVAL;

        net.minecraft.world.GameMode gameMode = player.getGameMode();
        if (gameMode == null) return GameMode.SURVIVAL;

        return GameMode.getById(gameMode.getIndex());
    }

    @Override
    public @NonNull Component getPlayerListHeader(@NonNull FPlayer fPlayer) {
        HeaderModule headerModule = injector.getInstance(HeaderModule.class);

        if (!headerModule.isModuleDisabledFor(fPlayer)) {
            String header = headerModule.getCurrentMessage(fPlayer);
            if (header != null) {
                MessageContext messageContext = messagePipeline.createContext(fPlayer, header);
                return messagePipeline.build(messageContext);
            }
        }

        return Component.empty();
    }

    @Override
    public @NonNull Component getPlayerListFooter(@NonNull FPlayer fPlayer) {
        FooterModule footerModule = injector.getInstance(FooterModule.class);

        if (!footerModule.isModuleDisabledFor(fPlayer)) {
            String footer = footerModule.getCurrentMessage(fPlayer);
            if (footer != null) {
                MessageContext messageContext = messagePipeline.createContext(fPlayer, footer);
                return messagePipeline.build(messageContext);
            }
        }

        return Component.empty();
    }

    @Override
    public int getObjectiveScore(@NonNull UUID uuid, ObjectiveModule.@Nullable Mode mode) {
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
    public @Nullable Statistics getStatistics(@NonNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
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
    public @Nullable Coordinates getCoordinates(@NonNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return null;

        return new Coordinates(player.getBlockX(), player.getBlockY(), player.getBlockZ());
    }

    @Override
    public @Nullable Location getLocation(@NonNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return null;

        return new Location(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
    }

    @Override
    public double distance(@NonNull UUID first, @NonNull UUID second) {
        if (first.equals(second)) return 0.0;

        ServerPlayerEntity firstPlayer = getPlayer(first);
        if (firstPlayer == null) return -1.0;

        ServerPlayerEntity secondPlayer = getPlayer(second);
        if (secondPlayer == null) return -1.0;
        if (!firstPlayer.getEntityWorld().equals(secondPlayer.getEntityWorld())) return -1.0;

        return firstPlayer.distanceTo(secondPlayer);
    }

    @Override
    public boolean isConsole(@NonNull Object platformPlayer) {
        return platformPlayer instanceof ServerCommandSource source && source.getPlayer() == null;
    }

    @Override
    public boolean isOperator(@NonNull UUID uuid) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        PlayerManager playerManager = minecraftServer.getPlayerManager();
        if (playerManager == null) return false;

        return playerManager.isOperator(new PlayerConfigEntry(uuid, getName(uuid)));
    }

    @Override
    public boolean isSneaking(@NonNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return false;

        return player.isSneaking();
    }

    @Override
    public boolean hasPlayedBefore(@NonNull UUID uuid) {
        return true;
    }

    @Override
    public boolean hasPotionEffect(@NonNull UUID uuid, @NonNull PotionType potionType) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return false;

        ClientVersion clientVersion = packetProvider.getServerVersion().toClientVersion();
        Optional<RegistryEntry.Reference<StatusEffect>> statusEffect = Registries.STATUS_EFFECT.getEntry(potionType.getId(clientVersion));
        return statusEffect.filter(player::hasStatusEffect).isPresent();
    }

    @Override
    public boolean isOnline(@NonNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
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
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return;

        player.getInventory().updateItems();
    }

    @Override
    public @Nullable Object getItem(@NonNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return null;

        return player.getMainHandStack();
    }

    @Override
    public @NonNull List<UUID> getOnlinePlayers() {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return Collections.emptyList();

        return minecraftServer.getPlayerManager().getPlayerList()
                .stream()
                .map(Entity::getUuid)
                .toList();
    }

    @Override
    public @NonNull Set<UUID> findPlayersWhoCanSee(@NonNull UUID uuid, double x, double y, double z) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return Collections.emptySet();

        Vec3d position = new Vec3d(player.getX(), player.getY(), player.getZ());
        Box searchBox = new Box(position.subtract(x, y, z), position.add(x, y, z));
        return player.getEntityWorld().getEntitiesByClass(ServerPlayerEntity.class, searchBox, entity -> true)
                .stream()
                .filter(target -> {
                    HitResult hitResult = target.getEntityWorld().raycast(
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
    public @NonNull List<Integer> getPassengers(UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return Collections.emptyList();

        return player.getPassengerList()
                .stream()
                .map(Entity::getId)
                .toList();
    }

    @Override
    public @NonNull List<PlayedTimePlayer> getPlayedTimePlayers() {
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
