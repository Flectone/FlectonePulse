package net.flectone.pulse.platform.render;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.flectone.pulse.FlectonePulseAPI;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.HeadText;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.serializer.ComponentSerializer;
import net.kyori.adventure.text.Component;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleHeadTextRender implements HeadTextRender {

    private static final double SQUARED_UPDATE_RADIUS = 32 * 32;

    private final Map<UUID, List<HolderEntity>> activeBubbles = new ConcurrentHashMap<>();

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ComponentSerializer componentSerializer;
    private final TaskScheduler taskScheduler;

    @With
    @Builder(toBuilder = true)
    private record HolderEntity(
            @NonNull ModuleName moduleName,
            @NonNull Ref<EntityStore> entityRef,
            @NonNull World world,
            @NonNull HeadText headText,
            PlatformPlayerAdapter.@NonNull Coordinates coordinates,
            int componentHash,
            int row,
            long endTime
    ) {

        private boolean isEnd(long now) {
            return !headText.isPermanent() && now > endTime;
        }

    }

    @Override
    public void reload() {
        removeAll();

        taskScheduler.runAsyncTimer(() -> {
            if (activeBubbles.isEmpty()) return;

            long now = System.currentTimeMillis();

            activeBubbles.forEach((holder, current) -> {
                PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(holder);

                if (coordinates == null
                        || !(platformPlayerAdapter.convertToPlatformPlayer(holder) instanceof PlayerRef playerRef)
                        || playerRef.getReference() == null
                        || !isCorrectHolder(holder)) {
                    remove(holder);
                    return;
                }

                boolean isSneaking = platformPlayerAdapter.isSneaking(holder);

                boolean needUpdate = current.stream()
                        .anyMatch(entity -> isExpired(entity, isSneaking, now) || needsPositionSync(entity, coordinates));
                if (!needUpdate) return;

                World world = playerRef.getReference().getStore().getExternalData().getWorld();
                world.execute(() -> {
                    List<HolderEntity> expired = new ArrayList<>();

                    activeBubbles.computeIfPresent(holder, (uuid, existing) -> {
                        List<HolderEntity> alive = new ArrayList<>(existing.size());

                        boolean synced = false;
                        for (HolderEntity entity : existing) {
                            if (isExpired(entity, isSneaking, now)) {
                                expired.add(entity);
                            } else {
                                if (needsPositionSync(entity, coordinates)) {
                                    entity = entity.withCoordinates(coordinates);
                                    syncPosition(entity, playerRef);
                                    synced = true;
                                }

                                alive.add(entity.withRow(alive.size()));
                            }
                        }

                        if (expired.isEmpty()) {
                            if (synced) {
                                return List.copyOf(alive);
                            }

                            return existing;
                        }

                        if (alive.isEmpty()) return null;

                        alive.forEach(entity -> updateEntityPosition(entity, playerRef));

                        return List.copyOf(alive);
                    });

                    expired.forEach(entity -> removeEntityNow(entity.entityRef()));
                });
            });
        }, 1L, 1L);
    }

    @Override
    public void removeAll() {
        activeBubbles.keySet().forEach(this::remove);
    }

    @Override
    public void render(@NonNull ModuleName moduleName,
                       @NonNull FPlayer holder,
                       @NonNull Set<FPlayer> receivers,
                       @NonNull Function<FPlayer, Component> component,
                       @NonNull HeadText headText) {
        PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(holder);
        if (coordinates == null) return;

        render(moduleName, holder, component, headText, coordinates);
    }

    @Override
    public void render(@NonNull ModuleName moduleName,
                       @NonNull FPlayer holder,
                       @NonNull FPlayer viewer,
                       @NonNull Function<FPlayer, Component> component,
                       @NonNull HeadText headText) {
        PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(holder);
        if (coordinates == null) return;

        render(moduleName, holder, component, headText, coordinates);
    }

    @Override
    public void render(@NonNull ModuleName moduleName,
                       @NonNull FPlayer holder,
                       @NonNull FPlayer viewer,
                       @NonNull Function<FPlayer, Component> component,
                       @NonNull HeadText headText,
                       PlatformPlayerAdapter.@NonNull Coordinates holderCoordinates,
                       @NonNull String holderWorld) {
        render(moduleName, holder, component, headText, holderCoordinates);
    }

    private void render(@NonNull ModuleName moduleName,
                        @NonNull FPlayer holder,
                        @NonNull Function<FPlayer, Component> component,
                        @NonNull HeadText headText,
                        PlatformPlayerAdapter.@NonNull Coordinates holderCoordinates) {
        if (FlectonePulseAPI.isDisabling()) return;
        if (!isCorrectHolder(holder.uuid())) return;
        if (headText.hideWhenSneaking() && platformPlayerAdapter.isSneaking(holder.uuid())) return;
        if (!(platformPlayerAdapter.convertToPlatformPlayer(holder) instanceof PlayerRef playerRef)) return;

        Ref<EntityStore> storeRef = playerRef.getReference();
        if (storeRef == null) return;

        World world = storeRef.getStore().getExternalData().getWorld();

        Component message = component.apply(holder);
        String bubbleText = componentSerializer.toPlain(message);
        int componentHash = message.hashCode();

        HeadText finalHeadText = headText;
        if (headText.isAuto()) {
            int countWords = bubbleText.split(" ").length;
            finalHeadText = headText.withLifeTime((long) (((countWords + 10.0) / 90) * 60) * 20);
        }

        HeadText text = finalHeadText;
        Transform playerTransform = playerRef.getTransform();
        world.execute(() -> activeBubbles.compute(holder.uuid(), (uuid, existing) -> {
            List<HolderEntity> holderEntities = existing == null
                    ? new ArrayList<>()
                    : new ArrayList<>(existing);

            int targetRow = text.row();
            if (!text.movable()) {
                for (int i = 0; i < holderEntities.size(); i++) {
                    HolderEntity entity = holderEntities.get(i);
                    if (entity.moduleName() != moduleName || entity.row() != targetRow) continue;
                    if (!entity.entityRef().isValid()) continue;

                    if (entity.componentHash() == componentHash) {
                        return existing;
                    }

                    entity = entity.toBuilder()
                            .headText(text)
                            .endTime(text.calculateEndTime())
                            .componentHash(componentHash)
                            .build();

                    holderEntities.set(i, entity);
                    updateEntityComponent(entity, bubbleText);
                    return List.copyOf(holderEntities);
                }
            }

            long count = holderEntities.stream()
                    .filter(entity -> entity.moduleName() == moduleName)
                    .count();
            if (count >= text.maxCount()) return existing;

            targetRow = shiftEntitiesAndGetRow(targetRow, holderEntities);

            long endTime = text.calculateEndTime();
            Ref<EntityStore> entityStoreRef = spawn(world, playerRef, playerTransform, text, bubbleText, endTime);
            if (entityStoreRef == null) return existing;

            holderEntities.add(HolderEntity.builder()
                    .moduleName(moduleName)
                    .entityRef(entityStoreRef)
                    .world(world)
                    .headText(text)
                    .coordinates(holderCoordinates)
                    .componentHash(componentHash)
                    .row(targetRow)
                    .endTime(endTime)
                    .build()
            );
            holderEntities.sort(Comparator.comparingInt(HolderEntity::row));

            holderEntities.forEach(entity -> updateEntityPosition(entity, playerRef));

            return List.copyOf(holderEntities);
        }));
    }

    @Override
    public void remove(@NonNull UUID holder) {
        List<HolderEntity> holderEntities = activeBubbles.remove(holder);
        if (holderEntities == null) return;

        removeEntities(holderEntities);
    }

    @Override
    public void remove(@NonNull UUID holder, @NonNull UUID viewer) {
        // entities are common for all viewers in Hytale
    }

    @Override
    public void dispose(@NonNull UUID holder) {
        remove(holder);
    }

    @Override
    public void setPassengers(@NonNull UUID holder, int[] entitiesIds) {
        // Hytale uses MountedComponent, passengers are not synchronized manually
    }

    @Override
    public void setScale(@NonNull UUID holder, float scale) {
    }

    @Override
    public boolean isCorrectHolder(@NonNull UUID holder) {
        Object playerObj = platformPlayerAdapter.convertToPlatformPlayer(holder);
        if (!(playerObj instanceof PlayerRef)) return false;

        List<Integer> passengers = platformPlayerAdapter.getPassengers(holder);
        return passengers.isEmpty();
    }

    private boolean isExpired(@NonNull HolderEntity entity, boolean isSneaking, long now) {
        return !entity.entityRef().isValid()
                || entity.isEnd(now)
                || isSneaking && entity.headText().hideWhenSneaking();
    }

    private boolean needsPositionSync(@NonNull HolderEntity entity, PlatformPlayerAdapter.@NonNull Coordinates holderCoordinates) {
        return entity.coordinates().distanceSquared(holderCoordinates) > SQUARED_UPDATE_RADIUS;
    }

    private @Nullable Ref<EntityStore> spawn(@NonNull World world,
                                             @NonNull PlayerRef playerRef,
                                             @NonNull Transform playerTransform,
                                             @NonNull HeadText text,
                                             @NonNull String bubbleText,
                                             long endTime) {
        Holder<EntityStore> entityStoreHolder = EntityStore.REGISTRY.newHolder();

        ProjectileComponent projectileComponent = new ProjectileComponent("Projectile");
        entityStoreHolder.putComponent(ProjectileComponent.getComponentType(), projectileComponent);

        Vector3d playerPosition = playerTransform.getPosition();

        entityStoreHolder.putComponent(TransformComponent.getComponentType(),
                new TransformComponent(
                        new Vector3d(playerPosition.x(), playerPosition.y() + text.modern().offsetY(), playerPosition.z()),
                        playerTransform.getRotation().clone()
                )
        );

        entityStoreHolder.ensureComponent(UUIDComponent.getComponentType());
        entityStoreHolder.ensureComponent(Intangible.getComponentType());

        if (projectileComponent.getProjectile() == null) {
            projectileComponent.initialize();
            if (projectileComponent.getProjectile() == null) {
                return null;
            }
        }

        // we will add despawn component automatically if our removal does not work correctly
        if (!text.isPermanent()) {
            entityStoreHolder.putComponent(DespawnComponent.getComponentType(), new DespawnComponent(Instant.ofEpochMilli(endTime).plus(5, ChronoUnit.SECONDS)));
        }

        entityStoreHolder.putComponent(NetworkId.getComponentType(), new NetworkId((world.getEntityStore().getStore().getExternalData()).takeNextNetworkId()));
        entityStoreHolder.putComponent(Nameplate.getComponentType(), new Nameplate(bubbleText));
        entityStoreHolder.putComponent(MountedComponent.getComponentType(),
                new MountedComponent(
                        playerRef.getReference(),
                        new Vector3f(0.0F, 0.0F, 0.0F),
                        MountController.Minecart
                )
        );

        return world.getEntityStore().getStore().addEntity(entityStoreHolder, AddReason.SPAWN);
    }

    private int shiftEntitiesAndGetRow(int targetRow, @NonNull List<HolderEntity> entities) {
        if (entities.isEmpty()) return targetRow;

        boolean shifting = false;
        for (int i = 0; i < entities.size(); i++) {
            HolderEntity entity = entities.get(i);

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

            entities.set(i, entity.withRow(entity.row() + 1));
        }

        return targetRow;
    }

    private void updateEntityComponent(@NonNull HolderEntity holderEntity, @NonNull String bubbleText) {
        Ref<EntityStore> ref = holderEntity.entityRef();
        if (!ref.isValid()) return;

        ref.getStore().putComponent(ref, Nameplate.getComponentType(), new Nameplate(bubbleText));
    }

    private void updateEntityPosition(@NonNull HolderEntity holderEntity, @NonNull PlayerRef playerRef) {
        Ref<EntityStore> ref = holderEntity.entityRef();
        if (!ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();

        MountedComponent mounted = store.getComponent(ref, MountedComponent.getComponentType());
        if (mounted == null) return;

        HeadText.Modern modern = holderEntity.headText().modern();
        float offsetX = modern.offsetX();
        float offsetY = modern.offsetY() + modern.space() * holderEntity.row();
        float offsetZ = modern.offsetZ();

        store.putComponent(
                ref,
                MountedComponent.getComponentType(),
                new MountedComponent(playerRef.getReference(), new Vector3f(offsetX, offsetY, offsetZ), mounted.getControllerType())
        );
    }

    private void syncPosition(@NonNull HolderEntity holderEntity, @NonNull PlayerRef playerRef) {
        Ref<EntityStore> ref = holderEntity.entityRef();
        if (!ref.isValid()) return;

        TransformComponent transform = ref.getStore().getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return;

        Vector3d position = playerRef.getTransform().getPosition();
        transform.setPosition(new Vector3d(position.x(), position.y() + holderEntity.headText().modern().offsetY(), position.z()));
    }

    private void removeEntities(@NonNull List<HolderEntity> holderEntities) {
        for (HolderEntity holderEntity : holderEntities) {
            Ref<EntityStore> ref = holderEntity.entityRef();
            holderEntity.world().execute(() -> removeEntityNow(ref));
        }
    }

    private void removeEntityNow(@NonNull Ref<EntityStore> ref) {
        if (!ref.isValid()) return;

        try {
            ref.getStore().removeEntity(ref, RemoveReason.REMOVE);
        } catch (IllegalStateException _) {
        }
    }

}