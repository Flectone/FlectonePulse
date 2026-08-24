package net.flectone.pulse.module.integration.libertybans;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.ExternalMuteService;
import net.flectone.pulse.service.FPlayerService;
import space.arim.libertybans.api.*;
import space.arim.libertybans.api.event.PostPardonEvent;
import space.arim.libertybans.api.event.PostPunishEvent;
import space.arim.libertybans.api.punish.Punishment;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;
import space.arim.omnibus.events.ListenerPriorities;
import space.arim.omnibus.events.RegisteredListener;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitLibertyBansIntegration implements FIntegration {

    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final ExternalMuteService externalMuteService;
    @Getter private final FLogger fLogger;

    private LibertyBans libertyBans;
    private Omnibus omnibus;
    private RegisteredListener punishListener;
    private RegisteredListener pardonListener;

    @Override
    public String getIntegrationName() {
        return "LibertyBans";
    }

    @Override
    public void hook() {
        try {
            omnibus = OmnibusProvider.getOmnibus();

            Optional<LibertyBans> optionalLibertyBans = omnibus.getRegistry().getProvider(LibertyBans.class);
            if (optionalLibertyBans.isEmpty()) return;

            libertyBans = optionalLibertyBans.get();

            punishListener = omnibus.getEventBus().registerListener(PostPunishEvent.class, ListenerPriorities.NORMAL, event -> {
                if (event.getPunishment().getType() != PunishmentType.MUTE) return;

                extractPlayerUuid(event.getPunishment().getVictim()).ifPresent(externalMuteService::invalidate);
            });

            pardonListener = omnibus.getEventBus().registerListener(PostPardonEvent.class, ListenerPriorities.NORMAL, event -> {
                if (event.getPunishment().getType() != PunishmentType.MUTE) return;

                extractPlayerUuid(event.getPunishment().getVictim()).ifPresent(externalMuteService::invalidate);
            });

            logHook();
        } catch (Exception e) {
            lohHookFailed(e);
        }
    }

    @Override
    public void unhook() {
        try {
            if (omnibus != null) {
                if (punishListener != null) {
                    omnibus.getEventBus().unregisterListener(punishListener);
                }

                if (pardonListener != null) {
                    omnibus.getEventBus().unregisterListener(pardonListener);
                }
            }
        } catch (Exception _) {
            // ignore
        }

        libertyBans = null;
        omnibus = null;
        punishListener = null;
        pardonListener = null;

        logUnhook();
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

            return taskScheduler.await(
                    libertyBans.getSelector()
                            .selectionByApplicabilityBuilder(uuid, ip)
                            .type(PunishmentType.MUTE)
                            .build()
                            .getFirstSpecificPunishment()
                            .toCompletableFuture(),
                    Optional.empty(),
                    "Mute of " + fPlayer.name() + " in LibertyBans"
            );
        } catch (UnknownHostException _) {
            return Optional.empty();
        }
    }

    private Optional<UUID> extractPlayerUuid(Victim victim) {
        return switch (victim) {
            case PlayerVictim playerVictim -> Optional.of(playerVictim.getUUID());
            case CompositeVictim compositeVictim -> Optional.of(compositeVictim.getUUID());
            default -> Optional.empty();
        };
    }

}