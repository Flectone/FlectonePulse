package net.flectone.pulse.module.integration.litebans;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import litebans.api.Database;
import litebans.api.Entry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LiteBansIntegration implements FIntegration {

    private final FLogger fLogger;

    @Getter private boolean hooked;

    @Override
    public void hook() {
        hooked = true;
        fLogger.info("✔ LiteBans hooked");
    }

    @Override
    public void unhook() {
        hooked = false;
        fLogger.info("✖ LiteBans unhooked");
    }

    public boolean isMuted(FEntity fEntity) {
        return Database.get().isPlayerMuted(fEntity.uuid(), null);
    }

    public ExternalModeration getMute(FEntity fEntity) {
        Entry mute = Database.get().getMute(fEntity.uuid(), null, null);
        if (mute == null) return null;

        return new ExternalModeration(
                fEntity.name(),
                mute.getExecutorName(),
                mute.getReason(),
                mute.getId(),
                mute.getDateStart(),
                mute.getDateEnd(),
                mute.isPermanent()
        );
    }
}
