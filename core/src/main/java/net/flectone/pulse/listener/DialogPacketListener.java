package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCustomClickAction;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.platform.controller.DialogController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DialogPacketListener implements PacketListener {

    private final Cache<UUID, AtomicInteger> clickCounts = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    private final DialogController dialogController;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CUSTOM_CLICK_ACTION) {
            User user = event.getUser();

            // close dialog for spam user
            if (isSpam(user.getUUID())) return;

            WrapperPlayClientCustomClickAction wrapper = new WrapperPlayClientCustomClickAction(event);

            String key = wrapper.getId().getKey();

            dialogController.process(user.getUUID(), key, wrapper.getPayload());
        }
    }

    public boolean isSpam(UUID uuid) {
        AtomicInteger count = clickCounts.getIfPresent(uuid);
        if (count == null) {
            count = new AtomicInteger(0);
            clickCounts.put(uuid, count);
        }

        int current = count.incrementAndGet();
        return current > 5;
    }

}
