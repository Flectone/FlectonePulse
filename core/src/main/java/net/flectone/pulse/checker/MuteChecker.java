package net.flectone.pulse.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.repository.ModerationRepository;

@Singleton
public class MuteChecker {

    private final ModerationRepository moderationRepository;
    private final IntegrationModule integrationModule;
    private final NewbieModule newbieModule;

    @Inject
    public MuteChecker(ModerationRepository moderationRepository,
                       IntegrationModule integrationModule,
                       NewbieModule newbieModule) {
        this.moderationRepository = moderationRepository;
        this.integrationModule = integrationModule;
        this.newbieModule = newbieModule;
    }

    public Status check(FPlayer fPlayer) {
        if (!moderationRepository.getValid(fPlayer, Moderation.Type.MUTE).isEmpty()) {
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
