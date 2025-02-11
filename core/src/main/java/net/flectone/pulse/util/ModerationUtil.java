package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;

@Singleton
public class ModerationUtil {

    private final FileManager fileManager;
    private final FPlayerDAO fPlayerDAO;
    private final TimeUtil timeUtil;

    @Inject
    public ModerationUtil(FileManager fileManager,
                          FPlayerDAO fPlayerDAO,
                          TimeUtil timeUtil) {
        this.fileManager = fileManager;
        this.fPlayerDAO = fPlayerDAO;
        this.timeUtil = timeUtil;
    }

    public String replacePlaceholders(String string, FPlayer fReceiver, Moderation moderation) {
        Localization localization = fileManager.getLocalization(fReceiver);

        Localization.ReasonMap constantReasons = switch (moderation.getType()) {
            case BAN -> localization.getCommand().getBan().getReasons();
            case MUTE -> localization.getCommand().getMute().getReasons();
            case WARN -> localization.getCommand().getWarn().getReasons();
            case KICK -> localization.getCommand().getKick().getReasons();
        };

        FPlayer fModerator = fPlayerDAO.getFPlayer(moderation.getModerator());
        FPlayer fTarget = fPlayerDAO.getFPlayer(moderation.getPlayer());

        return string
                .replace("<player>", fTarget.getName())
                .replace("<id>", String.valueOf(moderation.getId()))
                .replace("<moderator>", fModerator.getName())
                .replace("<reason>", constantReasons.getConstant(moderation.getReason()))
                .replace("<date>", timeUtil.formatDate(moderation.getDate()))
                .replace("<time>", moderation.isPermanent()
                        ? localization.getTime().getPermanent()
                        : timeUtil.format(fReceiver, moderation.getOriginalTime())
                )
                .replace("<time_left>", moderation.isPermanent()
                        ? localization.getTime().getPermanent()
                        : timeUtil.format(fReceiver, moderation.getRemainingTime())
                );
    }

    public String buildMuteMessage(FPlayer fPlayer) {
        if (!fPlayer.isMuted()) return "";

        String format = fileManager.getLocalization(fPlayer).getCommand().getMute().getPerson();
        return replacePlaceholders(format, fPlayer, fPlayer.getMute().get());
    }
}
