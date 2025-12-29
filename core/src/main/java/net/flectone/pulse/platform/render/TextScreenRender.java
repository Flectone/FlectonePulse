package net.flectone.pulse.platform.render;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.TextScreen;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.bubble.renderer.BubbleRenderer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.converter.ColorConverter;
import net.flectone.pulse.util.EntityUtil;
import net.flectone.pulse.util.RandomUtil;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TextScreenRender {

    private final Map<UUID, List<Integer>> livingEntities = new ConcurrentHashMap<>();

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PacketSender packetSender;
    private final ColorConverter colorConverter;
    private final TaskScheduler taskScheduler;
    private final RandomUtil randomUtil;
    private final EntityUtil entityUtil;
    private final Provider<BubbleRenderer> bubbleRenderer;

    public void clear() {
        livingEntities.forEach((uuid, entities) -> entities.forEach(integer -> destroy(uuid, integer)));
        livingEntities.clear();
    }

    public void render(FPlayer fPlayer, Component message, TextScreen textScreen) {
        Optional<Integer> optionalId = spawn(fPlayer, message, textScreen);
        if (optionalId.isEmpty()) return;

        int entityId = optionalId.get();
        int playerId = platformPlayerAdapter.getEntityId(fPlayer.getUuid());

        addAndRide(fPlayer.getUuid(), playerId, entityId);
        bubbleRenderer.get().rideEntities(fPlayer, fPlayer);

        if (textScreen.hasAnimation()) {
            animationSpawn(fPlayer, textScreen, entityId);
        }

        animationDespawnAndDestroy(fPlayer, textScreen, entityId);
    }

    public List<Integer> getPassengers(UUID uuid) {
        return livingEntities.getOrDefault(uuid, new CopyOnWriteArrayList<>());
    }

    public void ride(UUID uuid, int playerId, List<Integer> textScreenPassengers, boolean silent) {
        List<Integer> playerPassengers = platformPlayerAdapter.getPassengers(uuid);
        int[] finalPassengers = Stream.of(textScreenPassengers, playerPassengers)
                .flatMap(Collection::stream)
                .mapToInt(Integer::intValue)
                .toArray();

        packetSender.send(uuid, new WrapperPlayServerSetPassengers(playerId, finalPassengers), silent);
    }

    public void updateAndRide(int playerId) {
        taskScheduler.runAsync(() -> {
            UUID uuid = platformPlayerAdapter.getPlayerByEntityId(playerId);
            if (uuid == null) return;

            List<Integer> textScreenPassengers = getPassengers(uuid);
            if (textScreenPassengers.isEmpty()) return;

            ride(uuid, playerId, textScreenPassengers, true);
        });
    }

    private Optional<Integer> spawn(FPlayer fPlayer, Component message, TextScreen textScreen) {
        if (!textScreen.hasLiveTime()) return Optional.empty();

        Location location = platformPlayerAdapter.getLocation(fPlayer);
        if (location == null) return Optional.empty();

        location.setPosition(location.getPosition().add(0, 1.8, 0));

        int entityId = randomUtil.nextInt(Integer.MAX_VALUE);
        EntityType entityType = EntityTypes.TEXT_DISPLAY;

        packetSender.send(fPlayer, new WrapperPlayServerSpawnEntity(
                entityId, UUID.randomUUID(), entityType, location, 0, 0, null
        ));

        List<EntityData<?>> metadataList = new ArrayList<>();

        Vector3f translation = new Vector3f(textScreen.offsetX(), textScreen.offsetY(), textScreen.offsetZ());
        metadataList.add(new EntityData<>(entityUtil.displayOffset() + 2, EntityDataTypes.VECTOR3F, translation));

        // scale
        Vector3f scale = textScreen.hasAnimation()
                ? new Vector3f()
                : new Vector3f(textScreen.scale(), textScreen.scale(), textScreen.scale());
        metadataList.add(new EntityData<>(entityUtil.displayOffset() + 3, EntityDataTypes.VECTOR3F, scale));

        // center for viewer
        metadataList.add(new EntityData<>(entityUtil.displayOffset() + 6, EntityDataTypes.BYTE, (byte) BubbleModule.Billboard.CENTER.ordinal()));

        // text
        metadataList.add(new EntityData<>(entityUtil.textDisplayOffset(), EntityDataTypes.ADV_COMPONENT, message));

        // width
        metadataList.add(new EntityData<>(entityUtil.textDisplayOffset() + 1, EntityDataTypes.INT, textScreen.width()));

        // background color
        metadataList.add(new EntityData<>(entityUtil.textDisplayOffset() + 2, EntityDataTypes.INT, colorConverter.parseHexToArgb(textScreen.background())));

        byte flags = 0x00;

        if (textScreen.hasShadow()) {
            flags |= 0x01;
        }

        if (textScreen.seeThrough()) {
            flags |= 0x02;
        }

        if (flags != 0x00) {
            metadataList.add(new EntityData<>(entityUtil.textDisplayOffset() + 4, EntityDataTypes.BYTE, flags));
        }

        packetSender.send(fPlayer, new WrapperPlayServerEntityMetadata(entityId, metadataList));
        return Optional.of(entityId);
    }

    private void addAndRide(UUID uuid, int playerId, int entityId) {
        List<Integer> textScreenPassengers = getPassengers(uuid);
        textScreenPassengers.add(entityId);

        livingEntities.put(uuid, textScreenPassengers);

        ride(uuid, playerId, textScreenPassengers, false);
    }

    private void animationSpawn(FPlayer fPlayer, TextScreen textScreen, int entityId) {
        taskScheduler.runAsyncLater(() -> interpolate(fPlayer, textScreen, entityId, textScreen.scale()), 1);
    }

    private void animationDespawnAndDestroy(FPlayer fPlayer, TextScreen textScreen, int entityId) {
        taskScheduler.runAsyncLater(() -> {
            if (textScreen.hasAnimation()) {
                interpolate(fPlayer, textScreen, entityId, 0);
                taskScheduler.runAsyncLater(() -> destroy(fPlayer, entityId), textScreen.animationTime() + 1L);
            } else {
                destroy(fPlayer, entityId);
            }
        }, textScreen.liveTime() * 20L);
    }

    private void destroy(FPlayer fPlayer, int entityId) {
        destroy(fPlayer.getUuid(), entityId);
        livingEntities.computeIfPresent(fPlayer.getUuid(), (integer, integers) -> {
            integers.remove((Integer) entityId);
            return integers;
        });
    }

    private void destroy(UUID uuid, int entityId) {
        packetSender.send(uuid, new WrapperPlayServerDestroyEntities(entityId));
    }

    private void interpolate(FPlayer fPlayer, TextScreen textScreen, int entityId, float scale) {
        List<EntityData<?>> metadataList = new ArrayList<>();

        // interpolation delay
        metadataList.add(new EntityData<>(8, EntityDataTypes.INT, -1));

        // transformation duration
        metadataList.add(new EntityData<>(9, EntityDataTypes.INT, textScreen.animationTime()));

        // scale
        metadataList.add(new EntityData<>(entityUtil.displayOffset() + 3, EntityDataTypes.VECTOR3F, new Vector3f(scale, scale, scale)));

        packetSender.send(fPlayer, new WrapperPlayServerEntityMetadata(entityId, metadataList));
    }

}
