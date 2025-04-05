package net.flectone.pulse.module.integration.advancedban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Punishment;
import net.flectone.pulse.model.ExternalModeration;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class AdvancedBanIntegration implements FIntegration {

    private final FLogger fLogger;

    @Getter
    private boolean hooked;

    @Inject
    public AdvancedBanIntegration(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        hooked = true;
        fLogger.info("AdvancedBan hooked");
    }

    public boolean isMuted(FEntity fEntity) {
        return PunishmentManager.get().isMuted(getUUID(fEntity));
    }

    public ExternalModeration getMute(FEntity fEntity) {
        Punishment punishment = PunishmentManager.get().getMute(getUUID(fEntity));
        if (punishment == null) return null;

        return new ExternalModeration(
                fEntity.getName(),
                punishment.getOperator(),
                punishment.getReason(),
                punishment.getId(),
                punishment.getStart(),
                punishment.getEnd(),
                !punishment.getType().isTemp()
        );
    }

    private String getUUID(FEntity fEntity) {
        return UUIDManager.get().getUUID(fEntity.getName());
    }
}
