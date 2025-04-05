package net.flectone.pulse.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.repository.ModerationRepository;

@Singleton
public class MuteChecker {

    private final ModerationRepository moderationRepository;
    private final IntegrationModule integrationModule;

    @Inject
    public MuteChecker(ModerationRepository moderationRepository,
                       IntegrationModule integrationModule) {
        this.moderationRepository = moderationRepository;
        this.integrationModule = integrationModule;
    }

    public Status check(FPlayer fPlayer) {
        if (!moderationRepository.getValid(fPlayer, Moderation.Type.MUTE).isEmpty()) {
            return Status.LOCAL;
        }

        if (integrationModule.isMuted(fPlayer)) {
            return Status.EXTERNAL;
        }

        return Status.NONE;
    }

    public enum Status {
        LOCAL,
        EXTERNAL,
        NONE
    }

}
