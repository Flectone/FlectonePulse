package net.flectone.pulse.service;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.flectone.pulse.constant.PotionUtil;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.entity.HeadText;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.render.TextScreenRender;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.scheduler.SchedulerRunnable;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftHeadTextService implements HeadTextService {

    private final Map<UUID, Map<UUID, List<HolderViewerEntity>>> holderViewerEntities = new ConcurrentHashMap<>();

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final ReflectionResolver reflectionResolver;
    private final TaskScheduler taskScheduler;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final TextScreenRender textScreenRender;
    private final MinecraftPacketProvider packetProvider;
    private final MinecraftPacketSender packetSender;
    private final FLogger fLogger;

    @With
    private record HolderViewerEntity(
            ClientVersion clientVersion,
            HeadText text,
            int id,
            EntityType type,
            String world,
            int row
    ) {

        private boolean isTextDisplay() {
            return type == EntityTypes.TEXT_DISPLAY;
        }

    }

    @Override
    public void reload() {
        removeAll();

        taskScheduler.runAsyncTimer(this::tick, 20L, 20L);
    }

    @Override
    public void show(@NonNull HeadText text) {
        if (text.receivers().isEmpty()) return;

        CompletableFuture<Set<UUID>> nearbyEntitiesFuture = new CompletableFuture<>();

        SchedulerRunnable runnable = () -> {
            try {
                nearbyEntitiesFuture.complete(platformPlayerAdapter.findPlayersWhoCanSee(
                        text.holder(), text.distance(), text.distance(), text.distance()
                ));
            } catch (Exception e) {
                nearbyEntitiesFuture.completeExceptionally(e);
            }
        };

        if (reflectionResolver.isFolia()) {
            taskScheduler.runRegion(text.holder(), runnable);
        } else {
            taskScheduler.runSync(runnable);
        }

        nearbyEntitiesFuture.thenAcceptAsync(nearbyEntities -> nearbyEntities.stream()
                .map(fPlayerService::getFPlayer)
                .filter(fViewer -> !fViewer.isUnknown())
                .filter(fViewer -> text.receivers().contains(fViewer))
                .filter(fViewer -> text.visibleToSelf() || !fViewer.equals(text.holder()))
                .filter(fViewer -> !text.filterIgnores() || !socialService.isIgnored(fViewer, text.holder()))
                .filter(fViewer -> socialService.canSeeVanished(text.holder(), fViewer))
                .forEach(fViewer -> addEntity(text, fViewer)), taskScheduler.getExecutorService()
        ).exceptionally(e -> {
            fLogger.warning(e);
            return null;
        });
    }

    @Override
    public void remove(@NonNull UUID holder) {
        Map<UUID, List<HolderViewerEntity>> viewerEntities = holderViewerEntities.remove(holder);
        if (viewerEntities == null) return;

        viewerEntities.forEach((viewer, entities) -> destroy(holder, viewer, entities));
    }

    @Override
    public void removeAll() {
        holderViewerEntities.keySet().forEach(this::remove);
    }

    @Override
    public boolean isCorrectHolder(@NonNull UUID holder) {
        if (platformPlayerAdapter.isDead(holder)) return false;
        if (GameMode.SPECTATOR.name().equals(platformPlayerAdapter.getGamemode(holder))) return false;
        if (platformPlayerAdapter.hasPotionEffect(holder, PotionUtil.INVISIBILITY_POTION_NAME)) return false;
        if (!textScreenRender.getPassengers(holder).isEmpty()) return false;

        return platformPlayerAdapter.getPassengers(holder).isEmpty();
    }

    private void tick() {
        long now = System.currentTimeMillis();

        holderViewerEntities.forEach((holder, viewerEntities) -> {
            PlatformPlayerAdapter.Coordinates holderCoordinates = platformPlayerAdapter.getCoordinates(holder);
            if (holderCoordinates == null || !isCorrectHolder(holder)) {
                remove(holder);
                return;
            }

            String holderWorld = platformPlayerAdapter.getWorldName(holder);

            viewerEntities.forEach((viewer, entities) -> {
                PlatformPlayerAdapter.Coordinates viewerCoordinates = platformPlayerAdapter.getCoordinates(viewer);
                if (viewerCoordinates == null || !Objects.equals(holderWorld, platformPlayerAdapter.getWorldName(viewer))) {
                    List<HolderViewerEntity> removed = viewerEntities.remove(viewer);
                    if (removed != null) {
                        destroy(holder, viewer, removed);
                    }

                    return;
                }

                double distance = holderCoordinates.distance(viewerCoordinates);
                List<HolderViewerEntity> expired = new ArrayList<>();

                viewerEntities.computeIfPresent(viewer, (_, current) -> {
                    List<HolderViewerEntity> alive = new ArrayList<>(current.size());

                    for (HolderViewerEntity entity : current) {
                        if (distance > entity.text().distance() || entity.text().isEnd(now) || !holderWorld.equals(entity.world)) {
                            expired.add(entity);
                        } else {
                            alive.add(entity.withRow(alive.size()));
                        }
                    }

                    if (expired.isEmpty()) return current;

                    return alive.isEmpty() ? null : List.copyOf(alive);
                });

                destroy(holder, viewer, expired);
            });

            holderViewerEntities.computeIfPresent(holder, (_, current) -> current.isEmpty() ? null : current);
        });
    }

    private void addEntity(@NonNull HeadText text, @NonNull FPlayer fViewer) {
        holderViewerEntities.compute(text.holder().uuid(), (ignoredHolder, existingMap) -> {
            Map<UUID, List<HolderViewerEntity>> viewerEntities = existingMap == null
                    ? new ConcurrentHashMap<>()
                    : existingMap;

            viewerEntities.compute(fViewer.uuid(), (ignoredViewer, existingList) -> {
                List<HolderViewerEntity> entities = existingList == null
                        ? new ArrayList<>()
                        : new ArrayList<>(existingList);

                long count = entities.stream()
                        .filter(entity -> entity.text.row() == text.row())
                        .count();
                if (count >= text.maxCount()) return existingList;

                int targetRow = shiftEntitiesAndGetRow(text, entities);

                HolderViewerEntity newEntity = spawn(fViewer, text, targetRow, List.copyOf(entities));
                if (newEntity == null) return existingList;

                entities.add(newEntity);
                entities.sort(Comparator.comparingInt(HolderViewerEntity::row));
                return List.copyOf(entities);
            });

            return viewerEntities;
        });
    }

    private int shiftEntitiesAndGetRow(@NonNull HeadText text, @NonNull List<HolderViewerEntity> entities) {
        int targetRow = text.row();
        if (entities.isEmpty()) return targetRow;

        boolean shifting = false;
        for (int i = 0; i < entities.size(); i++) {
            HolderViewerEntity entity = entities.get(i);

            if (!shifting) {
                if (entity.row() < targetRow) {
                    continue;
                }

                if (!entity.text().movable()) {
                    targetRow = entity.row() + 1;
                    continue;
                }

                targetRow = entity.row();
                shifting = true;
            }

            entities.set(i, entity.withRow(entity.row() + 1));
        }

        return targetRow;
    }

    @Nullable
    private HolderViewerEntity spawn(FPlayer fReceiver, @NonNull HeadText text, int row, @NonNull List<HolderViewerEntity> existingEntities) {
        UUID holder = text.holder().uuid();
        UUID viewer = fReceiver.uuid();

        PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(holder);
        if (coordinates == null) return null;

        User user = packetProvider.getUser(fReceiver);
        if (user == null) return null;

        ClientVersion clientVersion = user.getClientVersion();

        int entityId = platformServerAdapter.generateEntityId();
        EntityType entityType = text.modern() && clientVersion.isNewerThanOrEquals(ClientVersion.V_1_19_4)
                ? EntityTypes.TEXT_DISPLAY
                : EntityTypes.AREA_EFFECT_CLOUD;
        Location spawnLocation = new Location(coordinates.x(), 0, coordinates.z(), coordinates.yaw(), coordinates.pitch());

        // create entity
        packetSender.send(fReceiver, new WrapperPlayServerSpawnEntity(
                entityId, UUID.randomUUID(), entityType, spawnLocation, 0, 0, null
        ));

        HolderViewerEntity entity = new HolderViewerEntity(clientVersion, text, entityId, entityType, platformPlayerAdapter.getWorldName(fReceiver), row);

        List<HolderViewerEntity> all = new ArrayList<>(existingEntities);
        all.add(entity);
        all.sort(Comparator.comparingInt(HolderViewerEntity::row));

        float offset = baseOffset(all);

        // update entity metadata
        updateEntityData(viewer, entity, text.component().apply(fReceiver), offset);

        // ride holder
        sendPassengers(holder, viewer, all);

        if (entity.isTextDisplay() && text.animationTime() > 0) {
            taskScheduler.runAsyncLater(() -> {
                changePosition(viewer, existingEntities, row, offset);

                float scale = text.scale();
                changeScale(viewer, entity, new Vector3f(scale, scale, scale));
            }, 1L); // Mojang, why I need delay for it?
        } else {
            changePosition(viewer, existingEntities, row, offset);
        }

        return entity;
    }

    @NonNull
    private List<HolderViewerEntity> getEntities(UUID holder, UUID viewer) {
        return holderViewerEntities.getOrDefault(holder, Map.of()).getOrDefault(viewer, List.of());
    }

    private void destroy(UUID holder, UUID viewer, List<HolderViewerEntity> entitiesToDelete) {
        if (entitiesToDelete.isEmpty()) return;

        List<HolderViewerEntity> instant = new ArrayList<>();
        List<HolderViewerEntity> animated = new ArrayList<>();

        String viewerWorld = platformPlayerAdapter.getWorldName(viewer);
        for (HolderViewerEntity entity : entitiesToDelete) {
            // if player is in another world, they will delete entity themselves
            if (!viewerWorld.equals(entity.world())) continue;

            if (entity.isTextDisplay() && entity.text().animationTime() > 0) {
                animated.add(entity);
            } else {
                instant.add(entity);
            }
        }

        if (!instant.isEmpty()) {
            destroyNow(holder, viewer, instant);
        }

        if (!animated.isEmpty()) {
            animated.forEach(entity -> changeScale(viewer, entity, Vector3f.zero()));

            int delay = animated.stream()
                    .mapToInt(entity -> entity.text().animationTime())
                    .max()
                    .orElse(1);

            taskScheduler.runAsyncLater(() -> destroyNow(holder, viewer, animated), delay);
        }
    }

    private void destroyNow(UUID holder, UUID viewer, List<HolderViewerEntity> entities) {
        int[] ids = entities.stream()
                .mapToInt(HolderViewerEntity::id)
                .toArray();

        packetSender.send(viewer, new WrapperPlayServerDestroyEntities(ids));
        updatePassengers(holder, viewer);
    }

    private void updatePassengers(UUID holder, UUID viewer) {
        List<HolderViewerEntity> remaining = getEntities(holder, viewer);
        if (remaining.isEmpty()) return;

        changePosition(viewer, remaining, 0, baseOffset(remaining));
        sendPassengers(holder, viewer, remaining);
    }

    private void sendPassengers(UUID holder, UUID viewer, @NonNull List<HolderViewerEntity> sortedEntities) {
        int[] displays = sortedEntities.stream()
                .filter(HolderViewerEntity::isTextDisplay)
                .mapToInt(HolderViewerEntity::id)
                .toArray();

        int[] legacy = sortedEntities.stream()
                .filter(entity -> !entity.isTextDisplay())
                .mapToInt(HolderViewerEntity::id)
                .toArray();

        int[] holderPassengers = legacy.length == 0
                ? displays
                : ArrayUtils.add(displays, legacy[0]);

        packetSender.send(viewer, new WrapperPlayServerSetPassengers(platformPlayerAdapter.getEntityId(holder), holderPassengers), true);

        for (int i = 0; i < legacy.length; i++) {
            int[] next = i + 1 < legacy.length ? new int[]{legacy[i + 1]} : ArrayUtils.EMPTY_INT_ARRAY;

            packetSender.send(viewer, new WrapperPlayServerSetPassengers(legacy[i], next), true);
        }
    }

    private void changePosition(@NonNull UUID viewer, @NonNull List<HolderViewerEntity> entities, int start, float baseOffset) {
        for (HolderViewerEntity entity : entities) {
            if (entity.row() < start) continue;
            if (!entity.isTextDisplay()) continue;

            List<EntityData<?>> passengerMetadata = new ArrayList<>();

            if (entity.text().animationTime() > 0) {
                // interpolation delay
                passengerMetadata.add(new EntityData<>(8, EntityDataTypes.INT, -1));

                // transformation duration
                passengerMetadata.add(new EntityData<>(9, EntityDataTypes.INT, entity.text().animationTime()));
            }

            // translation
            passengerMetadata.add(new EntityData<>(displayIndex(entity.clientVersion(), 2), EntityDataTypes.VECTOR3F, translation(entity, baseOffset)));

            packetSender.send(viewer, new WrapperPlayServerEntityMetadata(entity.id(), passengerMetadata));
        }
    }

    private void changeScale(@NonNull UUID viewer, @NonNull HolderViewerEntity entity, @NonNull Vector3f scale) {
        List<EntityData<?>> metadataList = new ArrayList<>();

        // interpolation delay
        metadataList.add(new EntityData<>(8, EntityDataTypes.INT, -1));

        // transformation duration
        metadataList.add(new EntityData<>(9, EntityDataTypes.INT, entity.text().animationTime()));

        // scale
        metadataList.add(new EntityData<>(displayIndex(entity.clientVersion(), 3), EntityDataTypes.VECTOR3F, scale));

        packetSender.send(viewer, new WrapperPlayServerEntityMetadata(entity.id(), metadataList));
    }

    private void updateEntityData(@NonNull UUID viewer, @NonNull HolderViewerEntity entity, @NonNull Component component, float baseOffset) {
        List<EntityData<?>> metadataList = new ArrayList<>();

        ClientVersion clientVersion = entity.clientVersion();
        HeadText text = entity.text();

        if (entity.isTextDisplay()) {
            // translation
            metadataList.add(new EntityData<>(displayIndex(clientVersion, 2), EntityDataTypes.VECTOR3F, translation(entity, baseOffset)));

            // scale
            float scale = text.scale();
            Vector3f scaleVector = text.animationTime() > 0 ? Vector3f.zero() : new Vector3f(scale, scale, scale);
            metadataList.add(new EntityData<>(displayIndex(clientVersion, 3), EntityDataTypes.VECTOR3F, scaleVector));

            // center for viewer
            HeadText.Billboard billboard = text.billboard();
            if (billboard != null) {
                metadataList.add(new EntityData<>(displayIndex(clientVersion, 6), EntityDataTypes.BYTE, billboard.id()));
            }

            // text
            metadataList.add(new EntityData<>(textDisplayIndex(clientVersion, 0), EntityDataTypes.ADV_COMPONENT, component));

            // width
            metadataList.add(new EntityData<>(textDisplayIndex(clientVersion, 1), EntityDataTypes.INT, 100000));

            // background color
            metadataList.add(new EntityData<>(textDisplayIndex(clientVersion, 2), EntityDataTypes.INT, text.background()));

            // flags
            byte flags = text.flags();
            if (flags != 0x00) {
                metadataList.add(new EntityData<>(textDisplayIndex(clientVersion, 4), EntityDataTypes.BYTE, flags));
            }
        } else {
            // text
            metadataList.add(new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(component)));

            // visible
            metadataList.add(new EntityData<>(3, EntityDataTypes.BOOLEAN, true));

            // empty cloud
            metadataList.add(new EntityData<>(areaEffectCloudRadiusIndex(clientVersion), EntityDataTypes.FLOAT, 0f));
        }

        packetSender.send(viewer, new WrapperPlayServerEntityMetadata(entity.id(), metadataList));
    }

    private float baseOffset(@NonNull List<HolderViewerEntity> sortedEntities) {
        return sortedEntities.getFirst().text().offset();
    }

    private Vector3f translation(@NonNull HolderViewerEntity entity, float baseOffset) {
        float step = entity.text().space() * entity.text().scale();
        return new Vector3f(0, baseOffset + step * entity.row(), 0);
    }

    private int displayIndex(@NonNull ClientVersion clientVersion, int field) {
        return 8 + (clientVersion.isNewerThanOrEquals(ClientVersion.V_1_20_2) ? 1 : 0) + field;
    }

    private int textDisplayIndex(@NonNull ClientVersion clientVersion, int field) {
        return displayIndex(clientVersion, 14 + field);
    }

    private int areaEffectCloudRadiusIndex(@NonNull ClientVersion clientVersion) {
        if (clientVersion.isNewerThanOrEquals(ClientVersion.V_1_17)) {
            return 8;
        } else if (clientVersion.isNewerThanOrEquals(ClientVersion.V_1_14)) {
            return 7;
        } else {
            return 6;
        }
    }

}