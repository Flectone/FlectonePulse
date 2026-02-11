package net.flectone.pulse.module.integration.advancedban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Punishment;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AdvancedBanIntegration implements FIntegration {

    private final FLogger fLogger;

    @Getter
    private boolean hooked;

    @Override
    public void hook() {
        hooked = true;
        fLogger.info("✔ AdvancedBan hooked");
    }

    @Override
    public void unhook() {
        hooked = false;
        fLogger.info("✖ AdvancedBan unhooked");
    }

    public boolean isMuted(FEntity fEntity) {
        return PunishmentManager.get().isMuted(getUUID(fEntity));
    }

    public ExternalModeration getMute(FEntity fEntity) {
        Punishment punishment = PunishmentManager.get().getMute(getUUID(fEntity));
        if (punishment == null) return null;

        return new ExternalModeration(
                fEntity.name(),
                punishment.getOperator(),
                punishment.getReason(),
                punishment.getId(),
                punishment.getStart(),
                punishment.getEnd(),
                !punishment.getType().isTemp()
        );
    }

    private String getUUID(FEntity fEntity) {
        return UUIDManager.get().getUUID(fEntity.name());
    }
}
