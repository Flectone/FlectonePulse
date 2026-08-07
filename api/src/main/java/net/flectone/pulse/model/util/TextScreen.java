package net.flectone.pulse.model.util;

/**
 * Floating text drawn in the world in front of the player.
 *
 * @param background the background color in ARGB hex
 * @param hasShadow whether the text is drawn with a shadow
 * @param seeThrough whether the text shows through blocks
 * @param animationTime how long the reveal animation runs, in ticks
 * @param liveTime how long the text stays after appearing, in ticks
 * @param width the wrapping width
 * @param scale the text scale
 * @param offsetX the horizontal offset from the player
 * @param offsetY the vertical offset from the player
 * @param offsetZ the forward offset from the player
 * @author TheFaser
 */
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

    /**
     * Whether the text is revealed with an animation rather than appearing at once.
     *
     * @return true if an animation time is set
     */
    public boolean hasAnimation() {
        return animationTime > 0;
    }

    /**
     * Whether the text disappears on its own instead of staying until replaced.
     *
     * @return true if a live time is set
     */
    public boolean hasLiveTime() {
        return liveTime > 0;
    }

}