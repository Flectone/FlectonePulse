package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.setting.ViolationSetting;
import net.flectone.pulse.data.repository.ModerationRepository;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.util.checker.MuteChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationService {

    private final Map<ViolationKey, List<Long>> playerViolations = new ConcurrentHashMap<>();

    private final ModerationRepository moderationRepository;
    private final IntegrationModule integrationModule;
    private final FileFacade fileFacade;
    private final ProxySender proxySender;

    public void invalidate() {
        moderationRepository.invalidateAll();
        playerViolations.clear();
    }

    public void invalidate(UUID uuid) {
        moderationRepository.invalidateAll(uuid);
        Arrays.stream(MuteChecker.Status.values()).forEach(status -> playerViolations.remove(Pair.of(uuid, status)));
    }

    public void invalidateMutes(UUID uuid) {
        moderationRepository.invalidate(uuid, Moderation.Type.MUTE);
    }

    public void invalidateBans(UUID uuid) {
        moderationRepository.invalidate(uuid, Moderation.Type.BAN);
    }

    public void invalidateWarns(UUID uuid) {
        moderationRepository.invalidate(uuid, Moderation.Type.WARN);
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
    public Moderation warn(FPlayer fPlayer, long time, String reason, int moderator) {
        return add(fPlayer, time, reason, moderator, Moderation.Type.WARN);
    }

    @Nullable
    public Moderation kick(FPlayer fPlayer, String reason, int moderator) {
        return add(fPlayer, -1, reason, moderator, Moderation.Type.KICK);
    }

    public List<Moderation> getValidMutes(FPlayer fPlayer) {
        return getValid(fPlayer, Moderation.Type.MUTE);
    }

    public List<Moderation> getValidMutes() {
        return getValid(Moderation.Type.MUTE);
    }

    public List<Moderation> getValidBans(FPlayer fPlayer) {
        return getValid(fPlayer, Moderation.Type.BAN);
    }

    public List<Moderation> getValidBans() {
        return getValid(Moderation.Type.BAN);
    }

    public List<Moderation> getValidWarns(FPlayer fPlayer) {
        return getValid(fPlayer, Moderation.Type.WARN);
    }

    public List<Moderation> getValidWarns() {
        return getValid(Moderation.Type.WARN);
    }

    public List<Moderation> getValid(FPlayer fPlayer, Moderation.Type type) {
        return moderationRepository.getValid(fPlayer, type, getServer(type));
    }

    public List<Moderation> getValid(Moderation.Type type) {
        return moderationRepository.getValid(type, getServer(type));
    }

    public List<String> getValidNames(Moderation.Type type) {
        return moderationRepository.getValidNames(type, getServer(type));
    }

    @Nullable
    public Moderation add(FPlayer fPlayer, long time, String reason, int moderator, Moderation.Type type) {
        return add(fPlayer, System.currentTimeMillis(), time, reason, moderator, type, fileFacade.config().serverUuid());
    }

    @Nullable
    public Moderation add(FPlayer fPlayer, long date, long time, String reason, int moderator, Moderation.Type type, @Nullable String server) {
        moderationRepository.invalidate(fPlayer.uuid(), type);

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
        proxySender.send(FPlayer.UNKNOWN, ModuleName.SYSTEM_VIOLATION, outputStream -> {
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
        List<Long> timestamps = playerViolations.get(Pair.of(uuid, moduleSimple.name()));
        if (timestamps == null || timestamps.isEmpty()) return false;

        long currentTimestamp = System.currentTimeMillis();
        return timestamps.stream().filter(timestamp -> timestamp > currentTimestamp).count() >= violationSetting.violationLimit();
    }

    public Long getFirstViolationTimestamp(UUID uuid, ModuleSimple moduleSimple) {
        List<Long> timestamps = playerViolations.get(Pair.of(uuid, moduleSimple.name()));
        if (timestamps == null || timestamps.isEmpty()) return null;

        return timestamps.getLast();
    }


    @Nullable
    public Moderation remove(FPlayer fPlayer, List<Moderation> moderations) {
        return remove(fPlayer, moderations, "", fileFacade.config().serverUuid());
    }

    @Nullable
    public Moderation remove(FPlayer fPlayer, List<Moderation> moderations, @NonNull String reason) {
        return remove(fPlayer, moderations, reason, fileFacade.config().serverUuid());
    }

    @Nullable
    public Moderation remove(FPlayer fPlayer, List<Moderation> moderations, @NonNull String reason, @Nullable String server) {
        if (moderations.isEmpty()) return null;

        Moderation firstModeration = moderations.getFirst();
        moderationRepository.invalidate(fPlayer.uuid(), firstModeration.type());

        for (Moderation moderation : moderations) {
            moderationRepository.updateValid(moderation.withValid(false));
        }

        // save to un-moderation database
        return moderationRepository.save(fPlayer, System.currentTimeMillis(), -1, reason, firstModeration.moderator(), switch (firstModeration.type()) {
            case BAN -> Moderation.Type.UNBAN;
            case MUTE -> Moderation.Type.UNMUTE;
            case WARN -> Moderation.Type.UNWARN;
            default -> throw new IllegalArgumentException("Unknown un-moderation type: " + firstModeration.type());
        }, server);
    }

    public boolean isAllowedTime(FPlayer fPlayer, long time, Map<Integer, Long> timeLimits) {
        if (time != -1 && time < 1) return false;
        if (timeLimits.isEmpty()) return true;

        int groupWeight = integrationModule.getGroupWeight(fPlayer);

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

    private String getServer(Moderation.Type type) {
        return type == Moderation.Type.BAN && fileFacade.command().ban().filterByServer()
                || type == Moderation.Type.MUTE && fileFacade.command().mute().filterByServer()
                || type == Moderation.Type.WARN && fileFacade.command().warn().filterByServer()
                ? fileFacade.config().serverUuid()
                : null;
    }

    public record ViolationKey(
            UUID sender,
            ModuleName module
    ){}
}
