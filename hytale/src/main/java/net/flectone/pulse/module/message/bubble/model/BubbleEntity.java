package net.flectone.pulse.module.message.bubble.model;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public record BubbleEntity(
        Ref<EntityStore> entityRef,
        Bubble bubble,
        long expiryTime
) {}