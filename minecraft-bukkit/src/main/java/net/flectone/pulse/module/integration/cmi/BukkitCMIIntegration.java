package net.flectone.pulse.module.integration.cmi;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.LazyInstance;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitCMIIntegration implements FIntegration {

    private final LazyInstance<FPlayerService> fPlayerService;
    @Getter private final FLogger fLogger;

    private CMI cmi;

    @Override
    public String getIntegrationName() {
        return "CMI";
    }

    @Override
    public void hook() {
        try {
            cmi = CMI.getInstance();
            logHook();
        } catch (Exception e) {
            logHookFailed(e);
        }
    }

    public boolean isMuted(FEntity fEntity) {
        if (cmi == null) return false;

        CMIUser user = cmi.getPlayerManager().getUser(fEntity.name());
        if (user == null) return false;

        return user.isMuted();
    }

    public ExternalModeration getMute(FEntity fEntity) {
        if (cmi == null) return null;

        CMIUser user = cmi.getPlayerManager().getUser(fEntity.uuid());
        if (user == null) return null;

        long mutedUntil = user.getMutedUntil();
        boolean permanent = mutedUntil <= 0 || mutedUntil == Long.MAX_VALUE;

        return new ExternalModeration(
                fEntity.name(),
                fPlayerService.get().getConsole().name(),
                user.getMutedReason(),
                0,
                Math.max(user.getMutedUntil() - System.currentTimeMillis(), System.currentTimeMillis()),
                user.getMutedUntil(),
                permanent
        );
    }

    public boolean isHooked() {
        return cmi != null;
    }

}