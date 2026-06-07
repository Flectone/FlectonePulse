package net.flectone.pulse.module.command.tictactoe.listener;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.module.command.tictactoe.TictactoeModule;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToe;
import net.flectone.pulse.module.command.tictactoe.service.TictactoeService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TictactoeProxyMessageListener implements PulseListener {

    private final TictactoeModule tictactoeModule;
    private final TictactoeService tictactoeService;
    private final Gson gson;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_TICTACTOE) return event;
        if (!(event.sender() instanceof FPlayer fPlayer)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            TictactoeModule.GamePhase gamePhase = TictactoeModule.GamePhase.valueOf(proxyPayload.readString());
            switch (gamePhase) {
                case CREATE -> {
                    FPlayer fReceiver = gson.fromJson(proxyPayload.readString(), FPlayer.FPlayerImpl.class);
                    int ticTacToeId = proxyPayload.readInt();
                    boolean isHard = proxyPayload.readBoolean();

                    TicTacToe ticTacToe = tictactoeService.get(ticTacToeId);
                    if (tictactoeService.get(ticTacToeId) == null) {
                        ticTacToe = tictactoeService.create(ticTacToeId, fPlayer, fReceiver, isHard);
                    }

                    tictactoeModule.sendCreateMessage(fPlayer, fReceiver, ticTacToe, event.uuid());
                }
                case MOVE -> {
                    FPlayer fReceiver = gson.fromJson(proxyPayload.readString(), FPlayer.FPlayerImpl.class);
                    TicTacToe ticTacToe = tictactoeService.fromString(proxyPayload.readString());
                    int typeTitle = proxyPayload.readInt();
                    String move = proxyPayload.readString();

                    tictactoeModule.sendMoveMessage(fPlayer, fReceiver, ticTacToe, typeTitle, move, event.uuid());
                }
            }
        }

        return event.withProcessed(true);
    }

}
