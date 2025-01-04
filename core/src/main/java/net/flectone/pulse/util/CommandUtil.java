package net.flectone.pulse.util;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

public abstract class CommandUtil {

    public abstract void unregister(String command);
    public abstract void dispatch(String command);

    public abstract Optional<Object> getOptional(int index, Object arguments);
    public abstract String getLiteral(int index, Object arguments);
    public abstract String getString(int index, Object arguments);
    public abstract String getText(int index, Object arguments);
    public abstract String getFull(Object arguments);
    public abstract Integer getInteger(int index, Object arguments);
    public abstract Boolean getBoolean(int index, Object arguments);
    public abstract <T> T getByClassOrDefault(int index, Class<T> clazz, T defaultValue, Object arguments);

    @Getter
    public enum TimeType {
        YEAR(31536000, "y"),
        WEEK(604800, "w"),
        DAY(86400, "d"),
        HOUR(3600, "h"),
        MINUTE(60, "m"),
        SECOND(1, "s");

        private final int second;
        private final String format;

        TimeType(int second, String format) {
            this.second = second;
            this.format = format;
        }

        public static TimeType fromString(String string) {
            return Arrays.stream(values())
                    .filter(value -> value.format.equalsIgnoreCase(string))
                    .findAny()
                    .orElse(null);
        }

        public int convertToRealTime(int time) {
            return second * time;
        }
    }

}
