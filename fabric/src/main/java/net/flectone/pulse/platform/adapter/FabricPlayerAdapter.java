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
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.objective.ObjectiveModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPlayerAdapter implements PlatformPlayerAdapter {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final PacketProvider packetProvider;
    private final Injector injector;

    @Override
    public int getEntityId(@NotNull UUID uuid) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return 0;

        for (ServerLevel world : minecraftServer.getAllLevels()) {
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

        return minecraftServer.getPlayerList().getPlayers()
                .stream()
                .filter(serverPlayer -> serverPlayer.getId() == entityId)
                .findAny()
                .map(Entity::getUUID)
                .orElse(null);
    }

    @Override
    public @Nullable UUID getUUID(@NotNull Object platformPlayer) {
        return switch (platformPlayer) {
            case ServerPlayer player -> player.getUUID();
            case net.minecraft.commands.CommandSourceStack commandSource -> {
                ServerPlayer player = commandSource.getPlayer();
                yield player == null ? null : player.getUUID();
            }
            default -> null;
        };
    }

    @Override
    public @Nullable Class<?> getPlayerClass() {
        return ServerPlayer.class;
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
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return "";

        return player.getName().getString();
    }

    @Override
    public @NotNull String getName(@NotNull Object platformPlayer) {
        return switch (platformPlayer) {
            case ServerPlayer player -> player.getName().getString();
            case CommandSourceStack commandSource -> commandSource.getTextName();
            default -> "";
        };
    }

    private final FLogger fLogger;

    @Override
    public @NotNull String getWorldName(@NotNull FPlayer fPlayer) {
        ServerPlayer player = getPlayer(fPlayer.getUuid());
        if (player == null) return "";

        Level level = player.level();

        ResourceKey<Level> dimension = level.dimension();

        return dimension.identifier().getPath();
    }

    @Override
    public @NotNull String getWorldEnvironment(@NotNull FPlayer fPlayer) {
        return getWorldName(fPlayer);
    }

    @Override
    public @Nullable String getIp(@NotNull FPlayer fPlayer) {
        ServerPlayer player = getPlayer(fPlayer.getUuid());
        if (player != null) {
            ServerGamePacketListenerImpl connection = player.connection;
            return connection.getRemoteAddress().toString();
        }

        User user = packetProvider.getUser(fPlayer);
        if (user == null) return null;

        return packetProvider.getHostAddress(user.getAddress());
    }

    @Override
    public @NotNull String getEntityTranslationKey(@Nullable Object platformPlayer) {
        if (platformPlayer instanceof Entity entity) {
            return entity.getType().getDescriptionId();
        }

        return "";
    }

    @Override
    public @Nullable PlayerHeadObjectContents.ProfileProperty getTexture(@NotNull UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
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
    public @NotNull GameMode getGamemode(@NotNull FPlayer fPlayer) {
        ServerPlayer player = getPlayer(fPlayer.getUuid());
        if (player == null) return GameMode.SURVIVAL;

        return GameMode.getById(player.gameMode.getGameModeForPlayer().getId());
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

        ServerPlayer player = getPlayer(uuid);
        if (player == null) return 0;

        return switch (mode) {
            case HEALTH -> (int) Math.round(player.getHealth() * 10.0) / 10;
            case LEVEL -> player.experienceLevel;
            case FOOD -> player.getFoodData().getFoodLevel();
            case PING -> packetProvider.getPlayerManager().getPing(player);
            case ARMOR -> player.getArmorValue();
            case ATTACK -> player.getMainHandItem().getDamageValue();
        };
    }

    @Override
    public @Nullable Statistics getStatistics(@NotNull FEntity fEntity) {
        ServerPlayer player = getPlayer(fEntity.getUuid());
        if (player == null) return null;

        return new Statistics(
                Math.round(player.getHealth() * 10.0),
                player.getArmorValue(),
                player.experienceLevel,
                player.getFoodData().getFoodLevel(),
                player.getMainHandItem().getDamageValue()
        );
    }

    @Override
    public @Nullable Coordinates getCoordinates(@NotNull FEntity fEntity) {
        ServerPlayer player = getPlayer(fEntity.getUuid());
        if (player == null) return null;

        return new Coordinates(player.getBlockX(), player.getBlockY(), player.getBlockZ());
    }

    @Override
    public @Nullable Location getLocation(@NotNull FPlayer fPlayer) {
        ServerPlayer player = getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        return new Location(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
    }

    @Override
    public double distance(@NotNull FPlayer first, @NotNull FPlayer second) {
        if (first.equals(second)) return 0.0;

        ServerPlayer firstPlayer = getPlayer(first.getUuid());
        if (firstPlayer == null) return -1.0;

        ServerPlayer secondPlayer = getPlayer(second.getUuid());
        if (secondPlayer == null) return -1.0;
        if (!firstPlayer.level().equals(secondPlayer.level())) return -1.0;

        return firstPlayer.distanceTo(secondPlayer);
    }

    @Override
    public boolean isConsole(@NotNull Object platformPlayer) {
        return platformPlayer instanceof net.minecraft.commands.CommandSourceStack source
                && source.getEntity() == null
                && source.getServer().isDedicatedServer();
    }

    @Override
    public boolean isOperator(@NotNull FPlayer fPlayer) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        return minecraftServer.getPlayerList().isOp(new NameAndId(fPlayer.getUuid(), fPlayer.getName()));
    }

    @Override
    public boolean isSneaking(@NotNull FPlayer fPlayer) {
        ServerPlayer player = getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        return player.isCrouching();
    }

    @Override
    public boolean hasPlayedBefore(@NotNull FPlayer fPlayer) {
        return true;
    }

    @Override
    public boolean hasPotionEffect(@NotNull FEntity fPlayer, @NotNull PotionType potionType) {
        ServerPlayer player = getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        ClientVersion clientVersion = packetProvider.getServerVersion().toClientVersion();
        Optional<Holder.Reference<@NotNull MobEffect>> effect = BuiltInRegistries.MOB_EFFECT.get(potionType.getId(clientVersion));
        if (effect.isEmpty()) return false;

        MobEffectInstance effectInstance = player.getEffect(effect.get());
        return effectInstance != null && effectInstance.getDuration() > 0;
    }

    @Override
    public boolean isOnline(@NotNull FPlayer fPlayer) {
        ServerPlayer player = getPlayer(fPlayer.getUuid());
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
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return;

        player.inventoryMenu.broadcastChanges();
    }

    @Override
    public @Nullable Object getItem(@NotNull UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return null;

        return player.getMainHandItem();
    }

    @Override
    public @NotNull List<UUID> getOnlinePlayers() {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return Collections.emptyList();

        return minecraftServer.getPlayerList().getPlayers()
                .stream()
                .map(Entity::getUUID)
                .toList();
    }

    @Override
    public @NotNull Set<UUID> findPlayersWhoCanSee(FPlayer fPlayer, double x, double y, double z) {
        ServerPlayer player = getPlayer(fPlayer.getUuid());
        if (player == null) return Collections.emptySet();

        Vec3 position = player.position();
        AABB searchBox = new AABB(
                position.x - x, position.y - y, position.z - z,
                position.x + x, position.y + y, position.z + z
        );

        return player.level().getEntitiesOfClass(ServerPlayer.class, searchBox, entity -> true)
                .stream()
                .filter(target -> {
                    Vec3 startPos = target.getEyePosition(1.0F);
                    Vec3 endPos = player.getEyePosition(1.0F);

                    BlockHitResult hitResult = target.level().clip(
                            new net.minecraft.world.level.ClipContext(
                                    startPos,
                                    endPos,
                                    net.minecraft.world.level.ClipContext.Block.VISUAL,
                                    net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY,
                                    target
                            )
                    );

                    return hitResult.getType() == HitResult.Type.MISS;
                })
                .map(Entity::getUUID)
                .collect(Collectors.toSet());
    }

    @Override
    public @NotNull List<Integer> getPassengers(UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return Collections.emptyList();

        return player.getPassengers()
                .stream()
                .map(Entity::getId)
                .toList();
    }

    @Override
    public @NotNull List<PlayedTimePlayer> getPlayedTimePlayers() {
        return Collections.emptyList();
    }

    @Nullable
    public ServerPlayer getPlayer(UUID uuid) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return null;

        return minecraftServer.getPlayerList().getPlayer(uuid);
    }
}