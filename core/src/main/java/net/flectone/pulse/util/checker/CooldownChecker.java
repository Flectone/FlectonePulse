package net.flectone.pulse.util.checker;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.platform.formatter.TimeFormatter;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownChecker {

    private final @Named("cooldown") Cache<CooldownKey, Long> cooldownCache;

    public record CooldownKey(UUID playerUUID, Cooldown cooldown) {}

    public boolean check(UUID playerUUID, Cooldown cooldown) {
        if (cooldown == null || !cooldown.enable()) return false;

        long currentTimeMillis = System.currentTimeMillis();

        CooldownKey cooldownKey = new CooldownKey(playerUUID, cooldown);
        Long expireTime = cooldownCache.getIfPresent(cooldownKey);
        if (expireTime == null || expireTime < currentTimeMillis) {
            long newExpireTime = currentTimeMillis + cooldown.duration() * TimeFormatter.MULTIPLIER;
            cooldownCache.put(cooldownKey, newExpireTime);
            return false;
        }

        return true;
    }

    public long getTimeLeft(UUID playerUUID, Cooldown cooldown) {
        if (cooldown == null || !cooldown.enable()) return 0;

        CooldownKey cooldownKey = new CooldownKey(playerUUID, cooldown);

        Long expireTime = cooldownCache.getIfPresent(cooldownKey);
        if (expireTime == null) return 0;

        return Math.max(0, expireTime - System.currentTimeMillis());
    }

}
