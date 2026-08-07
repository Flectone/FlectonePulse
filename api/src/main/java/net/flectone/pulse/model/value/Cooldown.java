package net.flectone.pulse.model.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * How long a player must wait before triggering the same action again.
 *
 * @param enable whether the cooldown is applied at all
 * @param duration the wait between uses, in seconds
 * @author TheFaser
 */
public record Cooldown(
        boolean enable,
        long duration
) {

    /**
     * A disabled cooldown, which every disabled entry collapses to.
     */
    public static final Cooldown DEFAULT = new Cooldown(false, 60L);

    /**
     * Reads a cooldown from its config representation.
     * A disabled cooldown collapses to {@link #DEFAULT}, so the duration is only read when enabled.
     *
     * @param map the raw config values
     * @return the parsed cooldown
     */
    @JsonCreator
    public static Cooldown fromJson(Map<String, Object> map) {
        boolean isEnable = Boolean.parseBoolean(String.valueOf(map.get("enable")));
        if (!isEnable) return DEFAULT;

        Object duration = map.get("duration");
        long longDuration = duration == null ? 60L : Long.parseLong(String.valueOf(duration));

        return new Cooldown(true, longDuration);
    }

    /**
     * Writes this cooldown back to its config representation.
     * The duration is omitted while disabled to keep the file short.
     *
     * @return the config values
     */
    @JsonValue
    public Map<String, Object> toJson() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enable", this.enable);

        if (this.enable) {
            map.put("duration", this.duration);
        }

        return map;
    }
}
