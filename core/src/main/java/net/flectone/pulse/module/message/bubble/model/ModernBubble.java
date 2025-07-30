package net.flectone.pulse.module.message.bubble.model;

import lombok.Getter;
import net.flectone.pulse.module.message.bubble.BubbleModule;

@Getter
public class ModernBubble extends Bubble {

    private final boolean hasShadow;
    private final int background;
    private final float scale;
    private final int animationTime;
    private final BubbleModule.Billboard billboard;

    private ModernBubble(ModernBuilder builder) {
        super(builder);
        this.hasShadow = builder.hasShadow;
        this.background = builder.background;
        this.scale = builder.scale;
        this.animationTime = builder.animationTime;
        this.billboard = builder.billboard;
    }

    public static class ModernBuilder extends Builder {
        private boolean hasShadow;
        private int background;
        private float scale;
        private int animationTime;
        private BubbleModule.Billboard billboard;

        public ModernBuilder hasShadow(boolean hasShadow) {
            this.hasShadow = hasShadow;
            return this;
        }

        public ModernBuilder background(int background) {
            this.background = background;
            return this;
        }

        public ModernBuilder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public ModernBuilder animationTime(int animationTime) {
            this.animationTime = animationTime;
            return this;
        }

        public ModernBuilder billboard(BubbleModule.Billboard billboard) {
            this.billboard = billboard;
            return this;
        }

        @Override
        public ModernBubble build() {
            return new ModernBubble(this);
        }
    }
}
