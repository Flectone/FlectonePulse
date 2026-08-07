package net.flectone.pulse.util.constant;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.PlayTime;

import java.util.Arrays;
import java.util.Optional;

/**
 * The play-time figures a placeholder can ask for. Each constant knows how to work its own
 * value out from a stored {@link PlayTime} row.
 * @author TheFaser
 */
public enum TimeType {

    FIRST {
        @Override
        public long getTime(FPlayer fPlayer, PlayTime playTime) {
            return System.currentTimeMillis() - playTime.first();
        }
    },
    LAST {
        @Override
        public long getTime(FPlayer fPlayer, PlayTime playTime) {
            return System.currentTimeMillis() - (playTime.last() > 0 ? playTime.last() : playTime.last() * -1);
        }
    },
    TOTAL {
        @Override
        public long getTime(FPlayer fPlayer, PlayTime playTime) {
            return playTime.total();
        }
    },
    TOTAL_DYNAMIC {
        @Override
        public long getTime(FPlayer fPlayer, PlayTime playTime) {
            return playTime.total() + (fPlayer.isOnline() && playTime.last() > 0 ? System.currentTimeMillis() - playTime.last() : 0);
        }
    };

    /**
     * Works out this figure for a player.
     *
     * @param fPlayer the player, consulted for whether they are online right now
     * @param playTime the stored session statistics
     * @return the time in milliseconds
     */
    public abstract long getTime(FPlayer fPlayer, PlayTime playTime);

    /**
     * Maps a name back to its constant, ignoring case.
     *
     * @param string the name to parse
     * @return the matching figure, or empty if the name is unknown
     */
    public static Optional<TimeType> fromString(String string) {
        return Arrays.stream(TimeType.values())
                .filter(type -> type.name().equalsIgnoreCase(string))
                .findAny();
    }

}
