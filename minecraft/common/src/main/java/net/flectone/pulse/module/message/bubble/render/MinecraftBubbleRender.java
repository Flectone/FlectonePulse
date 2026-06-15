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
import net.flectone.pulse.execution.scheduler.SchedulerRunnable;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.entity.MinecraftBubbleEntity;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.bubble.model.Bubble;
import net.flectone.pulse.module.message.bubble.model.ModernBubble;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.render.TextScreenRender;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.service.TranslationCacheService;
import net.flectone.pulse.util.MinecraftEntityUtil;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.PotionUtil;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
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
    private final SocialService socialService;
    private final PlatformServerAdapter platformServerAdapter;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MinecraftPacketSender packetSender;
    private final MessagePipeline messagePipeline;
    private final TaskScheduler taskScheduler;
    private final MinecraftEntityUtil entityUtil;
    private final MinecraftPacketProvider packetProvider;
    private final TextScreenRender textScreenRender;
    private final ReflectionResolver reflectionResolver;
    private final TranslationCacheService translationCacheService;

    @Override
    public void renderBubble(Bubble bubble) {
        FPlayer sender = bubble.getSender();
        if (!isCorrectPlayer(sender)) return;

        Message.Bubble config = fileFacade.message().bubble();
        double viewDistance = config.distance();

        CompletableFuture<Set<UUID>> nearbyEntitiesFuture = new CompletableFuture<>();

        SchedulerRunnable runnable = () -> {
            Set<UUID> nearbyEntities = platformPlayerAdapter.findPlayersWhoCanSee(sender, viewDistance, viewDistance, viewDistance);
            nearbyEntitiesFuture.complete(nearbyEntities);
        };

        if (reflectionResolver.isFolia()) {
            taskScheduler.runRegion(sender, runnable);
        } else {
            taskScheduler.runSync(runnable);
        }

        nearbyEntitiesFuture.thenAcceptAsync(nearbyEntities -> nearbyEntities
                .stream()
                .map(fPlayerService::getFPlayer)
                .filter(fViewer -> config.visibleToSelf() || !fViewer.equals(sender))
                .filter(fViewer -> !bubble.getViewers().isEmpty() && bubble.getViewers().contains(fViewer))
                .filter(fViewer -> !fViewer.isUnknown())
                .filter(fViewer -> !socialService.isIgnored(fViewer, sender))
                .filter(fViewer -> socialService.canSeeVanished(sender, fViewer))
                .forEach(fViewer -> renderBubble(fViewer, bubble))
        );
    }

    public void renderBubble(FPlayer fViewer, Bubble bubble) {
        // Chunks of one message share an id; render the whole stack on the first chunk
        // and ignore the rest (they only exist to drive the queue/maxCount accounting).
        if (bubble.getChunkIndex() != 0) return;

        // Per-viewer language resolution: same locale → original chunks; otherwise the
        // translated-and-re-split chunks if cached, else original now + async update.
        List<String> chunkTexts = resolveChunkTexts(bubble, fViewer);

        List<MinecraftBubbleEntity> textEntities = renderChunkStack(fViewer, bubble, chunkTexts);

        maybeScheduleTranslationUpdate(fViewer, bubble, chunkTexts, textEntities);
    }

    // Renders the vertical stack (one text entity per chunk, chunkIndex 0 on top) and
    // returns the created text entities in chunk order (index i == chunkIndex i).
    private List<MinecraftBubbleEntity> renderChunkStack(FPlayer fViewer, Bubble bubble, List<String> chunkTexts) {
        FPlayer sender = bubble.getSender();
        String key = sender.uuid().toString() + fViewer.uuid();
        Deque<MinecraftBubbleEntity> bubbleEntities = activeBubbleEntities.getOrDefault(key, new ConcurrentLinkedDeque<>());

        List<MinecraftBubbleEntity> textEntities = new ObjectArrayList<>();

        // Iterate chunks in forward order, mirroring how the original code pushed each
        // chunk one-by-one in queue order: every push prepends to the deque, so the
        // first chunk ends up at the back (top of the column) and the last chunk at the
        // front (bottom). This keeps reading order top-to-bottom (chunkIndex 0 on top).
        for (int i = 0; i < chunkTexts.size(); i++) {
            Component formattedMessage = createFormattedMessage(bubble, fViewer, chunkTexts.get(i));

            MinecraftBubbleEntity bubbleEntity = createBubbleEntity(bubble, formattedMessage, fViewer);
            bubbleEntities.push(bubbleEntity);
            textEntities.add(bubbleEntity);

            if (bubble.isInteractionRiding()) {
                bubbleEntities.push(createSpaceBubbleEntity(bubble, fViewer));
            } else {
                for (int j = 0; j < bubble.getElevation(); j++) {
                    bubbleEntities.push(createSpaceBubbleEntity(bubble, fViewer));
                }
            }
        }

        activeBubbleEntities.put(key, bubbleEntities);

        rideEntities(sender, fViewer);

        // textEntities is already in chunk order (index i == chunkIndex i), matching
        // the order applyTranslationUpdate expects for in-place text swaps.
        return textEntities;
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
        if (!socialService.canSeeVanished(sender, viewer)) return;

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

    // Returns the chunk texts to render for this viewer: original chunks when translation
    // is disabled / same locale / not yet cached, otherwise the re-split translation.
    private List<String> resolveChunkTexts(Bubble bubble, FPlayer fViewer) {
        List<String> original = splitFull(bubble, bubble.getFullMessage());

        if (!isBubbleTranslateEnabled()) return original;

        String senderLocale = bubble.getSenderLocale();
        String viewerLocale = socialService.getSetting(fViewer, SettingText.LOCALE);
        if (senderLocale == null || viewerLocale == null || viewerLocale.equals(senderLocale)) {
            return original;
        }

        String cached = translationCacheService.get(senderLocale, viewerLocale, bubble.getFullMessage());
        if (cached != null && !cached.isEmpty() && !cached.equals(bubble.getFullMessage())) {
            return splitFull(bubble, cached);
        }

        // Not cached yet — show original now, async update will replace it.
        return original;
    }

    // Re-splits a (translated or original) full message into chunks using the exact same
    // symbol rules the queue used.
    private List<String> splitFull(Bubble bubble, String fullMessage) {
        if (fullMessage == null) return List.of(bubble.getRawMessage());

        Message.Bubble config = fileFacade.message().bubble();
        return BubbleService.splitText(
                fullMessage,
                config.maxLength(),
                config.maxCount(),
                config.hintBufferLength(),
                config.wordBreakHint()
        );
    }

    // If a translation isn't cached yet, kick it off and, on completion, update this
    // viewer's already-rendered bubble: in-place text swap when the chunk count matches,
    // full re-render (despawn + respawn the stack) when it differs.
    private void maybeScheduleTranslationUpdate(FPlayer fViewer, Bubble bubble,
                                                List<String> renderedChunks,
                                                List<MinecraftBubbleEntity> textEntities) {
        if (!isBubbleTranslateEnabled()) return;

        String senderLocale = bubble.getSenderLocale();
        String viewerLocale = socialService.getSetting(fViewer, SettingText.LOCALE);
        if (senderLocale == null || viewerLocale == null || viewerLocale.equals(senderLocale)) return;

        String cached = translationCacheService.get(senderLocale, viewerLocale, bubble.getFullMessage());
        if (cached != null && !cached.isEmpty() && !cached.equals(bubble.getFullMessage())) {
            // Already rendered with translation in resolveChunkTexts — nothing to do.
            return;
        }

        List<String> providers = fileFacade.message().format().translate().providers();
        if (providers == null || providers.isEmpty()) return;

        translationCacheService.translateAsync(senderLocale, viewerLocale, bubble.getFullMessage(), providers)
                .thenAccept(translated -> {
                    if (translated == null || translated.isEmpty() || translated.equals(bubble.getFullMessage())) {
                        return;
                    }
                    applyTranslationUpdate(fViewer, bubble, renderedChunks, textEntities, translated);
                });
    }

    private void applyTranslationUpdate(FPlayer fViewer, Bubble bubble,
                                        List<String> renderedChunks,
                                        List<MinecraftBubbleEntity> textEntities,
                                        String translated) {
        String key = bubble.getSender().uuid().toString() + fViewer.uuid();
        Deque<MinecraftBubbleEntity> bubbleEntities = activeBubbleEntities.get(key);
        // Guard against TTL despawn / cleared state arriving before this async callback.
        if (bubbleEntities == null || bubbleEntities.isEmpty()) return;
        if (textEntities.isEmpty()) return;
        if (!bubbleEntities.contains(textEntities.get(0))) return;
        if (!fViewer.isOnline()) return;

        List<String> newChunks = splitFull(bubble, translated);

        if (newChunks.size() == renderedChunks.size()) {
            // Same shape — swap text on each existing entity with a metadata packet.
            for (int i = 0; i < textEntities.size(); i++) {
                MinecraftBubbleEntity entity = textEntities.get(i);
                if (!entity.isCreated()) continue;
                Component formatted = createFormattedMessage(bubble, fViewer, newChunks.get(i));
                entity.setMessage(formatted);
                updateEntityText(entity, formatted);
            }
            return;
        }

        // Chunk count changed — despawn this message's stack for the viewer and re-render
        // with the translated chunks (reflows the vertical stack).
        removeBubbleForViewer(bubble, fViewer);
        // Translation is now cached, so the re-render resolves it synchronously; no
        // further async update is scheduled.
        renderChunkStack(fViewer, bubble, newChunks);
    }

    // Pushes a single metadata packet that replaces just the text field of an existing
    // bubble entity (TEXT_DISPLAY: ADV_COMPONENT; AREA_EFFECT_CLOUD: OPTIONAL_ADV_COMPONENT).
    private void updateEntityText(MinecraftBubbleEntity entity, Component message) {
        EntityType entityType = entity.getEntityType();
        List<EntityData<?>> metadataList = new ObjectArrayList<>();

        if (entityType == EntityTypes.TEXT_DISPLAY) {
            metadataList.add(new EntityData<>(entityUtil.textDisplayOffset(), EntityDataTypes.ADV_COMPONENT, message));
        } else if (entityType == EntityTypes.AREA_EFFECT_CLOUD) {
            metadataList.add(new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(message)));
        } else {
            return;
        }

        packetSender.send(entity.getViewer(), new WrapperPlayServerEntityMetadata(entity.getId(), metadataList));
    }

    // Removes only this message's entities for a single viewer (one sender+viewer stack).
    private void removeBubbleForViewer(Bubble bubble, FPlayer fViewer) {
        String key = bubble.getSender().uuid().toString() + fViewer.uuid();
        Deque<MinecraftBubbleEntity> bubbleEntities = activeBubbleEntities.get(key);
        if (bubbleEntities == null || bubbleEntities.isEmpty()) return;

        List<MinecraftBubbleEntity> toRemove = bubbleEntities.stream()
                .filter(entity -> entity.getBubble().equals(bubble))
                .toList();
        if (toRemove.isEmpty()) return;

        toRemove.forEach(this::despawnBubbleEntity);
        bubbleEntities.removeAll(toRemove);

        rideEntities(bubble.getSender(), fViewer);
    }

    private Component createFormattedMessage(Bubble bubble, FPlayer viewer, String chunkText) {
        Localization.Message.Bubble localization = fileFacade.localization(socialService.getSetting(viewer, SettingText.LOCALE)).message().bubble();

        MessageContext messageContext = MessageContext.builder()
                .sender(bubble.getSender())
                .receiver(viewer)
                .message(chunkText)
                .flags(
                        new MessageFlag[]{MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.PLAYER_MESSAGE},
                        new boolean[]{false, false, false, true}
                )
                .build();

        return messagePipeline.build(messageContext.toBuilder()
                .message(localization.format())
                .flag(MessageFlag.PLAYER_MESSAGE, false)
                .tagResolver(messagePipeline.resolver("message", (_, _) -> Tag.inserting(messagePipeline.build(messageContext))))
                .build()
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

    // Bubble auto-translate runs only when the translate module is enabled AND the
    // bubble auto-translate toggle (message.format.translate.bubble) is on (default true).
    private boolean isBubbleTranslateEnabled() {
        Message.Format.Translate translate = fileFacade.message().format().translate();
        boolean enabled = Boolean.TRUE.equals(translate.enable());
        boolean bubbleToggle = !Boolean.FALSE.equals(translate.bubble());
        return enabled && bubbleToggle;
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