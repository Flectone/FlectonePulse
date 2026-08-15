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
import java.util.concurrent.atomic.AtomicLong;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownRepositoryImpl implements CooldownRepository {

    private final @Named("cooldown") Cache<CooldownKey, AtomicLong> cooldownCache;
    private final ProxyRegistry proxyRegistry;
    private final ProxySender proxySender;
    private final TaskScheduler taskScheduler;

    @Override
    public long getTimeLeft(UUID playerUUID, Cooldown cooldown, String cooldownOwner) {
        if (cooldown == null || !cooldown.enable()) return 0;

        CooldownKey cooldownKey = new CooldownKey(playerUUID, cooldownOwner);

        AtomicLong expireTime = cooldownCache.getIfPresent(cooldownKey);
        if (expireTime == null) return 0;

        return Math.max(0, expireTime.get() - System.currentTimeMillis());
    }

    @Override
    public boolean updateCache(UUID playerUUID, String cooldownClass, long newExpireTime) {
        CooldownKey cooldownKey = new CooldownKey(playerUUID, cooldownClass);
        long currentTime = System.currentTimeMillis();

        AtomicLong expireTime = cooldownCache.get(cooldownKey, _ -> new AtomicLong());

        long previous = expireTime.getAndAccumulate(newExpireTime, (current, candidate) ->
                current >= currentTime || candidate < current ? current : candidate);

        return previous < currentTime;
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
