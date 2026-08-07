package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.repository.CooldownRepository;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownCheckerImpl implements CooldownChecker {

    private final CooldownRepository cooldownRepository;

    @Override
    public boolean check(UUID playerUUID, Cooldown cooldown, @NonNull String cooldownOwner) {
        if (cooldown == null || !cooldown.enable()) return false;

        long currentTimeMillis = System.currentTimeMillis();
        long newExpireTime = currentTimeMillis + cooldown.duration() * TimeFormatter.MULTIPLIER;
        if (cooldownRepository.updateCache(playerUUID, cooldownOwner, newExpireTime)) {
            cooldownRepository.syncProxy(playerUUID, cooldownOwner, newExpireTime);
            return false;
        }

        return true;
    }


}
