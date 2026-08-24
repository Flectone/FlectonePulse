package net.flectone.pulse.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.LazyInstance;

import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ExternalMuteServiceImpl implements ExternalMuteService {

    private final @Named("externalModeration") Cache<UUID, Optional<ExternalModeration>> externalMuteCache;
    private final LazyInstance<IntegrationModule> integrationModule;

    @Override
    public Optional<ExternalModeration> get(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return Optional.empty();

        return externalMuteCache.get(fPlayer.uuid(), _ -> Optional.ofNullable(integrationModule.get().getMute(fPlayer)));
    }

    @Override
    public boolean isMuted(FPlayer fPlayer) {
        return get(fPlayer).isPresent();
    }

    @Override
    public void invalidate(UUID playerId) {
        externalMuteCache.invalidate(playerId);
    }

    @Override
    public void invalidateAll() {
        externalMuteCache.invalidateAll();
    }


}
