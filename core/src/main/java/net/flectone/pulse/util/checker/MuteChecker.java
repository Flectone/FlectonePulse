package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.moderation.caps.CapsModule;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;
import net.flectone.pulse.service.ModerationService;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MuteChecker {

    private final ModerationService moderationService;
    private final Provider<IntegrationModule> integrationModuleProvider;
    private final Provider<CapsModule> capsModuleProvider;
    private final Provider<FloodModule> floodModuleProvider;
    private final Provider<NewbieModule> newbieModuleProvider;
    private final Provider<SwearModule> swearModuleProvider;

    public Status check(FPlayer fPlayer) {
        if (moderationService.hasValid(fPlayer, Moderation.Type.MUTE)) {
            return Status.LOCAL;
        }

        if (newbieModuleProvider.get().isNewBie(fPlayer)) {
            return Status.NEWBIE;
        }

        if (capsModuleProvider.get().isRestricted(fPlayer.uuid())) {
            return Status.CAPS;
        }

        if (floodModuleProvider.get().isRestricted(fPlayer.uuid())) {
            return Status.FLOOD;
        }

        if (swearModuleProvider.get().isRestricted(fPlayer.uuid())) {
            return Status.SWEAR;
        }

        if (integrationModuleProvider.get().isMuted(fPlayer)) {
            return Status.EXTERNAL;
        }

        return Status.NONE;
    }

    public enum Status {
        LOCAL,
        EXTERNAL,
        CAPS,
        FLOOD,
        NEWBIE,
        SWEAR,
        NONE
    }

}
