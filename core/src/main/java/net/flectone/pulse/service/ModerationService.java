package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.repository.ModerationRepository;

import java.util.List;
import java.util.UUID;

@Singleton
public class ModerationService {

    private final ModerationRepository moderationRepository;

    @Inject
    public ModerationService(ModerationRepository moderationRepository) {
        this.moderationRepository = moderationRepository;
    }

    public void reload() {
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

    public Moderation ban(FPlayer fPlayer, long time, String reason, int moderator) {
        return add(fPlayer, time, reason, moderator, Moderation.Type.BAN);
    }

    public Moderation mute(FPlayer fPlayer, long time, String reason, int moderator) {
        return add(fPlayer, time, reason, moderator, Moderation.Type.MUTE);
    }

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

    public Moderation kick(FPlayer fPlayer, String reason, int moderator) {
        return moderationRepository.save(fPlayer, -1, reason, moderator, Moderation.Type.KICK);
    }

    public Moderation add(FPlayer fPlayer, long time, String reason, int moderator, Moderation.Type type) {
        moderationRepository.invalidate(fPlayer.getUuid(), type);

        return moderationRepository.save(fPlayer, time, reason, moderator, type);
    }

    public void remove(FPlayer fPlayer, List<Moderation> moderations) {
        if (moderations.isEmpty()) return;

        moderationRepository.invalidate(fPlayer.getUuid(), moderations.get(0).getType());

        for (Moderation moderation : moderations) {
            moderation.setInvalid();
            moderationRepository.updateValid(moderation);
        }
    }
}
