package net.flectone.pulse.module.message.bubble.model.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.flectone.pulse.module.message.bubble.model.Bubble;

public record HytaleBubbleEntity(
        Ref<EntityStore> entityRef,
        Bubble bubble,
        long expiryTime
) {
}