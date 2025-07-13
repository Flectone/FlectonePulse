package net.flectone.pulse.model;

import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.formatter.TimeFormatter;

import java.util.HashMap;
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
}
