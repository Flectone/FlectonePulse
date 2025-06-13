package net.flectone.pulse.module.message.bubble.model;

import lombok.Getter;
import net.flectone.pulse.model.FPlayer;
import org.jetbrains.annotations.NotNull;

@Getter
public class ModernBubble extends Bubble {

    private final boolean hasShadow;
    private final int background;
    private final float scale;
    private final int animationTime;

    public ModernBubble(int id,
                        @NotNull FPlayer sender,
                        @NotNull String message,
                        long duration,
                        int height,
                        float interactionHeight,
                        boolean interactionRiding,
                        boolean hasShadow,
                        int background,
                        int animationTime,
                        float scale) {
        super(id, sender, message, duration, height, interactionHeight, interactionRiding);

        this.hasShadow = hasShadow;
        this.background = background;
        this.animationTime = animationTime;
        this.scale = scale;
    }
}
