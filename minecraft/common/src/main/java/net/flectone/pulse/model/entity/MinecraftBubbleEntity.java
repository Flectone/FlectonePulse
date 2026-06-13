package net.flectone.pulse.model.entity;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.module.message.bubble.model.Bubble;
import net.kyori.adventure.text.Component;

@Getter
public class MinecraftBubbleEntity extends MinecraftPacketEntity {

    private final Bubble bubble;
    private final FPlayer viewer;

    // Mutable so a late-arriving translation can replace the rendered text in place
    // (metadata packet, no respawn) when the chunk count is unchanged.
    @Setter
    private Component message;

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