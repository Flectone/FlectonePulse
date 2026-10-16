package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
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
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.entity.HeadText;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.serializer.ComponentSerializer;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleHeadTextService implements HeadTextService {

    private final Map<UUID, List<HolderEntity>> activeBubbles = new ConcurrentHashMap<>();

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ComponentSerializer componentSerializer;
    private final TaskScheduler taskScheduler;

    @With
    private record HolderEntity(
            Ref<EntityStore> entityRef,
            World world,
            HeadText text,
            int row
    ) {
    }

    @Override
    public void reload() {
        removeAll();

        taskScheduler.runAsyncTimer(this::tick, 20L, 20L);
    }

    @Override
    public void show(@NonNull HeadText text) {
        FPlayer holder = text.holder();
        if (!platformPlayerAdapter.getPassengers(holder).isEmpty()) return;
        if (!(platformPlayerAdapter.convertToPlatformPlayer(holder) instanceof PlayerRef playerRef)) return;

        Ref<EntityStore> storeRef = playerRef.getReference();
        if (storeRef == null) return;

        World world = storeRef.getStore().getExternalData().getWorld();

        String bubbleText = componentSerializer.toPlain(text.component().apply(holder));

        Transform playerTransform = playerRef.getTransform();
        world.execute(() -> activeBubbles.compute(holder.uuid(), (uuid, existing) -> {
            List<HolderEntity> holderEntities = existing == null
                    ? new ArrayList<>()
                    : new ArrayList<>(existing);

            long count = holderEntities.stream()
                    .filter(entity -> entity.text().row() == text.row())
                    .count();
            if (count >= text.maxCount()) return existing;

            int targetRow = shiftEntitiesAndGetRow(text, holderEntities);

            Ref<EntityStore> entityStoreRef = spawn(world, playerRef, playerTransform, text, bubbleText);
            if (entityStoreRef == null) return existing;

            holderEntities.add(new HolderEntity(entityStoreRef, world, text, targetRow));
            holderEntities.sort(Comparator.comparingInt(HolderEntity::row));

            float baseOffset = holderEntities.getFirst().text().offset();
            holderEntities.forEach(entity -> updateEntityPosition(entity, baseOffset, playerRef));

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
    public void removeAll() {
        activeBubbles.keySet().forEach(this::remove);
    }

    @Override
    public boolean isCorrectHolder(@NonNull UUID holder) {
        Object playerObj = platformPlayerAdapter.convertToPlatformPlayer(holder);
        if (!(playerObj instanceof PlayerRef)) return false;

        List<Integer> passengers = platformPlayerAdapter.getPassengers(holder);
        return passengers.isEmpty();
    }

    private void tick() {
        long now = System.currentTimeMillis();

        activeBubbles.keySet().forEach(holder -> {
            List<HolderEntity> current = activeBubbles.get(holder);
            if (current == null) return;

            boolean needUpdate = current.stream()
                    .anyMatch(entity -> !entity.entityRef().isValid() || entity.text().isEnd(now));
            if (!needUpdate) return;

            if (!(platformPlayerAdapter.convertToPlatformPlayer(holder) instanceof PlayerRef playerRef)
                    || playerRef.getReference() == null) {
                remove(holder);
                return;
            }

            World world = playerRef.getReference().getStore().getExternalData().getWorld();
            world.execute(() -> {
                List<HolderEntity> toDelete = new ArrayList<>();

                activeBubbles.compute(holder, (uuid, existing) -> {
                    if (existing == null) return null;

                    List<HolderEntity> remaining = new ArrayList<>();
                    for (HolderEntity entity : existing) {
                        if (!entity.entityRef().isValid() || entity.text().isEnd(now)) {
                            toDelete.add(entity);
                        } else {
                            remaining.add(entity);
                        }
                    }

                    if (toDelete.isEmpty()) return existing;
                    if (remaining.isEmpty()) return null;

                    List<HolderEntity> compacted = new ArrayList<>(remaining.size());
                    for (int i = 0; i < remaining.size(); i++) {
                        compacted.add(remaining.get(i).withRow(i));
                    }

                    float baseOffset = compacted.getFirst().text().offset();
                    compacted.forEach(entity -> updateEntityPosition(entity, baseOffset, playerRef));

                    return List.copyOf(compacted);
                });

                toDelete.forEach(holderEntity -> removeEntityNow(holderEntity.entityRef()));
            });
        });
    }

    private @Nullable Ref<EntityStore> spawn(@NonNull World world, @NonNull PlayerRef playerRef, @NonNull Transform playerTransform, @NonNull HeadText text, @NonNull String bubbleText) {
        Holder<EntityStore> entityStoreHolder = EntityStore.REGISTRY.newHolder();

        ProjectileComponent projectileComponent = new ProjectileComponent("Projectile");
        entityStoreHolder.putComponent(ProjectileComponent.getComponentType(), projectileComponent);

        Vector3d playerPosition = playerTransform.getPosition();

        entityStoreHolder.putComponent(TransformComponent.getComponentType(),
                new TransformComponent(
                        new Vector3d(playerPosition.x(), playerPosition.y() + text.offset(), playerPosition.z()),
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
            entityStoreHolder.putComponent(DespawnComponent.getComponentType(), new DespawnComponent(Instant.now().plusMillis(text.animationTime()).plus(5, ChronoUnit.SECONDS)));
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

    private int shiftEntitiesAndGetRow(@NonNull HeadText text, @NonNull List<HolderEntity> entities) {
        int targetRow = text.row();
        if (entities.isEmpty()) return targetRow;

        boolean shifting = false;
        for (int i = 0; i < entities.size(); i++) {
            HolderEntity entity = entities.get(i);

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

    private void updateEntityPosition(@NonNull HolderEntity holderEntity, float baseOffset, @NonNull PlayerRef playerRef) {
        Ref<EntityStore> ref = holderEntity.entityRef();
        if (!ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();

        MountedComponent mounted = store.getComponent(ref, MountedComponent.getComponentType());
        if (mounted == null) return;

        float offsetY = baseOffset + holderEntity.text().space() * holderEntity.row();

        store.putComponent(
                ref,
                MountedComponent.getComponentType(),
                new MountedComponent(playerRef.getReference(), new Vector3f(0.0F, offsetY, 0.0F), mounted.getControllerType())
        );
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