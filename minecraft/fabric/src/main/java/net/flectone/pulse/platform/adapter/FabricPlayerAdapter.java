package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
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
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.PlayTime;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.sender.PacketSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPlayerAdapter implements PlatformPlayerAdapter {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;
    private final Injector injector;

    @Override
    public int getEntityId(@NonNull UUID uuid) {
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
                .filter(serverPlayerEntity -> serverPlayerEntity.getId() == entityId)
                .findAny()
                .map(Entity::getUUID)
                .orElse(null);
    }

    @Override
    public @Nullable UUID getUUID(@NonNull Object platformPlayer) {
        return switch (platformPlayer) {
            case ServerPlayer player -> player.getUUID();
            case CommandSourceStack commandSource -> {
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
    public @Nullable Object convertToPlatformPlayer(@NonNull UUID uuid) {
        return getPlayer(uuid);
    }

    @Override
    public @NonNull String getName(@NonNull UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return "";

        return player.getName().getString();
    }

    @Override
    public @NonNull String getName(@NonNull Object platformPlayer) {
        return switch (platformPlayer) {
            case ServerPlayer player -> player.getName().getString();
            case CommandSourceStack commandSource -> commandSource.getTextName();
            default -> "";
        };
    }

    @Override
    public int getPing(FPlayer fPlayer) {
        Object platformPlayer = convertToPlatformPlayer(fPlayer);
        if (platformPlayer == null) return 0;

        return packetProvider.getPing(platformPlayer);
    }

    @Override
    public @NonNull String getWorldName(@NonNull UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return "";

        return player.level().dimension().identifier().getPath();
    }

    @Override
    public @NonNull String getWorldEnvironment(@NonNull UUID uuid) {
        return getWorldName(uuid);
    }

    @Override
    public @Nullable String getIp(@NonNull UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
        if (player != null) {
            return player.connection.getRemoteAddress().toString();
        }

        return packetProvider.getHostAddress(uuid);
    }

    @Override
    public @NonNull String getEntityTranslationKey(@Nullable Object platformPlayer) {
        if (platformPlayer instanceof Entity entity) {
            return entity.getType().getDescriptionId();
        }

        return "";
    }

    @Override
    public PlayerHeadObjectContents.@Nullable ProfileProperty getTexture(@NonNull UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return null;

        GameProfile profile = player.getGameProfile();
        PropertyMap properties = profile.properties();

        Collection<Property> textures = properties.get("textures");
        if (textures.isEmpty()) return null;

        Property textureProperty = textures.iterator().next();

        return PlayerHeadObjectContents.property(
                "textures",
                textureProperty.value()
        );
    }

    @Override
    public @NonNull String getGamemode(@NonNull UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return GameMode.SURVIVAL.name();

        GameType gameType = player.gameMode();
        if (gameType == null) return GameMode.SURVIVAL.name();

        return GameMode.getById(gameType.getId()).name();
    }

    @Override
    public @NonNull Component getPlayerListHeader(@NonNull FPlayer fPlayer) {
        HeaderModule headerModule = injector.getInstance(HeaderModule.class);

        if (!moduleController.isDisabledFor(headerModule, fPlayer)) {
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

        if (!moduleController.isDisabledFor(footerModule, fPlayer)) {
            String footer = footerModule.getCurrentMessage(fPlayer);
            if (footer != null) {
                MessageContext messageContext = messagePipeline.createContext(fPlayer, footer);
                return messagePipeline.build(messageContext);
            }
        }

        return Component.empty();
    }

    @Override
    public @Nullable Statistics getStatistics(@NonNull UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
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
    public @Nullable Coordinates getCoordinates(@NonNull UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return null;

        return new Coordinates(player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getYRot(), player.getXRot());
    }

    @Override
    public double distance(@NonNull UUID first, @NonNull UUID second) {
        if (first.equals(second)) return 0.0;

        ServerPlayer firstPlayer = getPlayer(first);
        if (firstPlayer == null) return -1.0;

        ServerPlayer secondPlayer = getPlayer(second);
        if (secondPlayer == null) return -1.0;
        if (!firstPlayer.level().equals(secondPlayer.level())) return -1.0;

        return firstPlayer.distanceTo(secondPlayer);
    }

    @Override
    public boolean isConsole(@NonNull Object platformPlayer) {
        return platformPlayer instanceof CommandSourceStack source && source.getPlayer() == null;
    }

    @Override
    public boolean isOperator(@NonNull UUID uuid) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        return minecraftServer.getPlayerList().isOp(new NameAndId(uuid, getName(uuid)));
    }

    @Override
    public boolean isSneaking(@NonNull UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return false;

        return player.isCrouching();
    }

    @Override
    public boolean hasPlayedBefore(@NonNull UUID uuid) {
        return true;
    }

    @Override
    public boolean hasPotionEffect(@NonNull UUID uuid, @NonNull String potionType) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return false;

        PotionType potion = PotionTypes.getByName(potionType);
        if (potion == null) return false;

        ClientVersion clientVersion = packetProvider.getServerVersion().toClientVersion();
        Optional<Holder.Reference<@NotNull MobEffect>> effect = BuiltInRegistries.MOB_EFFECT.get(potion.getId(clientVersion));
        if (effect.isEmpty()) return false;

        MobEffectInstance effectInstance = player.getEffect(effect.get());
        return effectInstance != null && effectInstance.getDuration() > 0;
    }

    @Override
    public boolean isOnline(@NonNull UUID uuid) {
        return getPlayer(uuid) != null;
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
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return;

        player.inventoryMenu.broadcastChanges();
    }

    @Override
    public @Nullable Object getItem(@NonNull UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return null;

        return player.getMainHandItem();
    }

    @Override
    public @NonNull List<UUID> getOnlinePlayers() {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return Collections.emptyList();

        return minecraftServer.getPlayerList().getPlayers()
                .stream()
                .map(Entity::getUUID)
                .toList();
    }

    @Override
    public @NonNull Set<UUID> findPlayersWhoCanSee(@NonNull UUID uuid, double x, double y, double z) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return Collections.emptySet();

        Vec3 position = player.position();
        AABB searchBox = new AABB(
                position.x - x, position.y - y, position.z - z,
                position.x + x, position.y + y, position.z + z
        );

        return player.level().getEntitiesOfClass(ServerPlayer.class, searchBox, _ -> true)
                .stream()
                .filter(target -> {
                    Vec3 startPos = target.getEyePosition(1.0F);
                    Vec3 endPos = player.getEyePosition(1.0F);

                    BlockHitResult hitResult = target.level().clip(
                            new ClipContext(
                                    startPos,
                                    endPos,
                                    ClipContext.Block.VISUAL,
                                    ClipContext.Fluid.SOURCE_ONLY,
                                    target
                            )
                    );

                    return hitResult.getType() == HitResult.Type.MISS;
                })
                .map(Entity::getUUID)
                .collect(Collectors.toSet());
    }

    @Override
    public @NonNull List<Integer> getPassengers(UUID uuid) {
        ServerPlayer player = getPlayer(uuid);
        if (player == null) return Collections.emptyList();

        return player.getPassengers()
                .stream()
                .map(Entity::getId)
                .toList();
    }

    @Override
    public @Nullable PlayTime getPlayedTime(FPlayer fPlayer) {
        return null;
    }

    @Override
    public void kick(FPlayer fPlayer, Component reason) {
        packetSender.send(fPlayer, new WrapperPlayServerDisconnect(reason));
    }

    @Nullable
    public ServerPlayer getPlayer(UUID uuid) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return null;

        return minecraftServer.getPlayerList().getPlayer(uuid);
    }
}
