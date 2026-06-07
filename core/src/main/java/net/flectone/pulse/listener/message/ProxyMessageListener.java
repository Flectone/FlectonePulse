package net.flectone.pulse.listener.message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.repository.CooldownRepository;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.command.whitelist.WhitelistModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.io.ProxyPayload;
import net.flectone.pulse.util.logging.FLogger;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProxyMessageListener {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlaytimeService playtimeService;
    private final SocialService socialService;
    private final FLogger fLogger;
    private final ModerationService moderationService;
    private final Gson gson;
    private final TaskScheduler taskScheduler;
    private final CooldownRepository cooldownRepository;
    private final EventDispatcher eventDispatcher;

    private final QuitModule quitModule;
    private final JoinModule joinModule;
    private final BanModule banModule;
    private final MuteModule muteModule;
    private final MaintenanceModule maintenanceModule;
    private final WarnModule warnModule;
    private final WhitelistModule whitelistModule;
    private final KickModule kickModule;

    public void handleProxyMessage(byte[] bytes) {
        taskScheduler.runAsync(() -> {
            try (ProxyPayload proxyPayload = new ProxyPayload(bytes)) {

                ModuleName name = ModuleName.fromProxyString(proxyPayload.readString());
                if (name == null) return;

                UUID uuid = UUID.fromString(proxyPayload.readString());

                switch (name) {
                    case SYSTEM_ONLINE -> handleSystemOnline(uuid);
                    case SYSTEM_CONNECTED -> handleSystemConnected(uuid);
                    case SYSTEM_OFFLINE -> handleSystemOffline(uuid, proxyPayload.readBoolean());
                    default -> {
                        Set<String> proxyClusters = gson.fromJson(proxyPayload.readString(), new TypeToken<Set<String>>() {}.getType());

                        Optional<FEntity> optionalFEntity = proxyPayload.parseFEntity(gson, gson.fromJson(proxyPayload.readString(), JsonObject.class));
                        if (optionalFEntity.isEmpty()) return;

                        FEntity fEntity = optionalFEntity.get();
                        if (handleInvalidateCache(proxyPayload, name, fEntity)) {
                            return;
                        }

                        Set<String> configClusters = fileFacade.config().proxy().clusters();
                        if (!configClusters.isEmpty() && configClusters.stream().noneMatch(proxyClusters::contains)) {
                            return;
                        }

                        byte[] payload = proxyPayload.readAllBytes();
                        ProxyMessageEvent proxyMessageEvent = eventDispatcher.dispatch(new ProxyMessageEvent(name, fEntity, uuid, payload));
                        if (!proxyMessageEvent.processed()) {
                            fLogger.warning("Proxy message '%s' with UUID '%s' sent by '%s' was not processed", name, uuid.toString(), fEntity.name());
                        }
                    }
                }

            } catch (Exception e) {
                fLogger.warning(e);
            }
        });
    }

    public void handleSystemOnline(UUID uuid) {
        // delay is needed for proper sync Proxy
        taskScheduler.runAsyncLater(() -> {
            fPlayerService.invalidateOfflineCache(uuid, true);
            playtimeService.invalidate(uuid);
            socialService.invalidate(uuid);
        }, 5L);
    }

    public void handleSystemConnected(UUID uuid) {
        joinModule.proxySend(uuid);
    }

    public void handleSystemOffline(UUID uuid, boolean connected) throws IOException {
        if (connected) {
            taskScheduler.runAsyncLater(() -> {
                fPlayerService.invalidateOnlineCache(uuid);
                playtimeService.invalidate(uuid);
                socialService.invalidate(uuid);
            }, 5L);

            quitModule.proxySend(uuid);
        }
    }

    public void handleSystemSkin(UUID uuid) {
        // nothing
    }

    private boolean handleInvalidateCache(ProxyPayload proxyPayload, ModuleName tag, FEntity fEntity) throws IOException {
        return switch (tag) {
            case SYSTEM_COOLDOWN -> {
                UUID uuid = UUID.fromString(proxyPayload.readString());
                String cooldownClass = proxyPayload.readString();
                long newExpireTime = proxyPayload.readLong();

                cooldownRepository.updateCache(uuid, cooldownClass, newExpireTime);
                yield true;
            }
            case SYSTEM_VIOLATION -> {
                ModerationService.ViolationKey violationKey = gson.fromJson(proxyPayload.readString(), ModerationService.ViolationKey.class);
                long violationValue = proxyPayload.readLong();

                moderationService.addViolation(violationKey, violationValue);
                yield true;
            }
            case SYSTEM_BAN -> {
                if (!banModule.config().filterByServer()) {
                    moderationService.invalidate(fEntity.uuid(), Moderation.Type.BAN);
                    moderationService.invalidate(fEntity.uuid(), Moderation.Type.UNBAN);

                    Moderation moderation = gson.fromJson(proxyPayload.readString(), Moderation.class);
                    if (moderation.type() == Moderation.Type.BAN) {
                        // give some time
                        taskScheduler.runAsyncLater(() -> banModule.kick(moderation));
                    }
                }
                yield true;
            }
            case SYSTEM_MUTE -> {
                if (!muteModule.config().filterByServer()) {
                    moderationService.invalidate(fEntity.uuid(), Moderation.Type.MUTE);
                    moderationService.invalidate(fEntity.uuid(), Moderation.Type.UNMUTE);
                }
                yield true;
            }
            case SYSTEM_MAINTENANCE -> {
                if (!maintenanceModule.config().filterByServer()) {
                    moderationService.invalidate(fEntity.uuid(), Moderation.Type.MAINTENANCE);
                    moderationService.invalidate(fEntity.uuid(), Moderation.Type.UNMAINTENANCE);

                    Moderation moderation = gson.fromJson(proxyPayload.readString(), Moderation.class);
                    if (moderation.type() == Moderation.Type.MAINTENANCE) {
                        // give some time
                        taskScheduler.runAsyncLater(() -> maintenanceModule.kickOnlinePlayers(moderation));
                    }
                }
                yield true;
            }
            case SYSTEM_WARN -> {
                if (!warnModule.config().filterByServer()) {
                    moderationService.invalidate(fEntity.uuid(), Moderation.Type.WARN);
                    moderationService.invalidate(fEntity.uuid(), Moderation.Type.UNWARN);

                    Moderation moderation = gson.fromJson(proxyPayload.readString(), Moderation.class);
                    if (moderation.type() == Moderation.Type.WARN) {
                        taskScheduler.runAsyncLater(() ->  warnModule.sendForTarget(moderation));
                    }
                }
                yield true;
            }
            case SYSTEM_WHITELIST -> {
                if (!whitelistModule.config().filterByServer()) {
                    moderationService.invalidate(fEntity.uuid(), Moderation.Type.WHITELIST);
                    moderationService.invalidate(fEntity.uuid(), Moderation.Type.UNWHITELIST);

                    Moderation moderation = gson.fromJson(proxyPayload.readString(), Moderation.class);
                    FPlayer fTarget = fPlayerService.getFPlayer(moderation.player());

                    if (fTarget.isConsole()) {
                        if (moderation.type() == Moderation.Type.WHITELIST) {
                            taskScheduler.runAsyncLater(() -> whitelistModule.kickOnlinePlayers(moderation));
                        }

                    } else if (moderation.type() == Moderation.Type.UNWHITELIST && whitelistModule.isTurnedOn()) {
                        FPlayer fModerator = fPlayerService.getFPlayer(moderation.moderator());
                        taskScheduler.runAsyncLater(() -> whitelistModule.kickPlayer(fModerator, fTarget));
                    }

                }
                yield true;
            }
            case SYSTEM_SKIN -> {
                handleSystemSkin(fEntity.uuid());
                yield true;
            }
            case SYSTEM_KICK -> {
                if (!kickModule.config().filterByServer()) {
                    Moderation moderation = gson.fromJson(proxyPayload.readString(), Moderation.class);

                    // give some time
                    taskScheduler.runAsyncLater(() -> kickModule.kick(moderation));
                }
                yield true;
            }
            case SYSTEM_COLOR -> {
                if (fEntity instanceof FPlayer fPlayer) {
                    socialService.loadColors(fPlayer, false);
                }
                yield true;
            }
            case SYSTEM_SETTING -> {
                if (fEntity instanceof FPlayer fPlayer) {
                    socialService.loadSettings(fPlayer, false);
                }
                yield true;
            }
            case SYSTEM_IGNORE -> {
                if (fEntity instanceof FPlayer fPlayer) {
                    socialService.loadIgnores(fPlayer, false);
                }
                yield true;
            }
            default -> false;
        };
    }

}
