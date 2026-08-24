package net.flectone.pulse.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.LazyInstance;

import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ExternalModerationServiceImpl implements ExternalModerationService {

    private final @Named("externalModeration") Cache<ExternalModerationKey, Optional<ExternalModeration>> externalModerationCache;
    private final LazyInstance<IntegrationModule> integrationModule;

    @Override
    public Optional<ExternalModeration> get(FPlayer fPlayer, Moderation.Type type) {
        if (fPlayer.isUnknown()) return Optional.empty();
        if (type != Moderation.Type.MUTE) return Optional.empty();

        ExternalModerationKey key = new ExternalModerationKey(fPlayer.uuid(), type);
        return externalModerationCache.get(key, _ -> Optional.ofNullable(integrationModule.get().getMute(fPlayer)));
    }

    @Override
    public boolean isPresent(FPlayer fPlayer, Moderation.Type type) {
        return get(fPlayer, type).isPresent();
    }

    @Override
    public void invalidate(UUID uuid, Moderation.Type type) {
        externalModerationCache.invalidate(new ExternalModerationKey(uuid, type));
    }

    @Override
    public void invalidateAll() {
        externalModerationCache.invalidateAll();
    }

}