package net.flectone.pulse.persistence.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.persistence.database.dao.ModerationDAO;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationRepositoryImpl implements ModerationRepository {

    private final @Named("moderation") Cache<UUID, Map<String, List<Moderation>>> moderationCache;
    private final ModerationDAO moderationDAO;

    @Override
    public List<Moderation> getValid(@NonNull FPlayer player, Moderation.Type type, @Nullable String server, int limit, int offset) {
        Map<String, List<Moderation>> playerModerations = moderationCache.getIfPresent(player.uuid());
        if (playerModerations == null) {
            playerModerations = new ConcurrentHashMap<>();
            moderationCache.put(player.uuid(), playerModerations);
        }

        String typeServerKey = type.name() + server;
        List<Moderation> moderations = playerModerations.get(typeServerKey);
        if (moderations == null) {
            // get from database
            moderations = moderationDAO.getValid(player, type, server, limit, offset);

            // add to cache
            playerModerations.put(typeServerKey, moderations);

            return moderations;
        }

        if (moderations.stream().allMatch(Moderation::isActive)) {
            return moderations;
        }

        List<Moderation> valid = moderations.stream()
                .filter(Moderation::isActive)
                .toList();

        playerModerations.put(typeServerKey, valid);

        return valid;
    }

    @Override
    public void invalidate(@NonNull UUID playerId, Moderation.Type type, @Nullable String server) {
        Map<String, List<Moderation>> playerModerations = moderationCache.getIfPresent(playerId);
        if (playerModerations == null) return;

        playerModerations.remove(type.name() + server);

        // remove cache key if map empty
        if (playerModerations.isEmpty()) {
            moderationCache.invalidate(playerId);
        }
    }

    @Override
    public void invalidateAll() {
        moderationCache.invalidateAll();
    }

    @Override
    public void invalidateAll(@NonNull UUID playerId) {
        moderationCache.invalidate(playerId);
    }

    @Override
    public Moderation save(@NonNull FPlayer fTarget, long date, long time, String reason, int moderatorID, Moderation.Type type, String server) {
        return moderationDAO.insert(fTarget, date, time, reason, moderatorID, type, server);
    }

    @Override
    public List<Moderation> getValid(Moderation.Type type, @Nullable String server, int limit, int offset) {
        return moderationDAO.getValid(type, server, limit, offset);
    }

    @Override
    public Optional<Moderation> getValid(@Nullable String server, int id) {
        return moderationDAO.getValidById(server, id);
    }

    @Override
    public List<String> getValidNames(Moderation.Type type, @Nullable String server) {
        return moderationDAO.getValidPlayersNames(type, server);
    }

    @Override
    public int getTotalValidCount(FPlayer fPlayer, Moderation.Type type, @Nullable String server) {
        return moderationDAO.getTotalValidCount(fPlayer, type, server);
    }

    @Override
    public int getTotalValidCount(Moderation.Type type, @Nullable String server) {
        return moderationDAO.getTotalValidCount(type, server);
    }

    @Override
    public void updateValid(int id, @Nullable String server) {
        moderationDAO.updateValid(id, server);
    }

    @Override
    public void updateValid(int playerId, Moderation.@NonNull Type type, @Nullable String server) {
        moderationDAO.updateValid(playerId, type, server);
    }

}