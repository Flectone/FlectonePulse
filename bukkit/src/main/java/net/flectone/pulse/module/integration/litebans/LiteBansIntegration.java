package net.flectone.pulse.module.integration.litebans;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import litebans.api.Database;
import litebans.api.Entry;
import lombok.Getter;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.model.LiteBansModeration;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class LiteBansIntegration implements FIntegration {

    private final FLogger fLogger;

    @Getter private boolean hooked;

    @Inject
    public LiteBansIntegration(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        hooked = true;
        fLogger.info("LiteBans hooked");
    }

    public boolean isMuted(FEntity fEntity) {
        String ip = fEntity instanceof FPlayer fPlayer ? fPlayer.getIp() : null;

        return Database.get().isPlayerMuted(fEntity.getUuid(), ip);
    }

    public LiteBansModeration getMute(FEntity fEntity) {
        String ip = fEntity instanceof FPlayer fPlayer ? fPlayer.getIp() : null;

        Entry mute = Database.get().getMute(fEntity.getUuid(), ip, null);
        if (mute == null) return null;

        return new LiteBansModeration(
                fEntity.getName(),
                mute.getExecutorName(),
                mute.getReason(),
                mute.getId(),
                mute.getDateStart(),
                mute.getDuration(),
                mute.isPermanent()
        );
    }
}
