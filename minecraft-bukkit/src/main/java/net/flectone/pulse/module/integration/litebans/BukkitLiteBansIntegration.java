package net.flectone.pulse.module.integration.litebans;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import litebans.api.Database;
import litebans.api.Entry;
import litebans.api.Events;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.service.ExternalModerationService;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitLiteBansIntegration implements FIntegration {

    private final ExternalModerationService externalModerationService;
    @Getter private final FLogger fLogger;

    @Getter private boolean hooked;

    private Events.Listener listener;

    @Override
    public String getIntegrationName() {
        return "LiteBans";
    }

    @Override
    public void hook() {
        hooked = true;

        listener = new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                invalidateIfMute(entry);
            }

            @Override
            public void entryRemoved(Entry entry) {
                invalidateIfMute(entry);
            }
        };

        try {
            Events.get().register(listener);
        } catch (Exception e) {
            listener = null;
            fLogger.warning("Failed to listen for LiteBans punishment events", e);
        }

        logHook();
    }

    @Override
    public void unhook() {
        if (listener != null) {
            try {
                Events.get().unregister(listener);
            } catch (Exception _) {
                // ignore
            }

            listener = null;
        }

        hooked = false;
        logUnhook();
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

    private void invalidateIfMute(Entry entry) {
        if (!"mute".equalsIgnoreCase(entry.getType())) return;
        if (entry.getUuid() == null) return;

        try {
            externalModerationService.invalidate(UUID.fromString(entry.getUuid()), Moderation.Type.MUTE);
        } catch (IllegalArgumentException _) {
            // ignore
        }
    }

}
