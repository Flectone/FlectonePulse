package net.flectone.pulse.module.integration.cmi;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CMIIntegration implements FIntegration {

    private final Provider<FPlayerService> fPlayerServiceProvider;
    @Getter private final FLogger fLogger;

    private CMI cmi;

    @Override
    public String getIntegrationName() {
        return "CMI";
    }

    @Override
    public void hook() {
        cmi = CMI.getInstance();
        logHook();
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

        return new ExternalModeration(
                fEntity.name(),
                fPlayerServiceProvider.get().getConsole().name(),
                user.getMutedReason(),
                0,
                Math.max(user.getMutedUntil() - System.currentTimeMillis(), System.currentTimeMillis()),
                user.getMutedUntil(),
                false
        );
    }

    public boolean isHooked() {
        return cmi != null;
    }

}
