package net.flectone.pulse.listener.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.dispatcher.EventDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FEntityImpl;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.entity.FPlayerImpl;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.PlayerSwitchService;
import net.flectone.pulse.util.payload.ProxyPayload;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProxyMessageProcessorImpl implements ProxyMessageProcessor {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlayerSwitchService playerSwitchService;
    private final FLogger fLogger;
    private final Gson gson;
    private final TaskScheduler taskScheduler;
    private final EventDispatcher eventDispatcher;
    private final PlatformServerAdapter platformServerAdapter;

    @Override
    public void process(byte[] bytes) {
        taskScheduler.runAsync(() -> {
            try (ProxyPayload proxyPayload = new ProxyPayload(bytes)) {
                ModuleName name = ModuleName.fromProxyString(proxyPayload.readString());
                if (name == null) return;

                UUID uuid = UUID.fromString(proxyPayload.readString());

                switch (name) {
                    case PLAYER_SWITCHING -> playerSwitchService.setSwitching(uuid, proxyPayload.readBoolean());
                    case PLAYER_CONNECTED, PLAYER_DISCONNECTED -> processPlayerState(name, uuid, proxyPayload);
                    default -> processMessage(name, uuid, proxyPayload);
                }
            } catch (Exception exception) {
                fLogger.warning(exception);
            }
        });
    }

    private void processPlayerState(ModuleName name, UUID uuid, ProxyPayload proxyPayload) throws IOException {
        boolean connected = name == ModuleName.PLAYER_CONNECTED;

        // a switch is only finished when the player actually leaves the network
        if (!connected) {
            playerSwitchService.setSwitching(uuid, false);
        }

        // if player is unknown, it means he is not in database
        // message may have been sent from a server where FlectonePulse is not installed
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

        if (fPlayer.isUnknown()) {
            fPlayer = fPlayerService.getFPlayerFromDatabase(uuid);
        }

        if (fPlayer.isUnknown()) {
            if (connected) return;

            fPlayer = FPlayer.builder()
                    .uuid(uuid)
                    .name(FEntity.UNKNOWN_NAME)
                    .build();
        }

        eventDispatcher.dispatch(new ProxyMessageEvent(proxyPayload.readBoolean(), "", name, fPlayer, uuid, proxyPayload.readAllBytes()));
    }

    private void processMessage(ModuleName name, UUID uuid, ProxyPayload proxyPayload) throws IOException {
        String server = proxyPayload.readString();

        // this parameter is always different from config value, because it is taken relative to worlds.
        // this is to prevent user from creating two identical server
        boolean sentByThisServer = platformServerAdapter.getServerUUID().equals(proxyPayload.readString());

        Set<String> proxyClusters = gson.fromJson(proxyPayload.readString(), new TypeToken<Set<String>>() {}.getType());
        Set<String> configClusters = fileFacade.config().proxy().clusters();
        if (!configClusters.isEmpty() && configClusters.stream().noneMatch(proxyClusters::contains) && !configClusters.contains(server)) {
            return;
        }

        Optional<FEntity> optionalFEntity = parseFEntity(gson.fromJson(proxyPayload.readString(), JsonObject.class));
        if (optionalFEntity.isEmpty()) return;

        eventDispatcher.dispatch(new ProxyMessageEvent(sentByThisServer, server, name, optionalFEntity.get(), uuid, proxyPayload.readAllBytes()));
    }

    public Optional<FEntity> parseFEntity(JsonObject jsonObject) {
        if (jsonObject.has("name") && jsonObject.has("uuid") && jsonObject.has("type")) {
            boolean isPlayer = jsonObject.has("id");
            return Optional.of(gson.fromJson(jsonObject, isPlayer ? FPlayerImpl.class : FEntityImpl.class));
        }

        return Optional.empty();
    }

}
