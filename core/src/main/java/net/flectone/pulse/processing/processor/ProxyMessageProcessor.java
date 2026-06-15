package net.flectone.pulse.processing.processor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.io.ProxyPayload;
import net.flectone.pulse.util.logging.FLogger;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProxyMessageProcessor {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final FLogger fLogger;
    private final Gson gson;
    private final TaskScheduler taskScheduler;
    private final EventDispatcher eventDispatcher;
    private final PlatformServerAdapter platformServerAdapter;

    public void process(byte[] bytes) {
        taskScheduler.runAsync(() -> {
            try (ProxyPayload proxyPayload = new ProxyPayload(bytes)) {

                ModuleName name = ModuleName.fromProxyString(proxyPayload.readString());
                if (name == null) return;

                UUID uuid = UUID.fromString(proxyPayload.readString());
                if (name == ModuleName.PLAYER_CONNECTED || name == ModuleName.PLAYER_DISCONNECTED) {
                    ProxyMessageEvent proxyMessageEvent = eventDispatcher.dispatch(new ProxyMessageEvent(proxyPayload.readBoolean(), "", name, fPlayerService.getFPlayer(uuid), uuid, proxyPayload.readAllBytes()));
                    if (proxyMessageEvent.cancelled()) {
                        // nothing
                    }
                    return;
                }

                String server = proxyPayload.readString();

                // this parameter is always different from config value, because it is taken relative to worlds.
                // this is to prevent user from creating two identical server
                boolean sentByThisServer = platformServerAdapter.getServerUUID().equals(proxyPayload.readString());

                Set<String> proxyClusters = gson.fromJson(proxyPayload.readString(), new TypeToken<Set<String>>() {}.getType());
                Set<String> configClusters = fileFacade.config().proxy().clusters();
                if (!configClusters.isEmpty() && configClusters.stream().noneMatch(proxyClusters::contains) && !configClusters.contains(server)) {
                    return;
                }

                Optional<FEntity> optionalFEntity = proxyPayload.parseFEntity(gson, gson.fromJson(proxyPayload.readString(), JsonObject.class));
                if (optionalFEntity.isEmpty()) return;

                byte[] payload = proxyPayload.readAllBytes();
                FEntity fEntity = optionalFEntity.get();

                ProxyMessageEvent proxyMessageEvent = eventDispatcher.dispatch(new ProxyMessageEvent(sentByThisServer, server, name, fEntity, uuid, payload));
                if (proxyMessageEvent.cancelled()) {
                    // nothing
                }
            } catch (Exception e) {
                fLogger.warning(e);
            }
        });
    }

}
