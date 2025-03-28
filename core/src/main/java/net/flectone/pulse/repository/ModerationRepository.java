package net.flectone.pulse.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.dao.ModerationDAO;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;

import java.util.List;

@Singleton
public class ModerationRepository {

    private final ModerationDAO moderationDAO;

    @Inject
    public ModerationRepository(ModerationDAO moderationDAO) {
        this.moderationDAO = moderationDAO;
    }

    public void load(FPlayer fPlayer, Moderation.Type type) {
        if (type != Moderation.Type.MUTE) return;

        List<Moderation> moderations = get(fPlayer, type);
        fPlayer.addMutes(moderations);
    }

    public List<Moderation> get(FPlayer fPlayer, Moderation.Type type) {
        return moderationDAO.get(fPlayer, type);
    }

    public Moderation save(FPlayer fTarget, long time, String reason, int moderatorID, Moderation.Type type) {
        return moderationDAO.insert(fTarget, time, reason, moderatorID, type);
    }

    public List<Moderation> getValid(FPlayer fPlayer, Moderation.Type type) {
        return moderationDAO.getValid(fPlayer, type);
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
