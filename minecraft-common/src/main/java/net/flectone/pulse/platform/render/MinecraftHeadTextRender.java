package net.flectone.pulse.platform.render;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
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
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.flectone.pulse.FlectonePulseAPI;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.PotionUtil;
import net.flectone.pulse.converter.ColorConverter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.HeadText;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.serializer.ComponentSerializer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.NearbyService;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftHeadTextRender implements HeadTextRender {

    private static final float LEGACY_1_19_4_OFFSET = 0.42f;

    private final Map<UUID, int[]> platformPlayerPassengers = new ConcurrentHashMap<>();
    private final Map<UUID, Float> holderScales = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, List<HolderViewerEntity>>> holderViewerEntities = new ConcurrentHashMap<>();

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final TaskScheduler taskScheduler;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final MinecraftPacketProvider packetProvider;
    private final MinecraftPacketSender packetSender;
    private final ColorConverter colorConverter;
    private final ComponentSerializer componentSerializer;
    private final NearbyService nearbyService;

    @With
    @Builder(toBuilder = true)
    private record HolderViewerEntity(
            @NonNull ModuleName moduleName,
            @NonNull ClientVersion clientVersion,
            @NonNull HeadText headText,
            @NonNull EntityType type,
            @NonNull String world,
            PlatformPlayerAdapter.@NonNull Coordinates coordinates,
            int id,
            int componentHash,
            int row,
            long endTime,
            float holderScale
    ) {

        private boolean isTextDisplay() {
            return type == EntityTypes.TEXT_DISPLAY;
        }

        private boolean isEnd(long now) {
            return !headText.isPermanent() && now > endTime;
        }

    }

    @Override
    public void reload() {
        removeAll();

        taskScheduler.runAsyncTimer(() -> {
            if (holderViewerEntities.isEmpty()) return;

            long now = System.currentTimeMillis();

            holderViewerEntities.forEach((holder, viewerEntities) -> {
                PlatformPlayerAdapter.Coordinates holderCoordinates = platformPlayerAdapter.getCoordinates(holder);
                if (holderCoordinates == null || !isCorrectHolder(holder)) {
                    remove(holder);
                    return;
                }

                boolean isSneaking = platformPlayerAdapter.isSneaking(holder);
                boolean strictHolder = isCorrectHolder(holder);
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

                    double distanceSquared = holderCoordinates.distanceSquared(viewerCoordinates);
                    if (!hasExpired(entities, holderWorld, distanceSquared, isSneaking, strictHolder, now)) return;

                    List<HolderViewerEntity> expired = new ArrayList<>();

                    viewerEntities.computeIfPresent(viewer, (_, current) -> {
                        List<HolderViewerEntity> alive = new ArrayList<>(current.size());

                        for (HolderViewerEntity entity : current) {
                            if (isExpired(entity, holderWorld, distanceSquared, isSneaking, strictHolder, now)) {
                                expired.add(entity);
                            } else {
                                alive.add(entity.toBuilder()
                                        .coordinates(holderCoordinates)
                                        .row(alive.size())
                                        .build()
                                );
                            }
                        }

                        if (expired.isEmpty()) return current;

                        return alive.isEmpty() ? null : List.copyOf(alive);
                    });

                    destroy(holder, viewer, expired);
                });

                holderViewerEntities.computeIfPresent(holder, (_, current) -> current.isEmpty() ? null : current);
            });
        }, 1L, 1L);
    }

    @Override
    public void removeAll() {
        holderViewerEntities.keySet().forEach(this::remove);
    }

    @Override
    public void render(@NonNull ModuleName moduleName,
                       @NonNull FPlayer holder,
                       @NonNull Set<FPlayer> receivers,
                       @NonNull Function<FPlayer, Component> component,
                       @NonNull HeadText headText) {
        if (FlectonePulseAPI.isDisabling()) return;
        if (receivers.isEmpty()) return;

        PlatformPlayerAdapter.Coordinates holderCoordinates = platformPlayerAdapter.getCoordinates(holder);
        if (holderCoordinates == null) return;

        String holderWorld = platformPlayerAdapter.getWorldName(holder);
        for (UUID player : nearbyService.getNearbyPlayers(holder.uuid(), headText.distance())) {
            FPlayer viewer = fPlayerService.getFPlayer(player);
            if (viewer.isUnknown()) continue;
            if (!receivers.contains(viewer)) continue;

            render(moduleName, holder, viewer, component, headText, holderCoordinates, holderWorld);
        }
    }

    @Override
    public void render(@NonNull ModuleName moduleName,
                       @NonNull FPlayer holder,
                       @NonNull FPlayer viewer,
                       @NonNull Function<FPlayer, Component> component,
                       @NonNull HeadText headText) {
        if (FlectonePulseAPI.isDisabling()) return;

        PlatformPlayerAdapter.Coordinates holderCoordinates = platformPlayerAdapter.getCoordinates(holder);
        if (holderCoordinates == null) return;

        String holderWorld = platformPlayerAdapter.getWorldName(holder);
        render(moduleName, holder, viewer, component, headText, holderCoordinates, holderWorld);
    }

    @Override
    public void render(@NonNull ModuleName moduleName,
                       @NonNull FPlayer holder,
                       @NonNull FPlayer viewer,
                       @NonNull Function<FPlayer, Component> component,
                       @NonNull HeadText headText,
                       PlatformPlayerAdapter.@NonNull Coordinates holderCoordinates,
                       @NonNull String holderWorld) {
        if (FlectonePulseAPI.isDisabling()) return;

        if (!headText.visibleToSelf() && viewer.equals(holder)) return;
        if (headText.filterIgnores() && socialService.isIgnored(viewer, holder)) return;
        if (!socialService.canSeeVanished(holder, viewer)) return;
        if (!isCorrectHolder(holder.uuid())) return;
        if (headText.hideWhenSneaking() && platformPlayerAdapter.isSneaking(holder)) return;

        PlatformPlayerAdapter.Coordinates viewerCoordinates = platformPlayerAdapter.getCoordinates(viewer);
        if (viewerCoordinates == null) return;
        if (holderCoordinates.distanceSquared(viewerCoordinates) > headText.distance() * headText.distance()) return;

        String viewerWorld = platformPlayerAdapter.getWorldName(viewer);
        if (!holderWorld.equals(viewerWorld)) return;

        holderViewerEntities.compute(holder.uuid(), (_, existingMap) -> {
            Map<UUID, List<HolderViewerEntity>> viewerEntities = existingMap == null
                    ? new ConcurrentHashMap<>()
                    : existingMap;

            viewerEntities.compute(viewer.uuid(), (_, existingList) -> {
                List<HolderViewerEntity> entities = existingList == null
                        ? new ArrayList<>()
                        : new ArrayList<>(existingList);

                int targetRow = headText.row();
                if (!headText.movable()) {
                    for (int i = 0; i < entities.size(); i++) {
                        HolderViewerEntity entity = entities.get(i);
                        if (entity.moduleName() == moduleName && entity.row() == targetRow) {

                            Component message = component.apply(viewer);
                            int componentHash = message.hashCode();
                            if (entity.componentHash() == componentHash){
                                return existingList;
                            }

                            entity = entity.toBuilder()
                                    .headText(headText)
                                    .coordinates(holderCoordinates)
                                    .endTime(headText.calculateEndTime())
                                    .componentHash(componentHash)
                                    .build();

                            entities.set(i, entity);
                            updateEntityComponent(holder.uuid(), viewer.uuid(), entity, message);
                            return List.copyOf(entities);
                        }
                    }
                }

                long count = entities.stream()
                        .filter(entity -> entity.moduleName() == moduleName)
                        .count();
                if (count >= headText.maxCount()) {
                    return existingList;
                }

                if (!entities.isEmpty()) {
                    boolean shifting = false;
                    for (int i = 0; i < entities.size(); i++) {
                        HolderViewerEntity entity = entities.get(i);
                        if (!shifting) {
                            if (entity.row() < targetRow) {
                                continue;
                            }

                            if (!entity.headText().movable()) {
                                targetRow = entity.row() + 1;
                                continue;
                            }

                            targetRow = entity.row();
                            shifting = true;
                        }

                        entities.set(i, entity.toBuilder()
                                .coordinates(holderCoordinates)
                                .row(entity.row() + 1)
                                .build()
                        );
                    }
                }

                HolderViewerEntity newEntity = spawn(moduleName, holder, viewer, headText, component, holderCoordinates, List.copyOf(entities), holderWorld, targetRow);
                if (newEntity == null) return existingList;

                entities.add(newEntity);
                entities.sort(Comparator.comparingInt(HolderViewerEntity::row));
                return List.copyOf(entities);
            });

            return viewerEntities;
        });
    }

    @Override
    public void remove(@NonNull UUID holder) {
        Map<UUID, List<HolderViewerEntity>> viewerEntities = holderViewerEntities.remove(holder);
        if (viewerEntities == null) return;

        viewerEntities.forEach((viewer, entities) -> destroy(holder, viewer, entities));
    }

    @Override
    public void remove(@NonNull UUID holder, @NonNull UUID viewer) {
        Map<UUID, List<HolderViewerEntity>> viewerEntities = holderViewerEntities.get(holder);
        if (viewerEntities == null) return;

        PlatformPlayerAdapter.Coordinates holderCoordinates = platformPlayerAdapter.getCoordinates(holder);
        if (holderCoordinates == null) {
            List<HolderViewerEntity> entities = viewerEntities.remove(viewer);
            if (entities != null) {
                destroy(holder, viewer, entities);
            }
            return;
        }

        viewerEntities.compute(viewer, (uuid, existing) -> {
            if (existing == null) return null;

            List<HolderViewerEntity> entitiesToDelete = existing.stream().filter(holderViewerEntity -> {
                double distance = holderViewerEntity.headText().distance();
                return holderCoordinates.distanceSquared(holderViewerEntity.coordinates()) > distance * distance;
            }).toList();

            if (entitiesToDelete.isEmpty()) return existing;

            destroy(holder, viewer, entitiesToDelete);

            return existing.stream()
                    .filter(holderViewerEntity -> !entitiesToDelete.contains(holderViewerEntity))
                    .toList();
        });
    }

    @Override
    public void setPassengers(@NonNull UUID holder, int[] entitiesIds) {
        if (entitiesIds == null) {
            platformPlayerPassengers.remove(holder);
            return;
        }

        platformPlayerPassengers.put(holder, entitiesIds);

        Map<UUID, List<HolderViewerEntity>> viewerEntities = holderViewerEntities.get(holder);
        if (viewerEntities == null) return;

        viewerEntities.forEach((viewer, entities) ->
                sendPassengers(holder, viewer, entities)
        );
    }

    @Override
    public void setScale(@NonNull UUID holder, float scale) {
        Float previousScale = holderScales.put(holder, scale);
        if (previousScale != null && previousScale == scale) return;

        Map<UUID, List<HolderViewerEntity>> viewerEntities = holderViewerEntities.get(holder);
        if (viewerEntities == null) return;

        viewerEntities.replaceAll((viewer, entities) -> {
            List<HolderViewerEntity> scaledEntities = entities.stream()
                    .map(entity -> entity.withHolderScale(scale))
                    .toList();

            scaledEntities.stream()
                    .filter(entity -> entity.isTextDisplay() && entity.headText().modern().scaleWithPlayer())
                    .forEach(entity -> {
                        float entityScale = scale(entity);
                        changeScale(viewer, entity, new Vector3f(entityScale, entityScale, entityScale));
                        changePosition(viewer, List.of(entity), entity.row());
                    });

            return scaledEntities;
        });
    }

    @Override
    public boolean isCorrectHolder(@NonNull UUID holder) {
        if (platformPlayerAdapter.isDead(holder)) return false;
        if (GameMode.SPECTATOR.name().equals(platformPlayerAdapter.getGamemode(holder))) return false;

        return !platformPlayerAdapter.hasPotionEffect(holder, PotionUtil.INVISIBILITY_POTION_NAME);
    }

    private boolean hasExpired(@NonNull List<HolderViewerEntity> entities,
                               @NonNull String holderWorld,
                               double distanceSquared,
                               boolean isSneaking,
                               boolean strictHolder,
                               long now) {
        for (HolderViewerEntity entity : entities) {
            if (isExpired(entity, holderWorld, distanceSquared, isSneaking, strictHolder, now)) {
                return true;
            }
        }

        return false;
    }

    private boolean isExpired(@NonNull HolderViewerEntity entity,
                              @NonNull String holderWorld,
                              double distanceSquared,
                              boolean isSneaking,
                              boolean strictHolder,
                              long now) {
        double distance = entity.headText().distance();
        return distanceSquared > distance * distance
                || entity.isEnd(now)
                || isSneaking && entity.headText().hideWhenSneaking()
                || !holderWorld.equals(entity.world())
                || !strictHolder;
    }

    private @Nullable HolderViewerEntity spawn(@NonNull ModuleName moduleName,
                                               @NonNull FPlayer holder,
                                               @NonNull FPlayer viewer,
                                               @NonNull HeadText headText,
                                               @NonNull Function<FPlayer, Component> component,
                                               PlatformPlayerAdapter.@NonNull Coordinates coordinates,
                                               @NonNull List<HolderViewerEntity> existingEntities,
                                               @NonNull String worldName,
                                               int row) {
        UUID holderUUID = holder.uuid();
        UUID viewerUUID = viewer.uuid();

        User packetUser = packetProvider.getUser(viewer);
        if (packetUser == null) return null;

        ClientVersion clientVersion = packetUser.getClientVersion();

        int entityId = platformServerAdapter.generateEntityId();
        EntityType entityType = headText.modern().enable() && clientVersion.isNewerThanOrEquals(ClientVersion.V_1_19_4)
                ? EntityTypes.TEXT_DISPLAY
                : EntityTypes.AREA_EFFECT_CLOUD;
        Location spawnLocation = new Location(coordinates.x(), 0, coordinates.z(), coordinates.yaw(), coordinates.pitch());

        // create entity
        packetSender.send(viewer, new WrapperPlayServerSpawnEntity(
                entityId, UUID.randomUUID(), entityType, spawnLocation, 0, 0, null
        ));

        Component message = component.apply(viewer);
        if (headText.isAuto()) {
            int countWords = componentSerializer.toPlain(message).split(" ").length;
            headText = headText.withLifeTime((long) (((countWords + 10.0) / 90) * 60) * 20);
        }

        HolderViewerEntity entity = HolderViewerEntity.builder()
                .id(entityId)
                .moduleName(moduleName)
                .clientVersion(clientVersion)
                .headText(headText)
                .type(entityType)
                .world(worldName)
                .coordinates(coordinates)
                .componentHash(message.hashCode())
                .row(row)
                .endTime(headText.calculateEndTime())
                .holderScale(holderScales.getOrDefault(holderUUID, 1.0f))
                .build();

        List<HolderViewerEntity> all = new ArrayList<>(existingEntities);
        all.add(entity);
        all.sort(Comparator.comparingInt(HolderViewerEntity::row));

        // update entity metadata
        updateEntityData(holderUUID, viewerUUID, entity, message);

        // ride holder
        sendPassengers(holderUUID, viewerUUID, all);

        if (entity.isTextDisplay() && headText.modern().animationTime() > 0) {
            float scale = scale(entity);
            taskScheduler.runAsyncLater(() -> {
                changePosition(viewerUUID, existingEntities, row);
                changeScale(viewerUUID, entity, new Vector3f(scale, scale, scale));
            }, 1L); // Mojang, why I need delay for it?
        } else {
            changePosition(viewerUUID, existingEntities, row);
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

            if (entity.isTextDisplay() && entity.headText().modern().animationTime() > 0) {
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
                    .mapToInt(entity -> entity.headText().modern().animationTime())
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

        changePosition(viewer, remaining, 0);
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

        int[] platformPassengers = platformPlayerPassengers.get(holder);
        if (platformPassengers != null) {
            holderPassengers = ArrayUtils.addAll(holderPassengers, platformPassengers);
        }

        packetSender.send(viewer, new WrapperPlayServerSetPassengers(platformPlayerAdapter.getEntityId(holder), holderPassengers), true);

        for (int i = 0; i < legacy.length; i++) {
            int[] next = i + 1 < legacy.length ? new int[]{legacy[i + 1]} : ArrayUtils.EMPTY_INT_ARRAY;

            packetSender.send(viewer, new WrapperPlayServerSetPassengers(legacy[i], next), true);
        }
    }

    private void changePosition(@NonNull UUID viewer, @NonNull List<HolderViewerEntity> entities, int start) {
        for (HolderViewerEntity entity : entities) {
            if (entity.row() < start) continue;
            if (!entity.isTextDisplay()) continue;

            List<EntityData<?>> passengerMetadata = new ArrayList<>();

            if (entity.headText().modern().animationTime() > 0) {
                // interpolation delay
                passengerMetadata.add(new EntityData<>(8, EntityDataTypes.INT, -1));

                // transformation duration
                passengerMetadata.add(new EntityData<>(9, EntityDataTypes.INT, entity.headText().modern().animationTime()));
            }

            // translation
            passengerMetadata.add(new EntityData<>(displayIndex(2), EntityDataTypes.VECTOR3F, translation(entity)));

            packetSender.send(viewer, new WrapperPlayServerEntityMetadata(entity.id(), passengerMetadata));
        }
    }

    private void changeScale(@NonNull UUID viewer, @NonNull HolderViewerEntity entity, @NonNull Vector3f scale) {
        List<EntityData<?>> metadataList = new ArrayList<>();

        // interpolation delay
        metadataList.add(new EntityData<>(8, EntityDataTypes.INT, -1));

        // transformation duration
        metadataList.add(new EntityData<>(9, EntityDataTypes.INT, entity.headText().modern().animationTime()));

        // scale
        metadataList.add(new EntityData<>(displayIndex(3), EntityDataTypes.VECTOR3F, scale));

        packetSender.send(viewer, new WrapperPlayServerEntityMetadata(entity.id(), metadataList));
    }

    private void updateEntityComponent(@NonNull UUID holder, @NonNull UUID viewer, @NonNull HolderViewerEntity entity, @NonNull Component component) {
        List<EntityData<?>> metadataList = new ArrayList<>();

        if (entity.isTextDisplay()) {
            // headText
            metadataList.add(new EntityData<>(textDisplayIndex(0), EntityDataTypes.ADV_COMPONENT, component));

            HeadText text = entity.headText();

            byte flags;
            if (!text.modern().seeThrough() || !text.modern().disableSeeThroughWhenSneaking()) {
                flags = text.modern().flags();
            } else {
                flags = text.modern().flags(!platformPlayerAdapter.isSneaking(holder));
            }

            if (flags != 0x00) {
                metadataList.add(new EntityData<>(textDisplayIndex(4), EntityDataTypes.BYTE, flags));
            }
        } else {
            // headText
            metadataList.add(new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(component)));
        }

        packetSender.send(viewer, new WrapperPlayServerEntityMetadata(entity.id(), metadataList));
    }

    private void updateEntityData(@NonNull UUID holder, @NonNull UUID viewer, @NonNull HolderViewerEntity entity, @NonNull Component component) {
        List<EntityData<?>> metadataList = new ArrayList<>();

        HeadText text = entity.headText();

        if (entity.isTextDisplay()) {
            // translation
            metadataList.add(new EntityData<>(displayIndex(2), EntityDataTypes.VECTOR3F, translation(entity)));

            // scale
            float scale = scale(entity);
            Vector3f scaleVector = text.modern().animationTime() > 0 ? Vector3f.zero() : new Vector3f(scale, scale, scale);
            metadataList.add(new EntityData<>(displayIndex(3), EntityDataTypes.VECTOR3F, scaleVector));

            // center for viewer
            HeadText.Modern.Billboard billboard = text.modern().billboard();
            if (billboard != null) {
                metadataList.add(new EntityData<>(displayIndex(6), EntityDataTypes.BYTE, billboard.id()));
            }

            // headText
            metadataList.add(new EntityData<>(textDisplayIndex(0), EntityDataTypes.ADV_COMPONENT, component));

            // width
            metadataList.add(new EntityData<>(textDisplayIndex(1), EntityDataTypes.INT, 100000));

            // background color
            metadataList.add(new EntityData<>(textDisplayIndex(2), EntityDataTypes.INT, colorConverter.parseHexToArgb(text.modern().background())));

            // flags
            byte flags;
            if (!text.modern().seeThrough() || !text.modern().disableSeeThroughWhenSneaking()) {
                flags = text.modern().flags();
            } else {
                flags = text.modern().flags(!platformPlayerAdapter.isSneaking(holder));
            }

            if (flags != 0x00) {
                metadataList.add(new EntityData<>(textDisplayIndex(4), EntityDataTypes.BYTE, flags));
            }
        } else {
            // headText
            metadataList.add(new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(component)));

            // visible
            metadataList.add(new EntityData<>(3, EntityDataTypes.BOOLEAN, true));

            // empty cloud
            metadataList.add(new EntityData<>(areaEffectCloudRadiusIndex(), EntityDataTypes.FLOAT, 0f));
        }

        packetSender.send(viewer, new WrapperPlayServerEntityMetadata(entity.id(), metadataList));
    }

    private Vector3f translation(@NonNull HolderViewerEntity entity) {
        HeadText.Modern modern = entity.headText().modern();
        float holderScale = modern.scaleWithPlayer() ? entity.holderScale() : 1.0f;

        float step = modern.space() * scale(entity);
        float offsetX = modern.offsetX() * holderScale;
        float offsetY = modern.offsetY() * holderScale + step * entity.row() + (entity.clientVersion.isNewerThanOrEquals(ClientVersion.V_1_20_2) ? 0 : LEGACY_1_19_4_OFFSET);
        float offsetZ = modern.offsetZ() * holderScale;
        return new Vector3f(offsetX, offsetY, offsetZ);
    }

    private float scale(@NonNull HolderViewerEntity entity) {
        HeadText.Modern modern = entity.headText().modern();
        return modern.scaleWithPlayer() ? modern.scale() * entity.holderScale() : modern.scale();
    }

    private int displayIndex(int field) {
        ServerVersion serverVersion = packetProvider.getServerVersion();
        return 8 + (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20_2) ? 1 : 0) + field;
    }

    private int textDisplayIndex(int field) {
        return displayIndex(14 + field);
    }

    private int areaEffectCloudRadiusIndex() {
        ServerVersion serverVersion = packetProvider.getServerVersion();
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17)) {
            return 8;
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14)) {
            return 7;
        } else {
            return 6;
        }
    }
}