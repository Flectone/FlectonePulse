package net.flectone.pulse.module.integration.libertybans;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import space.arim.libertybans.api.punish.Punishment;
import space.arim.libertybans.api.LibertyBans;
import space.arim.libertybans.api.PunishmentType;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LibertyBansIntegration implements FIntegration {

    private final LibertyBans libertyBans;
    private final FLogger fLogger;

    @Getter
    private boolean hooked;

    @Override
    public void hook() {
        hooked = true;
        fLogger.info("✔ LibertyBans hooked");
    }

    @Override
    public void unhook() {
        hooked = false;
        fLogger.info("✖ LibertyBans unhooked");
    }
    public Optional<Punishment> muteSearching(FEntity fEntity) {
        UUID uuid = getUUID(fEntity);

        return libertyBans.getSelector()
                .selectionByApplicabilityBuilder(uuid, (InetAddress) null)
                .type(PunishmentType.MUTE)
                .build()
                .getFirstSpecificPunishment()
                .toCompletableFuture().join();
    }

    public boolean isMuted(FEntity fEntity) {
        Optional<Punishment> muteOpt = muteSearching(fEntity);

        return muteOpt.isPresent() &&
                (muteOpt.get().getEndDate() == null || muteOpt.get().getEndDate().isAfter(Instant.now()));
    }

    public ExternalModeration getMute(FEntity fEntity) {
        Optional<Punishment> muteOpt = muteSearching(fEntity);

        return muteOpt.map(punishment -> new ExternalModeration(
                fEntity.getName(),
                punishment.getOperator().toString(),
                punishment.getReason(),
                punishment.getIdentifier(),
                punishment.getStartDateSeconds(),
                punishment.getEndDateSeconds(),
                !punishment.isPermanent()
        )).orElse(null);
    }

    private UUID getUUID(FEntity fEntity) {
        return fEntity.getUuid();
    }
}
