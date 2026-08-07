package net.flectone.pulse.model.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A repeating schedule for modules that fire on their own, such as auto messages.
 *
 * @param enable whether the schedule runs
 * @param period the delay between runs, in ticks
 * @author TheFaser
 */
public record Ticker(boolean enable, long period) {

    /**
     * A disabled schedule, which every disabled entry collapses to.
     */
    public static final Ticker DEFAULT = new Ticker(false, 100L);

    /**
     * Reads a ticker from its config representation.
     * A disabled ticker collapses to {@link #DEFAULT}, so the period is only read when enabled.
     *
     * @param map the raw config values
     * @return the parsed ticker
     */
    @JsonCreator
    public static Ticker fromJson(Map<String, Object> map) {
        boolean isEnable = Boolean.parseBoolean(String.valueOf(map.get("enable")));
        if (!isEnable) return DEFAULT;

        Object period = map.get("period");
        long longPeriod = period == null ? 100L : Long.parseLong(String.valueOf(period));

        return new Ticker(true, longPeriod);
    }

    /**
     * Writes this ticker back to its config representation, omitting the period while disabled.
     *
     * @return the config values
     */
    @JsonValue
    public Map<String, Object> toJson() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enable", this.enable);

        if (this.enable) {
            map.put("period", this.period);
        }

        return map;
    }
}