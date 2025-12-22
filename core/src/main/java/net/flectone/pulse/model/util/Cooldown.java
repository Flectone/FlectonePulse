package net.flectone.pulse.model.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.LinkedHashMap;
import java.util.Map;

public record Cooldown(
        boolean enable,
        long duration
) {

    public Cooldown() {
        this(false, 60L);
    }

    @JsonValue
    public Map<String, Object> toJson() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enable", this.enable);

        if (this.enable) {
            map.put("duration", this.duration);
        }

        return map;
    }

    @JsonCreator
    public static Cooldown fromJson(Map<String, Object> map) {
        boolean isEnable = Boolean.parseBoolean(String.valueOf(map.get("enable")));
        if (!isEnable) return new Cooldown();

        Object duration = map.get("duration");
        long longDuration = duration == null ? 60L : Long.parseLong(String.valueOf(duration));

        return new Cooldown(true, longDuration);
    }
}
