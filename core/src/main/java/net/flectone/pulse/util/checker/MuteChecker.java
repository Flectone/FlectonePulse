package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.service.ModerationService;

@Singleton
public class MuteChecker {

    private final ModerationService moderationService;
    private final Provider<IntegrationModule> integrationModuleProvider;
    private final Provider<NewbieModule> newbieModule;

    @Inject
    public MuteChecker(ModerationService moderationService,
                       Provider<IntegrationModule> integrationModuleProvider,
                       Provider<NewbieModule> newbieModuleProvider) {
        this.moderationService = moderationService;
        this.integrationModuleProvider = integrationModuleProvider;
        this.newbieModule = newbieModuleProvider;
    }

    public Status check(FPlayer fPlayer) {
        if (!moderationService.getValidMutes(fPlayer).isEmpty()) {
            return Status.LOCAL;
        }

        if (newbieModule.get().isNewBie(fPlayer)) {
            return Status.NEWBIE;
        }

        if (integrationModuleProvider.get().isMuted(fPlayer)) {
            return Status.EXTERNAL;
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
