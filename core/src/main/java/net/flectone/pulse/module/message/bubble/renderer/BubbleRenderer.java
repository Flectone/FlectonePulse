package net.flectone.pulse.module.message.bubble.renderer;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.model.Bubble;
import net.flectone.pulse.module.message.bubble.model.BubbleEntity;
import net.flectone.pulse.module.message.bubble.model.ModernBubble;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Responsible for rendering bubbles above players' heads
 */
@Singleton
public class BubbleRenderer {
    
    private final Map<String, Deque<BubbleEntity>> activeBubbleEntities = new ConcurrentHashMap<>();
    
    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final MessagePipeline messagePipeline;
    private final IntegrationModule integrationModule;
    private final TaskScheduler taskScheduler;
    private final RandomUtil randomUtil;
    
    @Inject
    public BubbleRenderer(FileResolver fileResolver,
                          FPlayerService fPlayerService,
                          PlatformPlayerAdapter platformPlayerAdapter,
                          PacketSender packetSender,
                          PacketProvider packetProvider,
                          MessagePipeline messagePipeline,
                          IntegrationModule integrationModule,
                          TaskScheduler taskScheduler,
                          RandomUtil randomUtil) {
        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.packetSender = packetSender;
        this.packetProvider = packetProvider;
        this.messagePipeline = messagePipeline;
        this.integrationModule = integrationModule;
        this.taskScheduler = taskScheduler;
        this.randomUtil = randomUtil;
    }

    public void renderBubble(Bubble bubble) {
        FPlayer sender = bubble.getSender();
        if (!isCorrectPlayer(sender)) return;

        Message.Bubble config = fileResolver.getMessage().getBubble();
        double viewDistance = config.getDistance();

        CompletableFuture<Set<UUID>> nearbyEntitiesFuture = new CompletableFuture<>();

        taskScheduler.runSyncRegion(platformPlayerAdapter.convertToPlatformPlayer(sender), () -> {
            Set<UUID> nearbyEntities = platformPlayerAdapter.findPlayersWhoCanSee(sender, viewDistance, viewDistance, viewDistance);
            nearbyEntitiesFuture.complete(nearbyEntities);
        });

        nearbyEntitiesFuture.thenAccept(nearbyEntities -> nearbyEntities
                .stream()
                .map(fPlayerService::getFPlayer)
                .filter(fViewer -> !fViewer.isUnknown())
                .filter(fViewer -> !fViewer.isIgnored(sender))
                .filter(fViewer -> integrationModule.isVanishedVisible(sender, fViewer))
                .forEach(fViewer -> {
                    Component formattedMessage = createFormattedMessage(bubble, fViewer);

                    String key = sender.getUuid().toString() + fViewer.getUuid();
                    Deque<BubbleEntity> bubbleEntities = activeBubbleEntities.getOrDefault(key, new ConcurrentLinkedDeque<>());

                    // create bubble entity
                    BubbleEntity bubbleEntity = createBubbleEntity(bubble, formattedMessage, fViewer);
                    bubbleEntities.push(bubbleEntity);

                    for (int i = 0; i < bubble.getElevation(); i++) {
                        bubbleEntities.push(createSpaceBubbleEntity(bubble, fViewer));
                    }

                    activeBubbleEntities.put(key, bubbleEntities);

                    rideEntities(sender, fViewer);
               }));
    }

    public void removeBubble(Bubble bubble) {
        activeBubbleEntities.forEach((uuid, bubbleEntities) -> {
            if (bubbleEntities.isEmpty()) return;

            List<BubbleEntity> bubbleEntitiesToRemove = bubbleEntities.stream()
                    .filter(bubbleEntity -> bubbleEntity.getBubble().getId() == bubble.getId())
                    .toList();

            if (bubbleEntitiesToRemove.isEmpty()) return;

            // despawn entities
            bubbleEntitiesToRemove.forEach(this::despawnBubbleEntity);

            // remove from active bubbles
            bubbleEntities.removeAll(bubbleEntitiesToRemove);

            // remove space
            rideEntities(bubble.getSender(), bubbleEntitiesToRemove.get(0).getViewer());
        });
    }

