package net.flectone.pulse.data.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.dao.ModerationDAO;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationRepository {

    // cache only for specific player
    private final Cache<CacheKey, List<Moderation>> moderationCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(5000)
            .build();

    private record CacheKey(UUID playerId, Moderation.Type type) {}

    private final ModerationDAO moderationDAO;

    public List<Moderation> getValid(FPlayer player, Moderation.Type type) {
        try {
            CacheKey key = new CacheKey(player.getUuid(), type);
            List<Moderation> cached = moderationCache.get(key, () -> moderationDAO.getValid(player, type));
            if (cached.stream().anyMatch(Moderation::isActive)) {
                return cached;
            }

            List<Moderation> valid = cached.stream()
                    .filter(Moderation::isActive)
                    .toList();

            moderationCache.put(key, valid);

            return valid;
        } catch (Exception e) {
            return List.of();
        }
    }

    public void invalidate(UUID playerId, Moderation.Type type) {
        moderationCache.invalidate(new CacheKey(playerId, type));
    }

    public void invalidateAll() {
        moderationCache.invalidateAll();
    }

    public void invalidateAll(UUID playerId) {
        for (Moderation.Type type : Moderation.Type.values()) {
            invalidate(playerId, type);
        }
    }

    public List<Moderation> get(FPlayer fPlayer, Moderation.Type type) {
        return moderationDAO.get(fPlayer, type);
    }

    public Moderation save(FPlayer fTarget, long time, String reason, int moderatorID, Moderation.Type type) {
        return moderationDAO.insert(fTarget, time, reason, moderatorID, type);
    }

    public List<Moderation> getValid(Moderation.Type type) {
        return moderationDAO.getValid(type);
    }

    public List<String> getValidNames(Moderation.Type type) {
        return moderationDAO.getValidPlayersNames(type);
    }

    public void updateValid(Moderation moderation) {
        moderationDAO.updateValid(moderation);
    }
}
