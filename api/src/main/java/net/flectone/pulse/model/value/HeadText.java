package net.flectone.pulse.model.value;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.platform.formatter.TimeFormatter;

/**
 * Settings for a head text display, controlling visibility, positioning, lifetime and modern rendering.
 *
 * @param visibleToSelf whether the text is visible to the player who owns it
 * @param hideWhenSneaking whether the text is hidden while the player sneaks
 * @param filterIgnores whether the ignore filter between players should be applied to this head text. If true, players who ignore the text owner will not see it
 * @param movable whether the text can be moved
 * @param row the row index for stacking multiple texts
 * @param maxCount the maximum number of texts allowed
 * @param distance the maximum distance at which the text is visible
 * @param lifeTime the lifetime in ticks, or {@link #AUTO} or {@link #PERMANENT}
 * @param modern the modern rendering settings
 * @author TheFaser
 * @since 1.15.0
 */
@With
@Builder(toBuilder = true)
public record HeadText(
        boolean visibleToSelf,
        boolean hideWhenSneaking,
        boolean filterIgnores,
        boolean movable,
        int row,
        int maxCount,
        double distance,
        long lifeTime,
        Modern modern
) {

    /**
     * Modern rendering settings for head text.
     *
     * @param enable whether modern rendering is enabled
     * @param hasShadow whether the text has a shadow
     * @param seeThrough whether the text is visible through blocks
     * @param disableSeeThroughWhenSneaking whether see-through is disabled while sneaking
     * @param background the background color in hex format
     * @param animationTime the animation time in ticks
     * @param offsetX the X offset
     * @param offsetY the Y offset
     * @param offsetZ the Z offset
     * @param scale the scale factor
     * @param scaleWithPlayer whether the scale, offsets and space are multiplied by the holder's scale
     * @param space the spacing between rows
     * @param billboard how the text turns toward the viewer
     */
    @With
    @Builder
    public record Modern(
            boolean enable,
            boolean hasShadow,
            boolean seeThrough,
            boolean disableSeeThroughWhenSneaking,
            String background,
            int animationTime,
            float offsetX,
            float offsetY,
            float offsetZ,
            float scale,
            boolean scaleWithPlayer,
            float space,
            Billboard billboard
    ) {

        /**
         * Builds the display flags using the current see-through setting.
         *
         * @return the flags with shadow and see-through bits set
         */
        public byte flags() {
            return flags(seeThrough);
        }

        /**
         * Builds the display flags with an explicit see-through setting.
         *
         * @param seeThrough whether the see-through bit should be set
         * @return the flags with shadow and see-through bits set
         */
        public byte flags(boolean seeThrough) {
            byte flags = 0x00;

            if (hasShadow) {
                flags |= 0x01;
            }

            if (seeThrough) {
                flags |= 0x02;
            }

            return flags;
        }

        /**
         * How the text turns toward the viewer.
         */
        public enum Billboard {
            FIXED((byte) 0),
            VERTICAL((byte) 1),
            HORIZONTAL((byte) 2),
            CENTER((byte) 3);

            private final byte id;

            Billboard(byte id) {
                this.id = id;
            }

            /**
             * Gets the protocol id.
             *
             * @return the id
             */
            public byte id() {
                return id;
            }
        }
    }

    /**
     * The lifetime value indicating that the text ends automatically.
     */
    public static final long AUTO = 0;

    /**
     * The lifetime value indicating that the text never ends.
     */
    public static final long PERMANENT = -1;

    /**
     * Checks whether the text never ends.
     *
     * @return true if the end time is {@link #PERMANENT}
     */
    public boolean isPermanent() {
        return lifeTime == PERMANENT;
    }

    /**
     * Checks whether the text ends automatically.
     *
     * @return true if the end time is {@link #AUTO}
     */
    public boolean isAuto() {
        return lifeTime == AUTO;
    }

    /**
     * Calculates the end time in milliseconds based on the current time and lifetime.
     *
     * @return the end time in milliseconds, or -1 if the text is permanent
     */
    public long calculateEndTime() {
        return isPermanent() ? -1 : System.currentTimeMillis() + lifeTime * TimeFormatter.MULTIPLIER;
    }

}