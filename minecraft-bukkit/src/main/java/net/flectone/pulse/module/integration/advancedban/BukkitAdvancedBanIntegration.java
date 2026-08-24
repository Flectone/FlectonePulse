package net.flectone.pulse.module.integration.advancedban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.bukkit.event.PunishmentEvent;
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.service.ExternalMuteService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitAdvancedBanIntegration implements Listener, FIntegration {

    private final ExternalMuteService externalMuteService;
    @Getter private final FLogger fLogger;

    @Getter private boolean hooked;

    @Override
    public String getIntegrationName() {
        return "AdvancedBan";
    }

    @Override
    public void hook() {
        hooked = true;
        logHook();
    }

    @Override
    public void unhook() {
        hooked = false;
        logUnhook();
    }

    @EventHandler
    public void onPunishmentEvent(PunishmentEvent event) {
        invalidateIfMute(event.getPunishment());
    }

    @EventHandler
    public void onRevokePunishmentEvent(RevokePunishmentEvent event) {
        invalidateIfMute(event.getPunishment());
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

    private void invalidateIfMute(Punishment punishment) {
        if (punishment == null || punishment.getType().getBasic() != PunishmentType.MUTE) return;

        String storedUuid = punishment.getUuid();
        if (storedUuid == null) return;

        UUID uuid = UUIDManager.get().fromString(storedUuid);
        if (uuid == null) return;

        externalMuteService.invalidate(uuid);
    }

}
