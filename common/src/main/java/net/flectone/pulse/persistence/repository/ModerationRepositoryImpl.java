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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationRepositoryImpl implements ModerationRepository {

    private static final UUID SHARED_KEY = UUID.nameUUIDFromBytes("flectonepulse:moderation:shared".getBytes(StandardCharsets.UTF_8));

    private static final String VALID_KEY = "valid";
    private static final String ALL_KEY = "all";

    private final @Named("moderation") Cache<UUID, Map<String, List<Moderation>>> moderationCache;
    private final ModerationDAO moderationDAO;

    @Override
    public List<Moderation> getValid(@NonNull FPlayer player, Moderation.Type type, @Nullable String server, int limit, int offset) {
        if (player.isUnknown()) return moderationDAO.getValid(player, type, server, limit, offset);

        return getValidCache(player.uuid(), cacheKey(type, server, VALID_KEY, limit, offset), () -> moderationDAO.getValid(player, type, server, limit, offset));
    }

    @Override
    public List<Moderation> getAll(@NonNull FPlayer player, Moderation.Type type, @Nullable String server, int limit, int offset) {
        if (player.isUnknown()) return moderationDAO.getAll(player, type, server, limit, offset);

        return getAllCache(player.uuid(), cacheKey(type, server, ALL_KEY, limit, offset), () -> moderationDAO.getAll(player, type, server, limit, offset));
    }

    @Override
    public List<Moderation> getValid(Moderation.Type type, @Nullable String server, int limit, int offset) {
        return getValidCache(SHARED_KEY, cacheKey(type, server, VALID_KEY, limit, offset), () -> moderationDAO.getValid(type, server, limit, offset));
    }

    @Override
    public Optional<Moderation> getValid(@Nullable String server, int id) {
        List<Moderation> moderations = getValidCache(SHARED_KEY, "id:" + server + ":" + id, () -> moderationDAO.getValidById(server, id)
                .map(List::of)
                .orElseGet(List::of)
        );

        return moderations.stream().findAny();
    }

    @Override
    public void invalidate(@NonNull UUID playerId, Moderation.Type type, @Nullable String server) {
        Map<String, List<Moderation>> playerModerations = moderationCache.getIfPresent(playerId);
        if (playerModerations != null) {
            playerModerations.keySet().removeIf(key -> key.startsWith(type.name()));

            if (playerModerations.isEmpty()) {
                moderationCache.invalidate(playerId);
            }
        }

        moderationCache.invalidate(SHARED_KEY);
    }

    @Override
    public void invalidateAll() {
        moderationCache.invalidateAll();
    }

    @Override
    public void invalidateAll(@NonNull UUID playerId) {
        moderationCache.invalidate(playerId);
        moderationCache.invalidate(SHARED_KEY);
    }

    @Override
    public Moderation save(@NonNull FPlayer fTarget, long date, long time, String reason, int moderatorID, Moderation.Type type, String server) {
        Moderation moderation = moderationDAO.insert(fTarget, date, time, reason, moderatorID, type, server);

        invalidate(fTarget.uuid(), type, server);

        return moderation;
    }

    @Override
    public List<String> getValidNames(Moderation.Type type, @Nullable String server) {
        return moderationDAO.getValidPlayersNames(type, server);
    }

    @Override
    public int getTotalCount(FPlayer fPlayer, Moderation.Type type, @Nullable String server) {
        return moderationDAO.getTotalCount(fPlayer, type, server);
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
        moderationCache.invalidate(SHARED_KEY);
    }

    @Override
    public void updateValid(int playerId, Moderation.@NonNull Type type, @Nullable String server) {
        moderationDAO.updateValid(playerId, type, server);
        moderationCache.invalidate(SHARED_KEY);
    }

    private List<Moderation> getValidCache(UUID cacheKey, String key, Supplier<List<Moderation>> loader) {
        Map<String, List<Moderation>> moderations = moderationCache.get(cacheKey, _ -> new ConcurrentHashMap<>());

        List<Moderation> cached = moderations.get(key);
        if (cached == null) {
            List<Moderation> loaded = loader.get();

            moderations.put(key, loaded);

            return loaded;
        }

        if (cached.stream().allMatch(Moderation::isActive)) {
            return cached;
        }

        List<Moderation> active = cached.stream()
                .filter(Moderation::isActive)
                .toList();

        moderations.put(key, active);

        return active;
    }

    private List<Moderation> getAllCache(UUID cacheKey, String key, Supplier<List<Moderation>> loader) {
        Map<String, List<Moderation>> moderations = moderationCache.get(cacheKey, _ -> new ConcurrentHashMap<>());

        List<Moderation> cached = moderations.get(key);
        if (cached != null) return cached;

        List<Moderation> loaded = loader.get();

        moderations.put(key, loaded);

        return loaded;
    }

    private String cacheKey(Moderation.Type type, @Nullable String server, String kind, int limit, int offset) {
        return type.name() + ":" + server + ":" + kind + ":" + limit + ":" + offset;
    }

}