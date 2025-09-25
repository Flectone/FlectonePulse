package net.flectone.pulse.model.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class Ticker {

    private final boolean enable;
    private final long period;

    public Ticker() {
        this.enable = false;
        this.period = 100L;
    }

    @JsonValue
    public Map<String, Object> toJson() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enable", this.enable);

        if (this.enable) {
            map.put("period", this.period);
        }

        return map;
    }

    @JsonCreator
    public static Ticker fromJson(Map<String, Object> map) {
        boolean isEnable = Boolean.parseBoolean(String.valueOf(map.get("enable")));
        if (!isEnable) return new Ticker();

        Object period = map.get("period");
        long longPeriod = period == null ? 100L : Long.parseLong(String.valueOf(period));

        return new Ticker(true, longPeriod);
    }
}
