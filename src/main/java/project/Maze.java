package project;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Maze {
    private int[][]  grid;
    private final static int CELL_SIZE = 64;

    public static final int PATH = 0;
    public static final int WALL_H = 1;
    public static final int WALL_V = 2;
    public static final int START_1 = 3;
    public static final int START_2 = 4;
    public static final int LEVER_1 = 5;
    public static final int LEVER_2 = 6;
    public static final int FINISH = 7;
    public static final int CORNER_1 = 8;
    public static final int CORNER_2 = 9;
    public static final int CORNER_3 = 10;
    public static final int CORNER_4 = 11;
    public static final int DOOR = 14;

    private Point2D startPoint1;
    private Point2D startPoint2;
    private Point2D finishPoint;
    private List<Point2D>leverPoints;
    private boolean lever1Active = false;
    private boolean lever2Active = false;
    private boolean isDoorOpen;
    private int width;
    private int height;
    private long lever1ActivationTime = 0;
    private long lever2ActivationTime = 0;
    private static final long LEVER_TIMEOUT = 2000;
    private Timer leverTimer;

    public Maze(){

        isDoorOpen = false;

        int[][] mazeData = {
                {8, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9},
                {2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
                {2, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
                {1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
                {1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {2, 0, 0, 0, 0, 0, 5, 0, 0, 6, 0, 0, 0, 2},
                {1, 1, 1, 1, 1, 1, 1, 14, 1, 1, 1, 1, 1, 1},
                {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
                {1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 2},
                {10, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 11}
        };
        loadMaze(mazeData);
    }

    private void loadMaze(int[][] mazeData){
        height = mazeData.length;
        width = mazeData[0].length;
        grid = new int[height][width];
        leverPoints = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = mazeData[y][x];
                switch (mazeData[y][x]) {
                    case START_1 -> startPoint1 = new Point2D(x, y);
                    case START_2 -> startPoint2 = new Point2D(x, y);
                    case FINISH -> finishPoint = new Point2D(x, y);
                    case LEVER_1, LEVER_2 -> leverPoints.add(new Point2D(x, y));
                }
            }
        }
        validateMaze();
    }

    private void validateMaze(){
        if (startPoint1 == null || startPoint2 == null || finishPoint == null) {
            throw new IllegalStateException("");
        }
        if (leverPoints.size() != 2){
            throw new IllegalStateException("");
        }
    }

    public synchronized void setLeverState(int x, int y, boolean state) {
        if (getCellType(x, y) == LEVER_1) {
            lever1Active = state;
            if (state) {
                lever1ActivationTime = System.currentTimeMillis();
                checkLeversTimeout();
            }
        } else if (getCellType(x, y) == LEVER_2) {
            lever2Active = state;
            if (state) {
                lever2ActivationTime = System.currentTimeMillis();
                checkLeversTimeout();
            }
        }

        if (lever1Active && lever2Active) {
            long currentTime = System.currentTimeMillis();
            long timeDiff = Math.abs(lever1ActivationTime - lever2ActivationTime);

            if (timeDiff <= LEVER_TIMEOUT) {
                isDoorOpen = true;
                if (leverTimer != null) {
                    leverTimer.cancel();
                    leverTimer = null;
                }
            }
        }
    }

    private void checkLeversTimeout() {
        if (leverTimer != null) {
            leverTimer.cancel();
        }

        leverTimer = new Timer();
        leverTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (lever1Active || lever2Active) {
                        long currentTime = System.currentTimeMillis();
                        boolean shouldReset = false;

                        if (lever1Active && (currentTime - lever1ActivationTime > LEVER_TIMEOUT)) {
                            shouldReset = true;
                        }
                        if (lever2Active && (currentTime - lever2ActivationTime > LEVER_TIMEOUT)) {
                            shouldReset = true;
                        }

                        if (shouldReset) {
                            System.out.println("Timeout reached, resetting levers");
                            resetLevers();
                        }
                    }
                });
            }
        }, LEVER_TIMEOUT);
    }

    private synchronized void resetLevers() {
        System.out.println("Resetting levers to initial state");
        lever1Active = false;
        lever2Active = false;
        lever1ActivationTime = 0;
        lever2ActivationTime = 0;
        isDoorOpen = false;

        if (leverTimer != null) {
            leverTimer.cancel();
            leverTimer = null;
        }
    }

    public boolean isDoorOpen() {
        return isDoorOpen;
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCellSize() {
        return CELL_SIZE;
    }



    public boolean getLeverState(int x, int y) {
        int leverType = grid[y][x];
        if (leverType == LEVER_1) {
            return lever1Active;
        } else if (leverType == LEVER_2) {
            return lever2Active;
        }
        return false;
    }

    public boolean isWall(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        int cell = grid[y][x];

        if (cell == DOOR) {
            return !isDoorOpen;
        }

        return cell == WALL_H || cell == WALL_V ||
                cell == CORNER_1 || cell == CORNER_2 ||
                cell == CORNER_3 || cell == CORNER_4 ||
                cell == DOOR;
    }

    public int getCellType(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return -1;
        }
        return grid[y][x];
    }

    public boolean canActivateLever(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        int cellType = grid[y][x];
        return cellType == LEVER_1 || cellType == LEVER_2;
    }
}
