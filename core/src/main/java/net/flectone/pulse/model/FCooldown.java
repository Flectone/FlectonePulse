package net.flectone.pulse.model;

import lombok.Getter;
import net.flectone.pulse.file.Config;
import net.flectone.pulse.util.PermissionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class FCooldown {

    private static final long MULTIPLIER = 50L;

    private final HashMap<UUID, Long> PLAYER_DURATION = new HashMap<>();

    private final boolean enabled;
    private final long duration;
    private final String permissionBypass;

    public FCooldown(Config.Cooldown cooldown, String permissionBypass) {
        enabled = cooldown.isEnable();
        // 1s = 20ticks -> 20ticks * 50 = 1000ms -> 1s = 1000ms
        duration = cooldown.getDuration() * MULTIPLIER;
        this.permissionBypass = permissionBypass;
    }

    public boolean isCooldowned(UUID uuid) {
        if (!isEnabled()) return false;

        long currentTime = System.currentTimeMillis();

        Long time = PLAYER_DURATION.get(uuid);

        if (time == null || currentTime >= time) {
            PLAYER_DURATION.put(uuid, currentTime + duration);
            return false;
        }

        return true;
    }

    public boolean isCooldowned(@NotNull FPlayer fPlayer, @NotNull PermissionUtil permissionUtil) {
        if (!isEnabled()) return false;
        if (permissionUtil.has(fPlayer, permissionBypass)) return false;

        return isCooldowned(fPlayer.getUuid());
    }

    public long getTimeLeft(@NotNull FPlayer fPlayer) {
        return PLAYER_DURATION.getOrDefault(fPlayer.getUuid(), 0L) - System.currentTimeMillis();
    }
}
