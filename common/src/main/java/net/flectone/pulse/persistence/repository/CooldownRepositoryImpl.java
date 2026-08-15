package net.flectone.pulse.persistence.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Cooldown;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.scheduler.TaskScheduler;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownRepositoryImpl implements CooldownRepository {

    private final @Named("cooldown") Cache<CooldownKey, Long> cooldownCache;
    private final ProxyRegistry proxyRegistry;
    private final ProxySender proxySender;
    private final TaskScheduler taskScheduler;

    @Override
    public long getTimeLeft(UUID playerUUID, Cooldown cooldown, String cooldownOwner) {
        if (cooldown == null || !cooldown.enable()) return 0;

        CooldownKey cooldownKey = new CooldownKey(playerUUID, cooldownOwner);

        Long expireTime = cooldownCache.getIfPresent(cooldownKey);
        if (expireTime == null) return 0;

        return Math.max(0, expireTime - System.currentTimeMillis());
    }

    @Override
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

    @Override
    public void syncProxy(UUID playerUUID, String cooldownOwner, long newExpireTime) {
        taskScheduler.runAsync(() -> {
            if (proxyRegistry.hasEnabledProxy()) {
                proxySender.send(FPlayer.UNKNOWN, ModuleName.UPDATE_CACHE_COOLDOWN, dataOutputStream -> {
                    dataOutputStream.writeUTF(playerUUID.toString());
                    dataOutputStream.writeUTF(cooldownOwner);
                    dataOutputStream.writeLong(newExpireTime);
                }, UUID.randomUUID());
            }
        });
    }


}
