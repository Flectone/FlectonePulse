package net.flectone.pulse.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.brand.BrandModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.flectone.pulse.module.message.sidebar.SidebarModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Singleton
public class FabricPlayerAdapter implements PlatformPlayerAdapter {

    private final MinecraftServer minecraftServer;
    private final Injector injector;

    @Inject
    public FabricPlayerAdapter(MinecraftServer minecraftServer,
                               Injector injector) {
        this.minecraftServer = minecraftServer;
        this.injector = injector;
    }

    @Override
    public int getEntityId(@NotNull UUID uuid) {
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
        for (ServerWorld world : minecraftServer.getWorlds()) {
            Entity entity = world.getEntityById(entityId);
            if (entity != null) {
                return entity.getUuid();
            }
        }

        return null;
    }

    @Override
    public @Nullable UUID getUUID(@NotNull Object platformPlayer) {
        return platformPlayer instanceof ServerPlayerEntity player ? player.getUuid() : null;
    }

    @Override
    public @Nullable Object convertToPlatformPlayer(@NotNull FPlayer fPlayer) {
        return getPlayer(fPlayer.getUuid());
    }

    @Override
    public @NotNull String getName(@NotNull UUID uuid) {
        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return "";

        return player.getName().getString();
    }

    @Override
    public @NotNull String getName(@NotNull Object platformPlayer) {
        return platformPlayer instanceof ServerCommandSource source ? source.getName() : "";
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
        if (player == null) return "";

        return player.getIp();
    }

    @Override
    public @NotNull GameMode getGamemode(@NotNull FPlayer fPlayer) {
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        if (player == null) return GameMode.SURVIVAL;

        return GameMode.getById(player.getGameMode().getIndex());
    }

    @Override
    public @NotNull Component getPlayerListHeader(@NotNull FPlayer fPlayer) {
        String header = injector.getInstance(HeaderModule.class).getCurrentMessage(fPlayer);
        if (header != null) {
            return injector.getInstance(MessagePipeline.class).builder(fPlayer, header).build();
        }

//        for (ServerWorld world : minecraftServer.getWorlds()) {
//            Entity entity = world.getEntity(fPlayer.getUuid());
//            if (entity instanceof ServerPlayerEntity player) {
//                return player.;
//            }
//        }

        return Component.empty();
    }

    @Override
    public @NotNull Component getPlayerListFooter(@NotNull FPlayer fPlayer) {
        String footer = injector.getInstance(FooterModule.class).getCurrentMessage(fPlayer);
        if (footer != null) {
            return injector.getInstance(MessagePipeline.class).builder(fPlayer, footer).build();
        }

        return Component.empty();
    }

    @Override
    public int getObjectiveScore(@NotNull UUID uuid, @Nullable ObjectiveMode objectiveMode) {
        if (objectiveMode == null) return 0;

        ServerPlayerEntity player = getPlayer(uuid);
        if (player == null) return 0;

        return switch (objectiveMode) {
            case HEALTH -> (int) Math.round(player.getHealth() * 10.0) / 10;
            case LEVEL -> player.experienceLevel;
            case FOOD -> player.getHungerManager().getFoodLevel();
            case PING -> PacketEvents.getAPI().getPlayerManager().getPing(player);
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
        return getAllTimePlayed(fPlayer) > 0;
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
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        if (player == null) return 0;

        return player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
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
    public void clear(@NotNull FPlayer fPlayer) {
        injector.getInstance(AfkModule.class).remove("quit", fPlayer);
        injector.getInstance(ScoreboardModule.class).remove(fPlayer);
//        injector.getInstance(BelownameModule.class).remove(fPlayer);
//        injector.getInstance(TabnameModule.class).remove(fPlayer);
    }

    @Override
    public void update(@NotNull FPlayer fPlayer) {
        injector.getInstance(WorldModule.class).update(fPlayer);
        injector.getInstance(AfkModule.class).remove("", fPlayer);
        injector.getInstance(StreamModule.class).setStreamPrefix(fPlayer, fPlayer.isSetting(FPlayer.Setting.STREAM));

        injector.getInstance(TaskScheduler.class).runAsyncLater(() -> {
            injector.getInstance(ScoreboardModule.class).add(fPlayer);
//            injector.getInstance(BelownameModule.class).add(fPlayer);
//            injector.getInstance(TabnameModule.class).add(fPlayer);
            injector.getInstance(PlayerlistnameModule.class).update();
            injector.getInstance(SidebarModule.class).send(fPlayer);
        }, 10L);

        injector.getInstance(FooterModule.class).send(fPlayer);
        injector.getInstance(HeaderModule.class).send(fPlayer);
        injector.getInstance(BrandModule.class).send(fPlayer);
    }

    @Override
    public @NotNull List<UUID> getOnlinePlayers() {
        List<UUID> onlinePlayers = new ArrayList<>();

        for (ServerWorld world : minecraftServer.getWorlds()) {
            onlinePlayers.addAll(world.getPlayers().stream().map(Entity::getUuid).toList());
        }

        return onlinePlayers;
    }

    @Override
    public @NotNull List<UUID> getNearbyEntities(FPlayer fPlayer, double x, double y, double z) {
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        if (player == null) return Collections.emptyList();

        Vec3d position = new Vec3d(player.getX(), player.getY(), player.getZ());

        Box searchBox = new Box(position.subtract(x, y, z), position.add(x, y, z));
        return player.getWorld().getOtherEntities(null, searchBox)
                .stream()
                .map(Entity::getUuid)
                .toList();
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
    public boolean hasPotionEffect(@NotNull FPlayer fPlayer, @NotNull PotionType potionType) {
        ServerPlayerEntity player = getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        Optional<RegistryEntry.Reference<StatusEffect>> statusEffect = Registries.STATUS_EFFECT.getEntry(potionType.getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion()));
        return statusEffect.filter(player::hasStatusEffect).isPresent();
    }

    @Nullable
    public ServerPlayerEntity getPlayer(UUID uuid) {
        for (ServerWorld world : minecraftServer.getWorlds()) {
            Entity entity = world.getPlayerByUuid(uuid);
            if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
                return serverPlayerEntity;
            }
        }

        return null;
    }
}
