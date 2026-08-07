package net.flectone.pulse.module.command.tictactoe.model;

import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Pair;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A game of tic-tac-toe, including the moves played and who is to move next.
 * @author TheFaser
 */
@Getter
public class TicTacToe {

    /**
     * The board value marking the first player's move.
     */
    public static final int FIRST_VALUE = 1;
    /**
     * The board value marking the second player's move.
     */
    public static final int SECOND_VALUE = 2;
    /**
     * Applied to a board value to mark the move that hard mode is about to drop.
     */
    public static final int REMOVE_MULTIPLIER = -1;
    /**
     * Added to a board value to mark a square that is part of the winning line.
     */
    public static final int WIN_OFFSET = 5;

    private final LinkedHashMap<Integer, Queue<String>> movesMap = new LinkedHashMap<>();

    private final int id;
    private final boolean hard;
    private final int[][] field = new int[3][3];

    private final int firstPlayer;
    private final int secondPlayer;

    @Setter private int nextPlayer;
    private int[] winningTrio = null;

    @Setter private boolean ended;
    private boolean created;

    /**
     * Creates a game.
     *
     * @param id the game id
     * @param firstPlayer the player who moves first
     * @param secondPlayer the other player
     * @param hard whether the older move is removed once a player has three on the board
     */
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

    /**
     * Whether the last move completed a line.
     *
     * @return true if the game is won
     */
    public boolean isWin() {
        return winningTrio != null;
    }

    /**
     * Looks for a completed line and records it, so it can be highlighted on the board.
     */
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
            winningTrio = new int[]{0, 0, 1, 1, 2, 2};
        }

        if (field[0][2] != 0 && Math.abs(field[0][2]) == Math.abs(field[1][1]) && Math.abs(field[1][1]) == Math.abs(field[2][0])) {
            winningTrio = new int[]{0, 2, 1, 1, 2, 0};
        }

        if (winningTrio != null) {
            for (int i = 1; i < winningTrio.length; i = i + 2) {
                int row = winningTrio[i - 1];
                int column = winningTrio[i];
                field[row][column] = Math.abs(field[row][column]) + WIN_OFFSET;
            }
        }
    }

    /**
     * Whether the board is full with no line completed.
     *
     * @return true if the game is drawn
     */
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

    /**
     * Whether a player is in this game.
     *
     * @param fPlayer the player
     * @return true if they are one of the two
     */
    public boolean contains(FPlayer fPlayer) {
        return movesMap.containsKey(fPlayer.id());
    }

    /**
     * Passes the turn to the other player.
     */
    public void setNextPlayer() {
        nextPlayer = nextPlayer == firstPlayer ? secondPlayer : firstPlayer;
    }

    /**
     * Plays a square, and in hard mode drops that player's oldest move.
     *
     * @param fPlayer the player moving
     * @param move the square, written as its row and column
     * @return true if the move was legal
     */
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

        int row = rowColumn.getLeft();
        int column = rowColumn.getRight();

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

            field[rowColumn.getLeft()][rowColumn.getRight()] = 0;
        }

        String nextRemove = moves.peek();
        if (nextRemove == null) return;

        Pair<Integer, Integer> rowColumn = parseMove(nextRemove);
        if (rowColumn == null) return;

        field[rowColumn.getLeft()][rowColumn.getRight()] = REMOVE_MULTIPLIER * currentPlayerValue;
    }

    private @Nullable Pair<Integer, Integer> parseMove(String move) {
        try {
            String[] stringMove = move.split("-");

            return Pair.of(Integer.parseInt(stringMove[0]), Integer.parseInt(stringMove[1]));
        } catch (NumberFormatException _) {
            return null;
        }
    }
}
