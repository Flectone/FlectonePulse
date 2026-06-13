package net.flectone.pulse.util.constant;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.PlayTime;

import java.util.Arrays;
import java.util.Optional;

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

    public abstract long getTime(FPlayer fPlayer, PlayTime playTime);

    public static Optional<TimeType> fromString(String string) {
        return Arrays.stream(TimeType.values())
                .filter(type -> type.name().equalsIgnoreCase(string))
                .findAny();
    }

}
