package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.repository.ModerationRepository;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.integration.IntegrationModule;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationService {

    private final ModerationRepository moderationRepository;
    private final IntegrationModule integrationModule;

    public void invalidate() {
        moderationRepository.invalidateAll();
    }

    public void invalidate(UUID uuid) {
        moderationRepository.invalidateAll(uuid);
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
        return moderationRepository.getValid(fPlayer, type);
    }

    public List<Moderation> getValid(Moderation.Type type) {
        return moderationRepository.getValid(type);
    }

    public List<String> getValidNames(Moderation.Type type) {
        return moderationRepository.getValidNames(type);
    }

    @Nullable
    public Moderation kick(FPlayer fPlayer, String reason, int moderator) {
        return moderationRepository.save(fPlayer, -1, reason, moderator, Moderation.Type.KICK);
    }

    @Nullable
    public Moderation add(FPlayer fPlayer, long time, String reason, int moderator, Moderation.Type type) {
        moderationRepository.invalidate(fPlayer.uuid(), type);

        return moderationRepository.save(fPlayer, time, reason, moderator, type);
    }

    @Nullable
    public Moderation remove(FPlayer fPlayer, List<Moderation> moderations) {
        return remove(fPlayer, moderations, "");
    }

    @Nullable
    public Moderation remove(FPlayer fPlayer, List<Moderation> moderations, @NonNull String reason) {
        if (moderations.isEmpty()) return null;

        Moderation firstModeration = moderations.getFirst();
        moderationRepository.invalidate(fPlayer.uuid(), firstModeration.type());

        for (Moderation moderation : moderations) {
            moderationRepository.updateValid(moderation.withValid(false));
        }

        // save to un-moderation database
        return moderationRepository.save(fPlayer, -1, reason, firstModeration.moderator(), switch (firstModeration.type()) {
            case BAN -> Moderation.Type.UNBAN;
            case MUTE -> Moderation.Type.UNMUTE;
            case WARN -> Moderation.Type.UNWARN;
            default -> throw new IllegalArgumentException("Unknown un-moderation type: " + firstModeration.type());
        });
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
}
