package net.flectone.pulse.model.util;

public record TextScreen(
        String background,
        boolean hasShadow,
        boolean seeThrough,
        int animationTime,
        int liveTime,
        int width,
        float scale,
        float offsetX,
        float offsetY,
        float offsetZ
) {

    public boolean hasAnimation() {
        return animationTime > 0;
    }

    public boolean hasLiveTime() {
        return liveTime > 0;
    }

}