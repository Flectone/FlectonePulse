package net.flectone.pulse.module.command.tictactoe.model;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.model.entity.FPlayer;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.type.tuple.Pair;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Getter
public class TicTacToe {

    private static final int FIRST_VALUE = 1;
    private static final int SECOND_VALUE = 2;
    private static final int REMOVE_MULTIPLIER = -1;
    private static final int WIN_OFFSET = 5;

    private final Map<Integer, Queue<String>> movesMap = new Int2ObjectArrayMap<>();

    private final int id;
    private final boolean hard;
    private final int[][] field = new int[3][3];

    private final int firstPlayer;
    private final int secondPlayer;

    @Setter private int nextPlayer;
    private int[] winningTrio = null;

    @Setter private boolean ended;
    private boolean created;

    public TicTacToe(int id, int firstPlayer, int secondPlayer, boolean hard) {
        this.id = id;
        this.hard = hard;

        movesMap.put(firstPlayer, new LinkedList<>());
        this.firstPlayer = firstPlayer;

        movesMap.put(secondPlayer, new LinkedList<>());
        this.secondPlayer = secondPlayer;

        nextPlayer = secondPlayer;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder()
                .append(id)
                .append(",")
                .append(nextPlayer)
                .append(",");

        for (int[] row : field) {
            for (int column : row) {
                stringBuilder.append(column).append(";");
            }
            stringBuilder.append(",");
        }

        stringBuilder.append(ended ? 1 : 0);

        return stringBuilder.toString();
    }

    public String build(String formatField,
                        String first,
                        String firstRemove,
                        String firstWin,
                        String second,
                        String secondRemove,
                        String secondWin,
                        String empty) {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                formatField = RegExUtils.replaceFirst(formatField, "\\[#]",
                        switch (field[i][j]) {
                            case FIRST_VALUE -> first;
                            case FIRST_VALUE * REMOVE_MULTIPLIER -> firstRemove;
                            case FIRST_VALUE + WIN_OFFSET -> firstWin;
                            case SECOND_VALUE -> second;
                            case SECOND_VALUE * REMOVE_MULTIPLIER -> secondRemove;
                            case SECOND_VALUE + WIN_OFFSET -> secondWin;
                            default -> Strings.CS.replace(empty, "<move>", i + "-" + j);
                        }
                );
            }
        }

        return formatField;
    }

    public boolean isWin() {
        return winningTrio != null;
    }

    public void checkWinningTrio() {
        for (int row = 0; row < 3; row++) {
            if (field[row][0] != 0 && Math.abs(field[row][0]) == Math.abs(field[row][1]) && Math.abs(field[row][1]) == Math.abs(field[row][2])) {
                winningTrio = new int[]{row, 0, row, 1, row, 2};
                break;
            }
        }

        for (int column = 0; column < 3; column++) {
            if (field[0][column] != 0 && Math.abs(field[0][column]) == Math.abs(field[1][column]) && Math.abs(field[1][column]) == Math.abs(field[2][column])) {
                winningTrio = new int[]{0, column, 1, column, 2, column};
                break;
            }
        }

        if (field[0][0] != 0 && Math.abs(field[0][0]) == Math.abs(field[1][1]) && Math.abs(field[1][1]) == Math.abs(field[2][2])) {
            winningTrio =  new int[]{0, 0, 1, 1, 2, 2};
        }

        if (field[0][2] != 0 && Math.abs(field[0][2]) == Math.abs(field[1][1]) && Math.abs(field[1][1]) == Math.abs(field[2][0])) {
            winningTrio = new int[]{0, 2, 1, 1, 2, 0};
        }

        if (winningTrio != null) {
            for (int i = 1; i < winningTrio.length; i = i + 2) {
                int row = winningTrio[i-1];
                int column = winningTrio[i];
                field[row][column] = Math.abs(field[row][column]) + WIN_OFFSET;
            }
        }
    }

    public boolean isDraw() {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                if (field[row][column] == 0) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean contains(FPlayer fPlayer) {
        return movesMap.containsKey(fPlayer.id());
    }

    public void setNextPlayer() {
        nextPlayer = nextPlayer == firstPlayer ? secondPlayer : firstPlayer;
    }

    public boolean move(FPlayer fPlayer, String move) {
        if (nextPlayer != fPlayer.id()) {
            return false;
        }

        Queue<String> moves = movesMap.get(fPlayer.id());
        if (movesMap.values().stream().allMatch(Collection::isEmpty) && (move == null || move.equals("create"))) {
            setNextPlayer();
            created = true;
            return true;
        }

        Pair<Integer, Integer> rowColumn = parseMove(move);
        if (rowColumn == null) return false;

        int row = rowColumn.first();
        int column = rowColumn.second();

        if (field[row][column] != 0) {
            return false;
        }

        moves.add(move);
        movesMap.put(fPlayer.id(), moves);

        int currentPlayerValue = firstPlayer == fPlayer.id()
                ? FIRST_VALUE
                : SECOND_VALUE;

        field[row][column] = currentPlayerValue;

        if (isHard() && moves.size() > 2) {
            removeMove(moves, currentPlayerValue);
        }

        checkWinningTrio();
        setNextPlayer();

        return true;
    }

    private void removeMove(Queue<String> moves, int currentPlayerValue) {
        if (moves.size() > 3) {
            String move = moves.poll();

            Pair<Integer, Integer> rowColumn = parseMove(move);
            if (rowColumn == null) return;

            field[rowColumn.first()][rowColumn.second()] = 0;
        }

        String nextRemove = moves.peek();
        if (nextRemove == null) return;

        Pair<Integer, Integer> rowColumn = parseMove(nextRemove);
        if (rowColumn == null) return;

        field[rowColumn.first()][rowColumn.second()] = REMOVE_MULTIPLIER * currentPlayerValue;
    }

    private @Nullable Pair<Integer, Integer> parseMove(String move) {
        try {
            String[] stringMove = move.split("-");

            return Pair.of(Integer.parseInt(stringMove[0]), Integer.parseInt(stringMove[1]));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
