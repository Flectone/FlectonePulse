package net.flectone.pulse.module.message.bubble.render;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.MinecraftBubbleEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.model.Bubble;
import net.flectone.pulse.module.message.bubble.model.ModernBubble;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.render.TextScreenRender;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.MinecraftEntityUtil;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.PotionUtil;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;

/**
 * Responsible for rendering bubbles above players' heads
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftBubbleRender implements BubbleRender {

    private final Map<String, Deque<MinecraftBubbleEntity>> activeBubbleEntities = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformServerAdapter platformServerAdapter;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MinecraftPacketSender packetSender;
    private final MessagePipeline messagePipeline;
    private final IntegrationModule integrationModule;
    private final TaskScheduler taskScheduler;
    private final MinecraftEntityUtil entityUtil;
    private final MinecraftPacketProvider packetProvider;
    private final TextScreenRender textScreenRender;

    @Override
    public void renderBubble(Bubble bubble) {
        FPlayer sender = bubble.getSender();
        if (!isCorrectPlayer(sender)) return;

        Message.Bubble config = fileFacade.message().bubble();
        double viewDistance = config.distance();

        CompletableFuture<Set<UUID>> nearbyEntitiesFuture = new CompletableFuture<>();

        taskScheduler.runRegion(sender, () -> {
            Set<UUID> nearbyEntities = platformPlayerAdapter.findPlayersWhoCanSee(sender, viewDistance, viewDistance, viewDistance);
            nearbyEntitiesFuture.complete(nearbyEntities);
        }, true);

        nearbyEntitiesFuture.thenAcceptAsync(nearbyEntities -> nearbyEntities
                .stream()
                .map(fPlayerService::getFPlayer)
                .filter(fViewer -> config.visibleToSelf() || !fViewer.equals(sender))
                .filter(fViewer -> !bubble.getViewers().isEmpty() && bubble.getViewers().contains(fViewer))
                .filter(fViewer -> !fViewer.isUnknown())
                .filter(fViewer -> !fViewer.isIgnored(sender))
                .filter(fViewer -> integrationModule.canSeeVanished(sender, fViewer))
                .forEach(fViewer -> renderBubble(fViewer, bubble))
        );
    }

    public void renderBubble(FPlayer fViewer, Bubble bubble) {
        Component formattedMessage = createFormattedMessage(bubble, fViewer);

        FPlayer sender = bubble.getSender();
        String key = sender.uuid().toString() + fViewer.uuid();
        Deque<MinecraftBubbleEntity> bubbleEntities = activeBubbleEntities.getOrDefault(key, new ConcurrentLinkedDeque<>());

        // create bubble entity
        MinecraftBubbleEntity bubbleEntity = createBubbleEntity(bubble, formattedMessage, fViewer);
        bubbleEntities.push(bubbleEntity);

        if (bubble.isInteractionRiding()) {
            bubbleEntities.push(createSpaceBubbleEntity(bubble, fViewer));
        } else {
            for (int i = 0; i < bubble.getElevation(); i++) {
                bubbleEntities.push(createSpaceBubbleEntity(bubble, fViewer));
            }
        }

        activeBubbleEntities.put(key, bubbleEntities);

        rideEntities(sender, fViewer);
    }

    @Override
    public void removeBubbleIf(Predicate<Bubble> bubbleEntityPredicate) {
        activeBubbleEntities.forEach((_, bubbleEntities) -> {
            if (bubbleEntities.isEmpty()) return;

            List<MinecraftBubbleEntity> bubbleEntitiesToRemove = bubbleEntities.stream()
                    .filter(bubbleEntity -> bubbleEntityPredicate.test(bubbleEntity.getBubble()))
                    .toList();

            if (bubbleEntitiesToRemove.isEmpty()) return;

            // despawn entities
            bubbleEntitiesToRemove.forEach(this::despawnBubbleEntity);

            // remove from active bubbles
            bubbleEntities.removeAll(bubbleEntitiesToRemove);

            // remove space
            MinecraftBubbleEntity bubbleEntity = bubbleEntitiesToRemove.getFirst();

            rideEntities(bubbleEntity.getBubble().getSender(), bubbleEntity.getViewer());
        });
    }

    public void rideEntities(FPlayer sender, FPlayer viewer) {
        Deque<MinecraftBubbleEntity> bubbleEntities = activeBubbleEntities.get(sender.uuid().toString() + viewer.uuid());
        if (bubbleEntities == null) return;
        if (bubbleEntities.isEmpty()) return;
        if (!isCorrectPlayer(sender)) return;
        if (!integrationModule.canSeeVanished(sender, viewer)) return;

        boolean hasSeenVisible = false;
        boolean hasSpawnedSpace = false;

        int playerId = platformPlayerAdapter.getEntityId(sender.uuid());
        int lastID = playerId;

        for (MinecraftBubbleEntity bubbleEntity : bubbleEntities) {
            boolean isFirstBubble = bubbleEntities.getFirst().equals(bubbleEntity);

            if (bubbleEntity.getEntityType() == EntityTypes.INTERACTION && !isFirstBubble) {
                List<EntityData<?>> metadataList = createEntityData(bubbleEntity, false);

                packetSender.send(bubbleEntity.getViewer(), new WrapperPlayServerEntityMetadata(bubbleEntity.getId(), metadataList));
            }

            if (bubbleEntity.isVisible()) {
                hasSpawnedSpace = false;
                hasSeenVisible = true;
            } else if (hasSeenVisible && hasSpawnedSpace) {
                continue;
            }

            spawnEntity(bubbleEntity, isFirstBubble);

            int[] passengers = new int[]{bubbleEntity.getId()};

            List<Integer> textScreenPassengers = textScreenRender.getPassengers(viewer.uuid());
            if (!textScreenPassengers.isEmpty() && playerId == lastID) {
                passengers = ArrayUtils.add(textScreenPassengers.stream().mapToInt(Integer::intValue).toArray(), bubbleEntity.getId());
            }

            lastID = rideEntity(bubbleEntity, lastID, passengers);

            if (!bubbleEntity.isVisible() && hasSeenVisible) {
                hasSpawnedSpace = true;
            }
        }
    }

    private int rideEntity(MinecraftBubbleEntity nextBubbleEntity, int entityId, int[] passengersIds) {
        packetSender.send(nextBubbleEntity.getViewer(), new WrapperPlayServerSetPassengers(entityId, passengersIds), true);

        return nextBubbleEntity.getId();
    }

    private Component createFormattedMessage(Bubble bubble, FPlayer viewer) {
        Localization.Message.Bubble localization = fileFacade.localization(viewer).message().bubble();

        MessageContext messageContext = messagePipeline.createContext(bubble.getSender(), viewer, bubble.getRawMessage())
                .addFlags(
                        new MessageFlag[]{MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.PLAYER_MESSAGE},
                        new boolean[]{false, false, false, true}
                );

        Component message = messagePipeline.build(messageContext);

        return messagePipeline.build(messageContext
                .withMessage(localization.format())
                .addFlag(MessageFlag.PLAYER_MESSAGE, false)
                .addTagResolver(TagResolver.resolver("message", (_, _) -> Tag.inserting(message)))
        );
    }

    private MinecraftBubbleEntity createBubbleEntity(Bubble bubble, Component formattedMessage, FPlayer viewer) {
        int id = platformServerAdapter.generateEntityId();

        EntityType entityType = bubble instanceof ModernBubble
                ? EntityTypes.TEXT_DISPLAY
                : EntityTypes.AREA_EFFECT_CLOUD;

        return new MinecraftBubbleEntity(id, entityType, bubble, viewer, formattedMessage);
    }

    private MinecraftBubbleEntity createSpaceBubbleEntity(Bubble bubble, FPlayer viewer) {
        int spaceEntityId = platformServerAdapter.generateEntityId();

        EntityType spaceBubbleEntityType = bubble.isInteractionRiding()
                ? EntityTypes.INTERACTION
                : EntityTypes.AREA_EFFECT_CLOUD;

        return new MinecraftBubbleEntity(spaceEntityId, spaceBubbleEntityType, bubble, viewer, Component.empty(), false);
    }

    private void despawnBubbleEntity(MinecraftBubbleEntity bubbleEntity) {
        int despawnDelay = 0;
        if (bubbleEntity.getBubble() instanceof ModernBubble bubble) {
            if (bubbleEntity.getEntityType() == EntityTypes.TEXT_DISPLAY) {
                interpolate(bubbleEntity, bubble, Vector3f.zero());
            }

            despawnDelay = bubble.getAnimationTime();
        }

        taskScheduler.runAsyncLater(() -> packetSender.send(bubbleEntity.getViewer(), new WrapperPlayServerDestroyEntities(bubbleEntity.getId())), despawnDelay);
    }

    @Override
    public void removeAllBubbles() {
        activeBubbleEntities.values().forEach(entities ->
                entities.forEach(this::despawnBubbleEntity)
        );
        activeBubbleEntities.clear();
    }

    private void spawnEntity(MinecraftBubbleEntity bubbleEntity, boolean isFirstBubble) {
        if (bubbleEntity.isCreated()) return;

        PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(bubbleEntity.getBubble().getSender());
        if (coordinates == null) return;

        Location location = new Location(coordinates.x(), coordinates.y() + 1.8, coordinates.z(), coordinates.yaw(), coordinates.pitch());

        int id = bubbleEntity.getId();
        EntityType entityType = bubbleEntity.getEntityType();

        packetSender.send(bubbleEntity.getViewer(), new WrapperPlayServerSpawnEntity(
                id, UUID.randomUUID(), entityType, location, 0, 0, null
        ));

        List<EntityData<?>> metadataList = createEntityData(bubbleEntity, isFirstBubble);

        packetSender.send(bubbleEntity.getViewer(), new WrapperPlayServerEntityMetadata(id, metadataList));

        bubbleEntity.setCreated(true);
        bubbleEntity.getBubble().setCreated(true);

        taskScheduler.runAsyncLater(() -> {
            if (entityType == EntityTypes.TEXT_DISPLAY && bubbleEntity.getBubble() instanceof ModernBubble bubble) {
                interpolate(bubbleEntity, bubble, new Vector3f(bubble.getScale(), bubble.getScale(), bubble.getScale()));
            }
        }, 1);
    }

    private void interpolate(MinecraftBubbleEntity bubbleEntity, ModernBubble bubble, Vector3f scale) {
        List<EntityData<?>> metadataList = new ObjectArrayList<>();

        // interpolation delay
        metadataList.add(new EntityData<>(8, EntityDataTypes.INT, -1));

        // transformation duration
        metadataList.add(new EntityData<>(9, EntityDataTypes.INT, bubble.getAnimationTime()));

        // scale
        metadataList.add(new EntityData<>(entityUtil.displayOffset() + 3, EntityDataTypes.VECTOR3F, scale));

        packetSender.send(bubbleEntity.getViewer(), new WrapperPlayServerEntityMetadata(bubbleEntity.getId(), metadataList));
    }

    private List<EntityData<?>> createEntityData(MinecraftBubbleEntity bubbleEntity, boolean isFirstBubble) {
        List<EntityData<?>> metadataList = new ObjectArrayList<>();

        EntityType entityType = bubbleEntity.getEntityType();

        Component message = bubbleEntity.getMessage();

        if (entityType == EntityTypes.TEXT_DISPLAY && bubbleEntity.getBubble() instanceof ModernBubble bubble) {

            // scale
            metadataList.add(new EntityData<>(entityUtil.displayOffset() + 3, EntityDataTypes.VECTOR3F, Vector3f.zero()));

            // center for viewer
            metadataList.add(new EntityData<>(entityUtil.displayOffset() + 6, EntityDataTypes.BYTE, (byte) bubble.getBillboard().ordinal()));

            // text
            metadataList.add(new EntityData<>(entityUtil.textDisplayOffset(), EntityDataTypes.ADV_COMPONENT, message));

            // width
            metadataList.add(new EntityData<>(entityUtil.textDisplayOffset() + 1, EntityDataTypes.INT, 100000));

            // background color
            int backgroundColor = bubble.getBackground();
            metadataList.add(new EntityData<>(entityUtil.textDisplayOffset() + 2, EntityDataTypes.INT, backgroundColor));

            byte flags = 0x00;

            if (bubble.isHasShadow()) {
                flags |= 0x01;
            }

            if (bubble.isSeeThrough()) {
                flags |= 0x02;
            }

            if (flags != 0x00) {
                metadataList.add(new EntityData<>(entityUtil.textDisplayOffset() + 4, EntityDataTypes.BYTE, flags));
            }

            return metadataList;
        }

        if (entityType == EntityTypes.AREA_EFFECT_CLOUD) {
            metadataList.add(new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(message)));
            metadataList.add(new EntityData<>(3, EntityDataTypes.BOOLEAN, bubbleEntity.isVisible()));
            metadataList.add(new EntityData<>(entityUtil.areaEffectCloudRadiusIndex(), EntityDataTypes.FLOAT, 0f));

            return metadataList;
        }

        if (entityType == EntityTypes.INTERACTION) {
            metadataList.add(new EntityData<>(8, EntityDataTypes.FLOAT, (float) 0.000001));

            Bubble bubble = bubbleEntity.getBubble();
            float height = isFirstBubble ? bubble.getElevation() : bubble.getInteractionHeight();
            metadataList.add(new EntityData<>(9, EntityDataTypes.FLOAT, height));

            return metadataList;
        }

        return metadataList;
    }

    @Override
    public boolean isCorrectPlayer(FPlayer sender) {
        List<Integer> passengers = platformPlayerAdapter.getPassengers(sender.uuid());

        return !platformPlayerAdapter.getGamemode(sender).equals(GameMode.SPECTATOR.name())
                && !platformPlayerAdapter.hasPotionEffect(sender, PotionUtil.INVISIBILITY_POTION_NAME)
                && textScreenRender.getPassengers(sender.uuid()).isEmpty()
                && passengers.isEmpty();
    }

    @Override
    public boolean isModern() {
        return packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4)
                && fileFacade.message().bubble().modern().enable();
    }

    @Override
    public boolean isInteractionRiding() {
        return packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_3)
                && fileFacade.message().bubble().interaction().enable();
    }

}