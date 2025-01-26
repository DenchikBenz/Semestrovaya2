package project;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.List;
public class Maze {
    private int[][]  grid;
    private final static int CELL_SIZE = 64;  // Устанавливаем 64

    public static final int PATH = 0;      // пол
    public static final int WALL_H = 1;    // горизонтальная стена
    public static final int WALL_V = 2;    // вертикальная стена
    public static final int START_1 = 3;   // старт первого игрока
    public static final int START_2 = 4;   // старт второго игрока
    public static final int LEVER_1 = 5;   // первый рычаг (на полу)
    public static final int LEVER_2 = 6;   // второй рычаг (на полу)
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
    private boolean isDoorOpen;
    private int width;
    private int height;

    public Maze(){

        isDoorOpen = false;

        int[][] mazeData = {
                // Стартовая комната
                {8, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9},
                {2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
                {2, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
                {1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                // Коридор
                {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
                // Комната с рычагами
                {1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {2, 0, 0, 0, 0, 0, 5, 0, 0, 6, 0, 0, 0, 2},
                {1, 1, 1, 1, 0, 1, 1, 14, 1, 1, 1, 1, 1, 1},
                // Коридор к финишу
                {2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
                // Финишная комната
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
            throw new IllegalStateException("Maze must have two start points and a finish point");
        }
        if (leverPoints.size() != 2){
            throw new IllegalStateException("Maze must have two lever points");
        }
    }

    public Point2D getStartPoint1() { return startPoint1; }
    public Point2D getStartPoint2() { return startPoint2; }
    public Point2D getFinishPoint() { return finishPoint; }
    public List<Point2D> getLeverPoints() { return new ArrayList<>(leverPoints); }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getCellSize() { return CELL_SIZE; }


    public boolean isWall(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        int cell = grid[y][x];

        if (cell == DOOR) {
            return !isDoorOpen; // Закрытая дверь работает как стена
        }

        return cell == WALL_H || cell == WALL_V ||
                cell == CORNER_1 || cell == CORNER_2 ||
                cell == CORNER_3 || cell == CORNER_4 ||
                cell == DOOR;
    }

    public boolean isDoorOpen() {
        return isDoorOpen;
    }

    public int getWallType(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return WALL_H;
        return grid[y][x];
    }

    public boolean isFinish(int x, int y) {
        return x == finishPoint.getX() && y == finishPoint.getY();
    }

    public boolean isLever(int x, int y) {
        return grid[y][x] == LEVER_1 || grid[y][x] == LEVER_2;
    }

    public int getCellType(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return -1;  // За пределами лабиринта
        }
        return grid[y][x];
    }

    public boolean canActivateLever(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        int cellType = grid[y][x];
        return cellType == LEVER_1 || cellType == LEVER_2;
    }

}
