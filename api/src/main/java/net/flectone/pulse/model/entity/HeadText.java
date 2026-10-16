package net.flectone.pulse.model.entity;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.constant.ModuleName;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.Function;

/**
 * A text shown above a player's head
 *
 * @param module the module that created it
 * @param holder the player the text is shown above
 * @param receivers the players who can see it
 * @param component the text for a given player
 * @param modern whether to use the modern display
 * @param movable whether newer texts can push it to another row
 * @param visibleToSelf whether the holder sees their own text
 * @param filterIgnores whether receivers who ignore the holder are skipped
 * @param hasShadow whether the text has a shadow
 * @param seeThrough whether it is visible through blocks
 * @param row the row it is placed in
 * @param maxCount the max number of texts in the same row
 * @param background the background color
 * @param distance the max distance it is visible from
 * @param animationTime how long the animation lasts in milliseconds
 * @param endTime the time it ends in milliseconds, or {@link #PERMANENT}
 * @param scale the text size multiplier
 * @param space the vertical gap between rows
 * @param offset the height above the holder
 * @param billboard how it turns toward the viewer
 */
@With
@Builder
public record HeadText(
        @NonNull ModuleName module,
        @NonNull FPlayer holder,
        @NonNull Set<FPlayer> receivers,
        @NonNull Function<FPlayer, Component> component,
        boolean modern,
        boolean movable,
        boolean visibleToSelf,
        boolean filterIgnores,
        boolean hasShadow,
        boolean seeThrough,
        int row,
        int maxCount,
        int background,
        double distance,
        int animationTime,
        long endTime,
        float scale,
        float space,
        float offset,
        @Nullable Billboard billboard
) {

    /**
     * The end time of a text that never ends
     */
    public static final long PERMANENT = -1;

    /**
     * Checks whether the text never ends
     *
     * @return true if the end time is {@link #PERMANENT}
     */
    public boolean isPermanent() {
        return endTime == PERMANENT;
    }

    /**
     * Checks whether the text has ended
     *
     * @param now the current time in milliseconds
     * @return true if the text is not permanent and its end time has passed
     */
    public boolean isEnd(long now) {
        return !isPermanent() && now > endTime;
    }

    /**
     * Builds the display flags
     *
     * @return the flags with shadow and see-through bits set
     */
    public byte flags() {
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
     * How the text turns toward the viewer
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
         * Gets the protocol id
         *
         * @return the id
         */
        public byte id() {
            return id;
        }
    }

}