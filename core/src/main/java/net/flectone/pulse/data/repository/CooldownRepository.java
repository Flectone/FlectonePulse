package net.flectone.pulse.data.repository;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.util.constant.MessageType;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownRepository {

    private final @Named("cooldown") Cache<CooldownKey, Long> cooldownCache;
    private final ProxyRegistry proxyRegistry;
    private final ProxySender proxySender;
    private final TaskScheduler taskScheduler;

    public record CooldownKey(UUID playerUUID, String cooldownClass) {}

    public long getTimeLeft(UUID playerUUID, Cooldown cooldown, String cooldownOwner) {
        if (cooldown == null || !cooldown.enable()) return 0;

        CooldownKey cooldownKey = new CooldownKey(playerUUID, cooldownOwner);

        Long expireTime = cooldownCache.getIfPresent(cooldownKey);
        if (expireTime == null) return 0;

        return Math.max(0, expireTime - System.currentTimeMillis());
    }

    public boolean updateCache(UUID playerUUID, String cooldownClass, long newExpireTime) {
        CooldownKey cooldownKey = new CooldownKey(playerUUID, cooldownClass);

        Long expireTime = cooldownCache.getIfPresent(cooldownKey);
        if (expireTime == null || expireTime < System.currentTimeMillis()) {
            // do not update time if it is less than the current one
            if (expireTime != null && newExpireTime < expireTime) return true;

            cooldownCache.put(cooldownKey, newExpireTime);
            return true;
        }

        return false;
    }

    public void syncProxy(UUID playerUUID, String cooldownOwner, long newExpireTime) {
        taskScheduler.runAsync(() -> {
            if (proxyRegistry.hasEnabledProxy()) {
                proxySender.send(FPlayer.UNKNOWN, MessageType.SYSTEM_COOLDOWN, dataOutputStream -> {
                    dataOutputStream.writeUTF(playerUUID.toString());
                    dataOutputStream.writeUTF(cooldownOwner);
                    dataOutputStream.writeLong(newExpireTime);
                }, UUID.randomUUID());
            }
        });
    }

}
