package net.flectone.pulse.module.message.bubble.model;

import lombok.Getter;
import net.flectone.pulse.model.FPlayer;
import org.jetbrains.annotations.NotNull;

@Getter
public class ModernBubble extends Bubble {

    private final boolean hasShadow;
    private final int background;
    private final float scale;

    public ModernBubble(int id,
                        @NotNull FPlayer sender,
                        @NotNull String message,
                        long duration,
                        float height,
                        boolean interactionRiding,
                        boolean hasShadow,
                        int background,
                        float scale) {
        super(id, sender, message, duration, height, interactionRiding);

        this.hasShadow = hasShadow;
        this.background = background;
        this.scale = scale;
    }
}
