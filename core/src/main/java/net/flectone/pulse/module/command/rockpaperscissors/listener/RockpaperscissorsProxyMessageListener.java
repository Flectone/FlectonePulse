package net.flectone.pulse.module.command.rockpaperscissors.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.module.command.rockpaperscissors.RockpaperscissorsModule;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RockpaperscissorsProxyMessageListener implements PulseListener {

    private final RockpaperscissorsModule rockpaperscissorsModule;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_ROCKPAPERSCISSORS) return event;

        try (ProxyPayload proxyPayload = new ProxyPayload(event.payload())) {
            RockpaperscissorsModule.GamePhase gamePhase = RockpaperscissorsModule.GamePhase.valueOf(proxyPayload.readString());
            switch (gamePhase) {
                case CREATE -> {
                    UUID id = UUID.fromString(proxyPayload.readString());
                    UUID receiver = UUID.fromString(proxyPayload.readString());
                    rockpaperscissorsModule.create(id, event.sender(), receiver);
                }
                case MOVE -> {
                    UUID id = UUID.fromString(proxyPayload.readString());
                    String move = proxyPayload.readString();

                    rockpaperscissorsModule.move(id, event.sender(), move, event.uuid());
                }
                case END -> {
                    if (!(event.sender() instanceof FPlayer fPlayer)) return event.withProcessed(true);

                    UUID id = UUID.fromString(proxyPayload.readString());
                    String move = proxyPayload.readString();

                    rockpaperscissorsModule.end(id, fPlayer, move, event.uuid());
                }
            }
        }

        return event.withProcessed(true);
    }

}
