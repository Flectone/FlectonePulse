package net.flectone.pulse.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.service.ModerationService;

@Singleton
public class MuteChecker {

    private final ModerationService moderationService;
    private final IntegrationModule integrationModule;
    private final NewbieModule newbieModule;

    @Inject
    public MuteChecker(ModerationService moderationService,
                       IntegrationModule integrationModule,
                       NewbieModule newbieModule) {
        this.moderationService = moderationService;
        this.integrationModule = integrationModule;
        this.newbieModule = newbieModule;
    }

    public Status check(FPlayer fPlayer) {
        if (!moderationService.getValidMutes(fPlayer).isEmpty()) {
            return Status.LOCAL;
        }

        if (integrationModule.isMuted(fPlayer)) {
            return Status.EXTERNAL;
        }

        if (newbieModule.isNewBie(fPlayer)) {
            return Status.NEWBIE;
        }

        return Status.NONE;
    }

    public enum Status {
        LOCAL,
        EXTERNAL,
        NEWBIE,
        NONE
    }

}
