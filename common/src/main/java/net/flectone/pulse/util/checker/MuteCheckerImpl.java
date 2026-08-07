package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
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
import net.flectone.pulse.util.LazyInstance;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MuteCheckerImpl implements MuteChecker {

    private final ModerationService moderationService;
    private final LazyInstance<IntegrationModule> integrationModule;
    private final LazyInstance<CapsModule> capsModule;
    private final LazyInstance<FloodModule> floodModule;
    private final LazyInstance<NewbieModule> newbieModule;
    private final LazyInstance<SwearModule> swearModule;

    @Override
    public Status check(FPlayer fPlayer) {
        if (moderationService.hasValid(fPlayer, Moderation.Type.MUTE)) {
            return Status.LOCAL;
        }

        if (newbieModule.get().isNewBie(fPlayer)) {
            return Status.NEWBIE;
        }

        if (capsModule.get().isRestricted(fPlayer.uuid())) {
            return Status.CAPS;
        }

        if (floodModule.get().isRestricted(fPlayer.uuid())) {
            return Status.FLOOD;
        }

        if (swearModule.get().isRestricted(fPlayer.uuid())) {
            return Status.SWEAR;
        }

        if (integrationModule.get().isMuted(fPlayer)) {
            return Status.EXTERNAL;
        }

        return Status.NONE;
    }

}