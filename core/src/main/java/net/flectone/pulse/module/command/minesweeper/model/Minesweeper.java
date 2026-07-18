package net.flectone.pulse.module.command.minesweeper.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

@Getter
public class Minesweeper {

    private static final int[] NEIGHBOR_ROW_OFFSETS = {-1, -1, -1, 0, 0, 1, 1, 1};
    private static final int[] NEIGHBOR_COLUMN_OFFSETS = {-1, 0, 1, -1, 1, -1, 0, 1};

    private final int rows;
    private final int columns;
    private final int mineCount;
    private final int seed;
    private final int totalSafeCellCount;
    private final Random random;

    private final boolean[][] mine;
    private final int[][] adjacentMineCount;
    private final boolean[][] revealed;
    private final boolean[][] flagged;

    @Setter
    private boolean flagMode = false;

    private boolean minesPlaced = false;
    private GameState state = GameState.IN_PROGRESS;
    private int revealedSafeCellCount = 0;
    private int flaggedCellCount = 0;

    public Minesweeper(int rows, int columns, int mineCount, int seed) {
        this.rows = rows;
        this.columns = columns;
        this.mineCount = mineCount;
        this.seed = seed;
        this.totalSafeCellCount = rows * columns - mineCount;
        this.random = new Random(seed);

        this.mine = new boolean[rows][columns];
        this.adjacentMineCount = new int[rows][columns];
        this.revealed = new boolean[rows][columns];
        this.flagged = new boolean[rows][columns];
    }

    public RevealResult reveal(int row, int column) {
        if (!minesPlaced) {
            placeMinesAvoiding(row, column);
            minesPlaced = true;
        }

        if (flagged[row][column] || revealed[row][column]) {
            return new RevealResult(state, 0, false);
        }

        if (mine[row][column]) {
            revealed[row][column] = true;
            state = GameState.LOSE;
            return new RevealResult(state, 1, true);
        }

        int openedCellCount = floodFill(row, column);

        if (revealedSafeCellCount == totalSafeCellCount) {
            state = GameState.WIN;
        }

        return new RevealResult(state, openedCellCount, false);
    }

    public void revealAll(boolean clearFlags) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                revealed[row][column] = true;

                if (clearFlags) {
                    flagged[row][column] = false;
                }
            }
        }
    }

    public void toggleFlag(int row, int column) {
        if (revealed[row][column]) {
            return;
        }

        flagged[row][column] = !flagged[row][column];
        flaggedCellCount += flagged[row][column] ? 1 : -1;
    }

    public String render(String line, Function<CellView, String> function) {
        StringBuilder result = new StringBuilder();
        for (int row = 0; row < rows; row++) {

            StringBuilder lineContent = new StringBuilder();
            for (int column = 0; column < columns; column++) {
                lineContent.append(function.apply(getCell(row, column)));
            }

            result.append(Strings.CS.replace(line, "<line>", lineContent.toString()));
        }

        return result.toString();
    }

    public boolean checkBounds(int row, int column) {
        return row >= 0 && row < rows && column >= 0 && column < columns;
    }

    private CellView getCell(int row, int column) {
        boolean revealMine = mine[row][column] && (revealed[row][column] || state != GameState.IN_PROGRESS);
        return new CellView(
                row,
                column,
                revealed[row][column],
                flagged[row][column],
                revealMine,
                revealed[row][column] ? adjacentMineCount[row][column] : 0
        );
    }

    private void placeMinesAvoiding(int safeRow, int safeColumn) {
        List<int[]> eligibleCells = new ArrayList<>(rows * columns);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                boolean tooCloseToSafeCell = Math.abs(row - safeRow) <= 1 && Math.abs(column - safeColumn) <= 1;
                if (!tooCloseToSafeCell) {
                    eligibleCells.add(new int[]{row, column});
                }
            }
        }

        for (int i = 0; i < mineCount; i++) {
            int swapIndex = i + random.nextInt(eligibleCells.size() - i);
            int[] temporary = eligibleCells.get(i);
            eligibleCells.set(i, eligibleCells.get(swapIndex));
            eligibleCells.set(swapIndex, temporary);
            int[] position = eligibleCells.get(i);
            mine[position[0]][position[1]] = true;
        }

        computeAdjacentMineCounts();
    }

    private void computeAdjacentMineCounts() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (mine[row][column]) {
                    continue;
                }

                int count = 0;
                for (int i = 0; i < NEIGHBOR_ROW_OFFSETS.length; i++) {
                    int neighborRow = row + NEIGHBOR_ROW_OFFSETS[i];
                    int neighborColumn = column + NEIGHBOR_COLUMN_OFFSETS[i];
                    if (checkBounds(neighborRow, neighborColumn) && mine[neighborRow][neighborColumn]) {
                        count++;
                    }
                }

                adjacentMineCount[row][column] = count;
            }
        }
    }

    private int floodFill(int startRow, int startColumn) {
        int openedCellCount = 0;
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(startRow * columns + startColumn);

        while (!queue.isEmpty()) {
            int packedPosition = queue.poll();
            int row = packedPosition / columns;
            int column = packedPosition % columns;

            if (revealed[row][column] || flagged[row][column]) {
                continue;
            }

            revealed[row][column] = true;
            revealedSafeCellCount++;
            openedCellCount++;

            if (adjacentMineCount[row][column] == 0) {
                for (int i = 0; i < NEIGHBOR_ROW_OFFSETS.length; i++) {
                    int neighborRow = row + NEIGHBOR_ROW_OFFSETS[i];
                    int neighborColumn = column + NEIGHBOR_COLUMN_OFFSETS[i];
                    if (checkBounds(neighborRow, neighborColumn)
                            && !revealed[neighborRow][neighborColumn]
                            && !mine[neighborRow][neighborColumn]) {
                        queue.add(neighborRow * columns + neighborColumn);
                    }
                }
            }
        }

        return openedCellCount;
    }

    public enum GameState {
        IN_PROGRESS,
        WIN,
        LOSE
    }

    public record CellView(
            int row,
            int column,
            boolean revealed,
            boolean flagged,
            boolean mine,
            int adjacentMines
    ) {

        public boolean isEmpty() {
            return revealed && !mine && adjacentMines == 0;
        }

    }

    public record RevealResult(GameState state, int cellsOpened, boolean hitMine) {
    }

}