    private void rideEntities(FPlayer sender, FPlayer viewer) {
        Deque<BubbleEntity> bubbleEntities = activeBubbleEntities.get(sender.getUuid().toString() + viewer.getUuid());
        if (bubbleEntities == null) return;
        if (bubbleEntities.isEmpty()) return;
        if (!isCorrectPlayer(sender)) return;
        if (!integrationModule.isVanishedVisible(sender, viewer)) return;

        boolean hasSeenVisible = false;
        boolean hasSpawnedSpace = false;

        int lastID = platformPlayerAdapter.getEntityId(sender.getUuid());

        for (BubbleEntity bubbleEntity : bubbleEntities) {
            if (bubbleEntity.isVisible()) {
                hasSpawnedSpace = false;
                hasSeenVisible = true;
            } else if (hasSeenVisible && hasSpawnedSpace) {
                continue;
            }

            spawnEntity(bubbleEntity);

            lastID = rideEntity(bubbleEntity, lastID, new int[]{bubbleEntity.getId()});

            if (!bubbleEntity.isVisible() && hasSeenVisible) {
                hasSpawnedSpace = true;
            }
        }
    }

    private int rideEntity(BubbleEntity nextBubbleEntity, int entityId, int[] passengersIds) {
        packetSender.send(nextBubbleEntity.getViewer(), new WrapperPlayServerSetPassengers(entityId, passengersIds));

        return nextBubbleEntity.getId();
    }
    
    private Component createFormattedMessage(Bubble bubble, FPlayer viewer) {
        Localization.Message.Bubble localization = fileResolver.getLocalization(viewer).getMessage().getBubble();

        Component message = messagePipeline.builder(bubble.getSender(), viewer, bubble.getRawMessage())
                .flag(MessageFlag.USER_MESSAGE, true)
                .flag(MessageFlag.MENTION, false)
                .flag(MessageFlag.INTERACTIVE_CHAT, false)
                .flag(MessageFlag.QUESTION, false)
                .build();

        
        return messagePipeline.builder(bubble.getSender(), viewer, localization.getFormat())
                .flag(MessageFlag.MENTION, false)
                .flag(MessageFlag.INTERACTIVE_CHAT, false)
                .flag(MessageFlag.QUESTION, false)
                .tagResolvers(TagResolver.resolver("message", (argumentQueue, context) -> Tag.inserting(message)))
                .build();
    }
    
    private BubbleEntity createBubbleEntity(Bubble bubble, Component formattedMessage, FPlayer viewer) {
        int id = randomUtil.nextInt(Integer.MAX_VALUE);

        EntityType entityType = bubble instanceof ModernBubble
                ? EntityTypes.TEXT_DISPLAY
                : EntityTypes.AREA_EFFECT_CLOUD;

        return new BubbleEntity(id, entityType, bubble, viewer, formattedMessage);
    }

    private BubbleEntity createSpaceBubbleEntity(Bubble bubble, FPlayer viewer) {
        int spaceEntityId = randomUtil.nextInt(Integer.MAX_VALUE);

        EntityType spaceBubbleEntityType = bubble.isInteractionRiding()
                ? EntityTypes.INTERACTION
                : EntityTypes.AREA_EFFECT_CLOUD;

        return new BubbleEntity(spaceEntityId, spaceBubbleEntityType, bubble, viewer, Component.empty(), false);
    }

    private void despawnBubbleEntity(BubbleEntity bubbleEntity) {
        EntityType entityType = bubbleEntity.getEntityType();
        int despawnDelay = 0;
        if (entityType == EntityTypes.TEXT_DISPLAY && bubbleEntity.getBubble() instanceof ModernBubble bubble) {
            interpolate(bubbleEntity, bubble, new Vector3f());
            despawnDelay = bubble.getAnimationTime();
        }

        taskScheduler.runAsyncLater(() -> packetSender.send(bubbleEntity.getViewer(), new WrapperPlayServerDestroyEntities(bubbleEntity.getId())), despawnDelay);
    }

    public void removeAllBubbles() {
        activeBubbleEntities.values().forEach(entities -> 
                entities.forEach(this::despawnBubbleEntity));
        activeBubbleEntities.clear();
    }

