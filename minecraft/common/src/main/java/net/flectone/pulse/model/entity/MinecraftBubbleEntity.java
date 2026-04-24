package net.flectone.pulse.model.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import lombok.Getter;
import net.flectone.pulse.module.message.bubble.model.Bubble;
import net.kyori.adventure.text.Component;

@Getter
public class MinecraftBubbleEntity extends MinecraftPacketEntity {

    private final Bubble bubble;
    private final FPlayer viewer;
    private final Component message;
    private final boolean visible;

    public MinecraftBubbleEntity(int id, EntityType entityType, Bubble bubble, FPlayer viewer, Component message, boolean visible) {
        super(id, entityType);

        this.bubble = bubble;
        this.viewer = viewer;
        this.message = message;
        this.visible = visible;
    }

    public MinecraftBubbleEntity(int id, EntityType entityType, Bubble bubble, FPlayer viewer, Component message) {
        this(id, entityType, bubble, viewer, message, true);
    }
}