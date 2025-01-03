package net.flectone.pulse.model;

import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.util.TimeUtil;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class Cooldown {

    private final HashMap<UUID, Long> PLAYER_DURATION = new HashMap<>();

    private final boolean enable;
    private final long duration;

    @Setter
    private String permissionBypass = "";

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

        Long time = PLAYER_DURATION.get(uuid);

        if (time == null || currentTime >= time) {
            PLAYER_DURATION.put(uuid, currentTime + duration * TimeUtil.MULTIPLIER);
            return false;
        }

        return true;
    }

    public long getTimeLeft(FPlayer fPlayer) {
        return PLAYER_DURATION.getOrDefault(fPlayer.getUuid(), 0L) - System.currentTimeMillis();
    }
}
