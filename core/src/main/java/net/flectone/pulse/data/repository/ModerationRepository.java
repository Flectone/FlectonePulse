package net.flectone.pulse.data.repository;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.dao.ModerationDAO;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import org.incendo.cloud.type.tuple.Pair;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationRepository {

    private final @Named("moderation") Cache<Pair<UUID, Moderation.Type>, List<Moderation>> moderationCache;
    private final ModerationDAO moderationDAO;

    public List<Moderation> getValid(FPlayer player, Moderation.Type type) {
        try {
            Pair<UUID, Moderation.Type> key = Pair.of(player.getUuid(), type);
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
        moderationCache.invalidate(Pair.of(playerId, type));
    }

    public void invalidateAll() {
        moderationCache.invalidateAll();
    }

    public void invalidateAll(UUID playerId) {
        moderationCache.asMap().keySet().removeIf(key ->
                key.first().equals(playerId)
        );
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
