package net.flectone.pulse.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Singleton;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.UUID;

@Singleton
public class PlayerSwitchServiceImpl implements PlayerSwitchService {

    private final Cache<UUID, Boolean> switching = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    @Override
    public void setSwitching(@NonNull UUID uuid, boolean switching) {
        if (switching) {
            this.switching.put(uuid, true);
        } else {
            this.switching.invalidate(uuid);
        }
    }

    @Override
    public boolean isSwitching(@NonNull UUID uuid) {
        return switching.getIfPresent(uuid) != null;
    }

}
