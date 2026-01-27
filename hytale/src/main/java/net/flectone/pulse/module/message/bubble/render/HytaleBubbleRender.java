package net.flectone.pulse.module.message.bubble.render;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.bubble.model.Bubble;
import net.flectone.pulse.module.message.bubble.model.BubbleEntity;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

@Singleton
public class HytaleBubbleRender implements BubbleRender {

    private final Map<String, List<BubbleEntity>> activeBubbles = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessagePipeline messagePipeline;
    private final TaskScheduler taskScheduler;

    @Inject
    public HytaleBubbleRender(FileFacade fileFacade,
                              PlatformPlayerAdapter platformPlayerAdapter,
                              MessagePipeline messagePipeline,
                              TaskScheduler taskScheduler) {
        this.fileFacade = fileFacade;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.messagePipeline = messagePipeline;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void renderBubble(Bubble bubble) {
        FPlayer sender = bubble.getSender();
        if (!isCorrectPlayer(sender)) return;

        renderBubble(sender, sender, bubble);
    }

    private void renderBubble(FPlayer sender, FPlayer viewer, Bubble bubble) {
        String playerKey = sender.getUuid().toString();

        Object playerObj = platformPlayerAdapter.convertToPlatformPlayer(sender);
        if (!(playerObj instanceof PlayerRef playerRef)) return;

        UUID worldUUID = playerRef.getWorldUuid();
        if (worldUUID == null) return;

        Universe universe = Universe.get();
        if (universe == null) return;

        World world = universe.getWorld(worldUUID);
        if (world == null) return;

        Transform playerTransform = playerRef.getTransform();
        world.execute(() -> {
            List<BubbleEntity> existingBubbles = activeBubbles.getOrDefault(playerKey, new CopyOnWriteArrayList<>());
            existingBubbles.removeIf(bubbleData -> !bubbleData.entityRef().isValid());

            existingBubbles.forEach(bubbleEntity -> {
                if (!bubbleEntity.entityRef().isValid()) return;

                EntityStore entityStore = bubbleEntity.entityRef().getStore().getExternalData();

                TransformComponent transform = entityStore.getStore().getComponent(bubbleEntity.entityRef(), TransformComponent.getComponentType());
                if (transform != null) {
                    Vector3d currentPosition = transform.getPosition();
                    Vector3d newPosition = new Vector3d(currentPosition.getX(), currentPosition.getY() + 0.5, currentPosition.getZ());
                    TransformComponent newTransform = new TransformComponent(newPosition, transform.getRotation());
                    entityStore.getStore().putComponent(bubbleEntity.entityRef(), TransformComponent.getComponentType(), newTransform);
                }

                MountedComponent mounted = entityStore.getStore().getComponent(bubbleEntity.entityRef(), MountedComponent.getComponentType());
                if (mounted != null) {
                    Vector3f currentOffset = mounted.getAttachmentOffset();
                    Vector3f newOffset = new Vector3f(currentOffset.getX(), currentOffset.getY() + 0.5f, currentOffset.getZ());
                    MountedComponent newMounted = new MountedComponent(playerRef.getReference(), newOffset, mounted.getControllerType());
                    entityStore.getStore().putComponent(bubbleEntity.entityRef(), MountedComponent.getComponentType(), newMounted);
                }
            });

            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

            ProjectileComponent projectileComponent = new ProjectileComponent("Projectile");
            holder.putComponent(ProjectileComponent.getComponentType(), projectileComponent);

            double baseHeight = 2.0 + (bubble.getElevation() * 0.3);
            holder.putComponent(TransformComponent.getComponentType(),
                    new TransformComponent(
                            playerTransform.getPosition().add(new Vector3d(0, baseHeight, 0)).clone(),
                            playerTransform.getRotation().clone()
                    )
            );

            holder.ensureComponent(UUIDComponent.getComponentType());
            holder.ensureComponent(Intangible.getComponentType());

            if (projectileComponent.getProjectile() == null) {
                projectileComponent.initialize();
                if (projectileComponent.getProjectile() == null) {
                    return;
                }
            }

            holder.putComponent(NetworkId.getComponentType(), new NetworkId((world.getEntityStore().getStore().getExternalData()).takeNextNetworkId()));
            holder.putComponent(Nameplate.getComponentType(), new Nameplate(PlainTextComponentSerializer.plainText().serialize(createFormattedMessage(bubble, viewer))));
            holder.putComponent(MountedComponent.getComponentType(),
                    new MountedComponent(
                            playerRef.getReference(),
                            new Vector3f(0.0F, (float)baseHeight, 0.0F),
                            MountController.Minecart
                    )
            );

            Ref<EntityStore> newBubbleRef = world.getEntityStore().getStore().addEntity(holder, AddReason.SPAWN);

            BubbleEntity newBubbleData = new BubbleEntity(newBubbleRef, bubble, System.currentTimeMillis() + bubble.getDuration());

            existingBubbles.addFirst(newBubbleData);
            activeBubbles.put(playerKey, existingBubbles);

            scheduleBubbleRemoval(newBubbleData, playerKey);
        });
    }

    private void scheduleBubbleRemoval(BubbleEntity bubbleData, String playerKey) {
        long delay = bubbleData.expiryTime() - System.currentTimeMillis();
        if (delay <= 0) {
            removeBubble(bubbleData, playerKey);
            return;
        }

        taskScheduler.runAsyncLater(() -> removeBubble(bubbleData, playerKey), delay / 50 + 10);
    }

    private void removeBubble(BubbleEntity bubbleData, String playerKey) {
        if (bubbleData.entityRef().isValid()) {
            EntityStore entityStore = bubbleData.entityRef().getStore().getExternalData();
            entityStore.getWorld().execute(() -> entityStore.getStore().removeEntity(bubbleData.entityRef(), RemoveReason.REMOVE));
        }

        List<BubbleEntity> bubbles = activeBubbles.get(playerKey);
        if (bubbles != null) {
            bubbles.remove(bubbleData);
            if (bubbles.isEmpty()) {
                activeBubbles.remove(playerKey);
            }
        }
    }

    private Component createFormattedMessage(Bubble bubble, FPlayer viewer) {
        Localization.Message.Bubble localization = fileFacade.localization(viewer).message().bubble();

        MessageContext messageContext = messagePipeline.createContext(bubble.getSender(), viewer, bubble.getRawMessage())
                .addFlags(
                        new MessageFlag[]{MessageFlag.MENTION, MessageFlag.INTERACTIVE_CHAT, MessageFlag.QUESTION},
                        new boolean[]{false, false, false}
                );

        Component message = messagePipeline.build(messageContext);

        return messagePipeline.build(messageContext
                .withMessage(localization.format())
                .addTagResolver(TagResolver.resolver("message", (argumentQueue, context) -> Tag.inserting(message)))
        );
    }

    @Override
    public void removeBubbleIf(Predicate<Bubble> bubblePredicate) {
        List<String> keysToRemove = new ArrayList<>();
        List<BubbleEntity> bubblesToRemove = new ArrayList<>();

        activeBubbles.forEach((key, bubbleDataList) -> bubbleDataList.stream()
                .filter(bubbleEntity -> bubblePredicate.test(bubbleEntity.bubble()))
                .forEach(bubbleEntity -> {
                    bubblesToRemove.add(bubbleEntity);
                    if (!keysToRemove.contains(key)) {
                        keysToRemove.add(key);
                    }
                })
        );

        bubblesToRemove.forEach(bubbleEntity -> {
            String playerKey = null;
            for (Map.Entry<String, List<BubbleEntity>> entry : activeBubbles.entrySet()) {
                if (entry.getValue().contains(bubbleEntity)) {
                    playerKey = entry.getKey();
                    break;
                }
            }

            if (playerKey != null) {
                removeBubble(bubbleEntity, playerKey);
            }
        });
    }

    @Override
    public void removeAllBubbles() {
        activeBubbles.forEach((key, bubbleDataList) -> bubbleDataList.stream()
                .filter(bubbleEntity -> bubbleEntity.entityRef().isValid())
                .forEach(bubbleEntity -> {
                    EntityStore entityStore = bubbleEntity.entityRef().getStore().getExternalData();
                    entityStore.getWorld().execute(() ->
                            entityStore.getStore().removeEntity(bubbleEntity.entityRef(), RemoveReason.REMOVE)
                    );
                })
        );

        activeBubbles.clear();
    }

    @Override
    public boolean isCorrectPlayer(FPlayer sender) {
        Object playerObj = platformPlayerAdapter.convertToPlatformPlayer(sender);
        if (!(playerObj instanceof PlayerRef)) return false;

        List<Integer> passengers = platformPlayerAdapter.getPassengers(sender.getUuid());
        return passengers.isEmpty();
    }

    @Override
    public boolean isModern() {
        return true;
    }

    @Override
    public boolean isInteractionRiding() {
        return false;
    }
}