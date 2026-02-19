package net.flectone.pulse.module.integration.libertybans;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;
import space.arim.libertybans.api.LibertyBans;
import space.arim.libertybans.api.PlayerOperator;
import space.arim.libertybans.api.PunishmentType;
import space.arim.libertybans.api.punish.Punishment;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LibertyBansIntegration implements FIntegration {

    private final FPlayerService fPlayerService;
    @Getter private final FLogger fLogger;

    private LibertyBans libertyBans;

    @Override
    public String getIntegrationName() {
        return "LibertyBans";
    }

    @Override
    public void hook() {
        Omnibus omnibus = OmnibusProvider.getOmnibus();
        Optional<LibertyBans> optionalLibertyBans = omnibus.getRegistry().getProvider(LibertyBans.class);
        if (optionalLibertyBans.isEmpty()) return;

        libertyBans = optionalLibertyBans.get();

        logHook();
    }

    public boolean isHooked() {
        return libertyBans != null;
    }

    public boolean isMuted(FEntity fEntity) {
        return selectMute(fEntity).isPresent();
    }

    public ExternalModeration getMute(FEntity fEntity) {
        Optional<Punishment> optionalPunishment = selectMute(fEntity);
        if (optionalPunishment.isEmpty()) return null;

        Punishment punishment = optionalPunishment.get();

        FPlayer operator = punishment.getOperator() instanceof PlayerOperator playerOperator
                ? fPlayerService.getFPlayer(playerOperator.getUUID())
                : fPlayerService.getConsole();

        return new ExternalModeration(
                fEntity.name(),
                operator.name(),
                punishment.getReason(),
                punishment.getIdentifier(),
                punishment.getStartDate().toEpochMilli(),
                punishment.isPermanent() ? -1 : punishment.getEndDate().toEpochMilli(),
                punishment.isPermanent()
        );
    }

    private Optional<Punishment> selectMute(FEntity fEntity) {
        if (!(fEntity instanceof FPlayer fPlayer) || !isHooked()) return Optional.empty();

        try {
            UUID uuid = fPlayer.uuid();
            InetAddress ip = InetAddress.getByName(fPlayer.ip());

            return libertyBans.getSelector()
                    .selectionByApplicabilityBuilder(uuid, ip)
                    .type(PunishmentType.MUTE)
                    .build()
                    .getFirstSpecificPunishment()
                    .toCompletableFuture()
                    .join();
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }
}