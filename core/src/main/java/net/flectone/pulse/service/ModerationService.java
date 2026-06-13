package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.setting.ViolationSetting;
import net.flectone.pulse.data.repository.ModerationRepository;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationService {

    private final Map<ViolationKey, List<Long>> playerViolations = new ConcurrentHashMap<>();

    private final ModerationRepository moderationRepository;
    private final FileFacade fileFacade;
    private final ProxySender proxySender;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    private Provider<IntegrationModule> integrationModuleProvider;

    public void invalidate() {
        moderationRepository.invalidateAll();
        playerViolations.clear();
    }

    public void invalidate(UUID uuid) {
        moderationRepository.invalidateAll(uuid);
        playerViolations.keySet().stream()
                .filter(violationKey -> violationKey.sender().equals(uuid))
                .forEach(playerViolations::remove);
    }

    public void invalidate(FPlayer fTarget, Moderation.Type type, int id) {
        invalidate(fTarget, type, id, getServer(type));
    }

    public void invalidate(FPlayer fTarget, Moderation.Type type, int id, @Nullable String server) {
        invalidate(fTarget.uuid(), type);

        if (id == -1) {
            moderationRepository.updateValid(fTarget.id(), type, server);
        } else {
            moderationRepository.updateValid(id, server);
        }
    }

    public void invalidate(UUID uuid, Moderation.Type type) {
        moderationRepository.invalidate(uuid, type, getServer(type));
    }

    @Nullable
    public Moderation ban(FPlayer fPlayer, long time, String reason, int moderator) {
        return add(fPlayer, time, reason, moderator, Moderation.Type.BAN);
    }

    @Nullable
    public Moderation mute(FPlayer fPlayer, long time, String reason, int moderator) {
        return add(fPlayer, time, reason, moderator, Moderation.Type.MUTE);
    }

    @Nullable
    public Moderation maintenance(FPlayer fPlayer, long time, String reason, int moderator) {
        return add(fPlayer, time, reason, moderator, Moderation.Type.MAINTENANCE);
    }

    @Nullable
    public Moderation warn(FPlayer fPlayer, long time, String reason, int moderator) {
        return add(fPlayer, time, reason, moderator, Moderation.Type.WARN);
    }

    @Nullable
    public Moderation kick(FPlayer fPlayer, String reason, int moderator) {
        return add(fPlayer, -1, reason, moderator, Moderation.Type.KICK);
    }

    @Nullable
    public Moderation whitelist(FPlayer fPlayer, long time, String reason, int moderator) {
        return add(fPlayer, time, reason, moderator, Moderation.Type.WHITELIST);
    }

    public boolean hasValid(FPlayer fTarget, Moderation.Type type) {
        return !getValid(fTarget, type, 1, 0).isEmpty();
    }

    public boolean hasValid(FPlayer fTarget, Moderation.Type type, int id) {
        return id == -1 ? hasValid(fTarget, type) : getValid(type, id).isPresent();
    }

    public Optional<Moderation> getValid(FPlayer fPlayer, Moderation.Type type) {
        return getValid(fPlayer, type, 1, 0).stream().findAny();
    }

    public List<Moderation> getValid(FPlayer fPlayer, Moderation.Type type, int limit, int offset) {
        return moderationRepository.getValid(fPlayer, type, getServer(type), limit, offset);
    }

    public List<Moderation> getValid(Moderation.Type type, int limit, int offset) {
        return moderationRepository.getValid(type, getServer(type), limit, offset);
    }

    public Optional<Moderation> getValid(Moderation.Type type, int id) {
        return moderationRepository.getValid(getServer(type), id);
    }

    public List<String> getValidNames(Moderation.Type type) {
        return moderationRepository.getValidNames(type, getServer(type));
    }

    public int getTotalValidCount(FPlayer fPlayer, Moderation.Type type, @Nullable String server) {
        return moderationRepository.getTotalValidCount(fPlayer, type, server);
    }

    public int getTotalValidCount(Moderation.Type type, @Nullable String server) {
        return moderationRepository.getTotalValidCount(type, server);
    }

    @Nullable
    public Moderation add(FPlayer fPlayer, long time, String reason, int moderator, Moderation.Type type) {
        return add(fPlayer, System.currentTimeMillis(), time, reason, moderator, type, fileFacade.config().server());
    }

    @Nullable
    public Moderation add(FPlayer fPlayer, long date, long time, String reason, int moderator, Moderation.Type type, @Nullable String server) {
        invalidate(fPlayer.uuid(), type);

        return moderationRepository.save(fPlayer, date, time, reason, moderator, type, server);
    }

    public void addViolation(UUID uuid, ModuleSimple moduleSimple, ViolationSetting violationSetting) {
        // create key
        ViolationKey violationKey = new ViolationKey(uuid, moduleSimple.name());

        // create value
        long violationValue = System.currentTimeMillis() + violationSetting.violationResetTime() * TimeFormatter.MULTIPLIER;

        // save to cache
        addViolation(violationKey, violationValue);

        // send to proxy
        proxySender.send(FPlayer.UNKNOWN, ModuleName.UPDATE_CACHE_VIOLATION, outputStream -> {
            outputStream.writeAsJson(violationKey);
            outputStream.writeLong(violationValue);
        }, UUID.randomUUID());
    }

    public void addViolation(ViolationKey violationKey, Long violationValue) {
        // get timestamps
        List<Long> timestamps = playerViolations.getOrDefault(violationKey, new CopyOnWriteArrayList<>());

        // get current time
        long currentTimestamp = System.currentTimeMillis();

        // remove old timestamps
        timestamps.removeIf(timestamp -> currentTimestamp > timestamp);

        // add new timestamp
        timestamps.add(violationValue);

        // save to cache
        playerViolations.put(violationKey, timestamps);
    }

    public boolean isViolationRestricted(UUID uuid, ModuleSimple moduleSimple, ViolationSetting violationSetting) {
        List<Long> timestamps = playerViolations.get(new ViolationKey(uuid, moduleSimple.name()));
        if (timestamps == null || timestamps.isEmpty()) return false;

        long currentTimestamp = System.currentTimeMillis();
        return timestamps.stream().filter(timestamp -> timestamp > currentTimestamp).count() >= violationSetting.violationLimit();
    }

    public Long getFirstViolationTimestamp(UUID uuid, ModuleSimple moduleSimple) {
        List<Long> timestamps = playerViolations.get(new ViolationKey(uuid, moduleSimple.name()));
        if (timestamps == null || timestamps.isEmpty()) return null;

        return timestamps.getLast();
    }


    @Nullable
    public Moderation remove(FPlayer fModerator, FPlayer fTarget, Moderation.Type type, int id, @Nullable String reason) {
        return remove(fModerator, fTarget, type, id, reason, getServer(type));
    }

    @Nullable
    public Moderation remove(FPlayer fModerator, FPlayer fTarget, Moderation.Type type, int id, @Nullable String reason, @Nullable String server) {
        return remove(fModerator, fTarget, type, -1, id, reason, server);
    }

    @Nullable
    public Moderation remove(FPlayer fModerator, FPlayer fTarget, Moderation.Type type, long time, int id, @Nullable String reason) {
        return remove(fModerator, fTarget, type, time, id, reason, getServer(type));
    }

    @Nullable
    public Moderation remove(FPlayer fModerator, FPlayer fTarget, Moderation.Type type, long time, int id, @Nullable String reason, @Nullable String server) {
        invalidate(fTarget, type, id, server);

        // save to un-moderation database
        return moderationRepository.save(fTarget, System.currentTimeMillis(), time, reason, fModerator.id(), switch (type) {
            case BAN -> Moderation.Type.UNBAN;
            case MUTE -> Moderation.Type.UNMUTE;
            case MAINTENANCE -> Moderation.Type.UNMAINTENANCE;
            case WARN -> Moderation.Type.UNWARN;
            case WHITELIST -> Moderation.Type.UNWHITELIST;
            default -> throw new IllegalArgumentException("Unknown un-moderation type: " + type);
        }, server);
    }

    public boolean isAllowedTime(FPlayer fPlayer, long time, Map<Integer, Long> timeLimits) {
        if (time != -1 && time < 1) return false;
        if (timeLimits.isEmpty()) return true;

        int groupWeight = integrationModuleProvider.get().getGroupWeight(fPlayer);

        long timeLimit = -1;
        for (Map.Entry<Integer, Long> timeEntry : timeLimits.entrySet()) {
            if (groupWeight >= timeEntry.getKey()) {
                if (timeEntry.getValue() == -1) return true;
                if (timeEntry.getValue() > timeLimit) {
                    timeLimit = timeEntry.getValue();
                }
            }
        }

        return time != -1 && timeLimit != -1 && timeLimit >= time;
    }

    public boolean hasHigherGroupThan(FPlayer source, FPlayer target) {
        if (source.isConsole()) return true;

        boolean sourceIsOperator = platformPlayerAdapter.isOperator(source);
        boolean targetIsOperator = platformPlayerAdapter.isOperator(target);
        if (!sourceIsOperator && targetIsOperator) return false;
        if (sourceIsOperator && !targetIsOperator) return true;

        IntegrationModule integrationModule = integrationModuleProvider.get();
        return integrationModule.getGroupWeight(source) > integrationModule.getGroupWeight(target);
    }

    public String getServer(Moderation.Type type) {
        return type == Moderation.Type.BAN && fileFacade.command().ban().filterByServer()
                || type == Moderation.Type.MUTE && fileFacade.command().mute().filterByServer()
                || type == Moderation.Type.WARN && fileFacade.command().warn().filterByServer()
                || type == Moderation.Type.WHITELIST && fileFacade.command().whitelist().filterByServer()
                || type == Moderation.Type.MAINTENANCE && fileFacade.command().maintenance().filterByServer()
                ? fileFacade.config().server()
                : null;
    }

    public record ViolationKey(
            UUID sender,
            ModuleName module
    ){}
}
