package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.repository.ModerationRepository;

import java.util.List;

@Singleton
public class ModerationService {

    private final ModerationRepository moderationRepository;

    @Inject
    public ModerationService(ModerationRepository moderationRepository) {
        this.moderationRepository = moderationRepository;
    }

    public void load(FPlayer fPlayer, Moderation.Type type) {
        moderationRepository.load(fPlayer, type);
    }

    public Moderation ban(FPlayer fPlayer, long time, String reason, int moderator) {
        return moderationRepository.save(fPlayer, time, reason, moderator, Moderation.Type.BAN);
    }

    public Moderation mute(FPlayer fPlayer, long time, String reason, int moderator) {
        return moderationRepository.save(fPlayer, time, reason, moderator, Moderation.Type.MUTE);
    }

    public Moderation warn(FPlayer fPlayer, long time, String reason, int moderator) {
        return moderationRepository.save(fPlayer, time, reason, moderator, Moderation.Type.WARN);
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

    public void setInvalid(Moderation moderation) {
        moderation.setInvalid();
        moderationRepository.updateValid(moderation);
    }
}