    private void spawnEntity(BubbleEntity bubbleEntity) {
        if (bubbleEntity.isCreated()) return;

        Location location = platformPlayerAdapter.getLocation(bubbleEntity.getBubble().getSender());
        if (location == null) return;

        location.setPosition(location.getPosition().add(0, 1.8, 0));

        int id = bubbleEntity.getId();
        EntityType entityType = bubbleEntity.getEntityType();

        packetSender.send(bubbleEntity.getViewer(), new WrapperPlayServerSpawnEntity(
                id, UUID.randomUUID(), entityType, location, 0, 0, null
        ));

        List<EntityData<?>> metadataList = createEntityData(bubbleEntity);

        packetSender.send(bubbleEntity.getViewer(), new WrapperPlayServerEntityMetadata(id, metadataList));

        bubbleEntity.setCreated(true);
        bubbleEntity.getBubble().setCreated(true);

        taskScheduler.runAsyncLater(() -> {
            if (entityType == EntityTypes.TEXT_DISPLAY && bubbleEntity.getBubble() instanceof ModernBubble bubble) {
                interpolate(bubbleEntity, bubble, new Vector3f(bubble.getScale(), bubble.getScale(), bubble.getScale()));
            }
        }, 1);
    }

    private void interpolate(BubbleEntity bubbleEntity, ModernBubble bubble, Vector3f scale) {
        List<EntityData<?>> metadataList = new ArrayList<>();

        // interpolation delay
        metadataList.add(new EntityData<>(8, EntityDataTypes.INT, -1));

        // transformation duration
        metadataList.add(new EntityData<>(9, EntityDataTypes.INT, bubble.getAnimationTime()));

        // scale
        metadataList.add(new EntityData<>(12, EntityDataTypes.VECTOR3F, scale));

        packetSender.send(bubbleEntity.getViewer(), new WrapperPlayServerEntityMetadata(bubbleEntity.getId(), metadataList));
    }


    private List<EntityData<?>> createEntityData(BubbleEntity bubbleEntity) {
        List<EntityData<?>> metadataList = new ArrayList<>();

        EntityType entityType = bubbleEntity.getEntityType();

        if (entityType == EntityTypes.TEXT_DISPLAY && bubbleEntity.getBubble() instanceof ModernBubble bubble) {
            // scale
            metadataList.add(new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f()));

            // center for viewer
            metadataList.add(new EntityData<>(15, EntityDataTypes.BYTE, (byte) bubble.getBillboard().ordinal()));

            // text
            Component message = bubbleEntity.getMessage();
            metadataList.add(new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, message));

            // width
            metadataList.add(new EntityData<>(24, EntityDataTypes.INT, 100000));

            // background color
            int backgroundColor = bubble.getBackground();
            metadataList.add(new EntityData<>(25, EntityDataTypes.INT, backgroundColor));

            if (bubble.isHasShadow()) {
                metadataList.add(new EntityData<>(27, EntityDataTypes.BYTE, (byte) 0x01));
            }

            return metadataList;
        }

        if (entityType == EntityTypes.AREA_EFFECT_CLOUD) {
            // text
            Component message = bubbleEntity.getMessage();
            metadataList.add(new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(message)));

            // custom name visible
            boolean visibleName = bubbleEntity.isVisible();
            metadataList.add(new EntityData<>(3, EntityDataTypes.BOOLEAN, visibleName));

            // radius
            int radiusIndex = 8;

            if (packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
                radiusIndex = 7;
            }

            metadataList.add(new EntityData<>(radiusIndex, EntityDataTypes.FLOAT, 0f));

            return metadataList;
        }

        if (entityType == EntityTypes.INTERACTION) {
            // width
            metadataList.add(new EntityData<>(8, EntityDataTypes.FLOAT, (float) 0.000001));

            Bubble bubble = bubbleEntity.getBubble();

            // height
            float height = bubble.getInteractionHeight();
            metadataList.add(new EntityData<>(9, EntityDataTypes.FLOAT, height));

            return metadataList;
        }

        return metadataList;
    }

    public boolean isCorrectPlayer(FPlayer sender) {
        List<Integer> passengers = platformPlayerAdapter.getPassengers(sender);

        return platformPlayerAdapter.getGamemode(sender) != GameMode.SPECTATOR
                && !platformPlayerAdapter.hasPotionEffect(sender, PotionTypes.INVISIBILITY)
                && passengers.isEmpty();
    }
}