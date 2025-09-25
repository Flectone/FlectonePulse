package net.flectone.pulse.model.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.model.entity.FPlayer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class Cooldown {

    private final HashMap<UUID, Long> playerDuration = new HashMap<>();

    private final boolean enable;
    private final long duration;

    @Setter private String permissionBypass = "";

    public Cooldown(boolean enable, long duration) {
        this.enable = enable;
        this.duration = duration;
    }

    public Cooldown() {
        this(false, 60L);
    }

    public boolean isCooldown(UUID uuid) {
        if (!isEnable()) return false;

        long currentTime = System.currentTimeMillis();

        Long time = playerDuration.get(uuid);

        if (time == null || currentTime >= time) {
            playerDuration.put(uuid, currentTime + duration * TimeFormatter.MULTIPLIER);
            return false;
        }

        return true;
    }

    public long getTimeLeft(FPlayer fPlayer) {
        return playerDuration.getOrDefault(fPlayer.getUuid(), 0L) - System.currentTimeMillis();
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
