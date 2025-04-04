package net.flectone.pulse.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.service.FPlayerService;

import java.util.Optional;

@Singleton
public class ModerationMessageFormatter {

    private final FileManager fileManager;
    private final FPlayerService fPlayerService;
    private final TimeFormatter timeFormatter;

    @Inject
    public ModerationMessageFormatter(FileManager fileManager,
                                      FPlayerService fPlayerService,
                                      TimeFormatter timeFormatter) {
        this.fileManager = fileManager;
        this.fPlayerService = fPlayerService;
        this.timeFormatter = timeFormatter;
    }

    public String replacePlaceholders(String string, FPlayer fReceiver, Moderation moderation) {
        Localization localization = fileManager.getLocalization(fReceiver);

        Localization.ReasonMap constantReasons = switch (moderation.getType()) {
            case BAN -> localization.getCommand().getBan().getReasons();
            case MUTE -> localization.getCommand().getMute().getReasons();
            case WARN -> localization.getCommand().getWarn().getReasons();
            case KICK -> localization.getCommand().getKick().getReasons();
        };

        FPlayer fModerator = fPlayerService.getFPlayer(moderation.getModerator());
        FPlayer fTarget = fPlayerService.getFPlayer(moderation.getPlayer());

        return string
                .replace("<player>", fTarget.getName())
                .replace("<id>", String.valueOf(moderation.getId()))
                .replace("<moderator>", fModerator.getName())
                .replace("<reason>", constantReasons.getConstant(moderation.getReason()))
                .replace("<date>", timeFormatter.formatDate(moderation.getDate()))
                .replace("<time>", moderation.isPermanent()
                        ? localization.getTime().getPermanent()
                        : timeFormatter.format(fReceiver, moderation.getOriginalTime())
                )
                .replace("<time_left>", moderation.isPermanent()
                        ? localization.getTime().getPermanent()
                        : timeFormatter.format(fReceiver, moderation.getRemainingTime())
                );
    }

    public String buildMuteMessage(FPlayer fPlayer) {
        Optional<Moderation> mute = fPlayer.getMute();
        if (mute.isEmpty()) return "";

        String format = fileManager.getLocalization(fPlayer).getCommand().getMute().getPerson();
        return replacePlaceholders(format, fPlayer, mute.get());
    }
}