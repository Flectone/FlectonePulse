package net.flectone.pulse.module.command.tictactoe.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.tictactoe.model.TicTacToe;
import net.flectone.pulse.util.RandomUtil;

import java.util.HashMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TictactoeService {

    private final Map<Integer, TicTacToe> ticTacToeMap = new HashMap<>();

    private final RandomUtil randomUtil;

    public TicTacToe create(int id, FPlayer fPlayer, FPlayer fReceiver, boolean hard) {
        TicTacToe ticTacToe = new TicTacToe(id, fPlayer.id(), fReceiver.id(), hard);
        ticTacToeMap.put(ticTacToe.getId(), ticTacToe);
        return ticTacToe;
    }

    public TicTacToe create(FPlayer fPlayer, FPlayer fReceiver, boolean hard) {
        return create(randomUtil.nextInt(Integer.MAX_VALUE), fPlayer, fReceiver, hard);
    }

    public void put(TicTacToe ticTacToe) {
        ticTacToeMap.put(ticTacToe.getId(), ticTacToe);
    }

    public TicTacToe get(int id) {
        return ticTacToeMap.get(id);
    }

    public TicTacToe fromString(String string) {
        String[] values = string.split(",");
        if (values.length < 5) return null;
        try {
            int id = Integer.parseInt(values[0]);

            TicTacToe ticTacToe = ticTacToeMap.get(id);
            if (ticTacToe == null) return null;

            int nextPlayer = Integer.parseInt(values[1]);
            ticTacToe.setNextPlayer(nextPlayer);
            ticTacToe.setEnded(string.endsWith("1"));

            for (int i = 2; i < 5; i++) {
                String[] column = values[i].split(";");
                if (column.length != 3) return null;

                ticTacToe.getField()[i-2][0] = Integer.parseInt(column[0]);
                ticTacToe.getField()[i-2][1] = Integer.parseInt(column[1]);
                ticTacToe.getField()[i-2][2] = Integer.parseInt(column[2]);
            }

            return ticTacToe;

        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public void clear() {
        ticTacToeMap.clear();
    }
}
