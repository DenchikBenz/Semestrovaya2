package project.ui;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import project.Maze;

public class GameScene {
    private Stage stage;
    private Scene scene;
    private Canvas gameCanvas;
    private GraphicsContext gc;
    private Maze maze;
    private MazeSprites mazeSprites;


    // Размеры одной клетки лабиринта
    private final int CELL_SIZE = 40;
    // Размеры поля (в клетках)
    private final int MAZE_WIDTH = 15;
    private final int MAZE_HEIGHT = 15;

    // Позиции игроков
    private double playerX = 1;
    private double playerY = 1;
    private double otherPlayerX = 1;
    private double otherPlayerY = 1;

    // Флаг для определения, является ли это сервером
    private boolean isServer;

    // Добавляем интерфейс для обработчика движения
    public interface PlayerMoveCallback {
        void onPlayerMove(double x, double y);
    }

    // Добавляем поле для хранения обработчика
    private PlayerMoveCallback moveCallback;

    public GameScene(Stage stage, boolean isServer) {
        this.stage = stage;
        this.isServer = isServer;
        this.maze = new Maze();
        this.mazeSprites = new MazeSprites();
        createGameScene();
    }

    // Добавляем метод установки обработчика движения
    public void setOnPlayerMove(PlayerMoveCallback callback) {
        this.moveCallback = callback;
    }

    private void createGameScene() {
        BorderPane root = new BorderPane();

        gameCanvas = new Canvas(maze.getWidth() * maze.getCellSize(), maze.getHeight() * maze.getCellSize());
        gc = gameCanvas.getGraphicsContext2D();

        root.setCenter(gameCanvas);

        scene = new Scene(root);

        setupKeyHandling();

        draw();
    }

    private void setupKeyHandling() {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W:
                case UP:
                    movePlayer(0, -1);
                    break;
                case S:
                case DOWN:
                    movePlayer(0, 1);
                    break;
                case A:
                case LEFT:
                    movePlayer(-1, 0);
                    break;
                case D:
                case RIGHT:
                    movePlayer(1, 0);
                    break;
            }
        });
    }

    private void movePlayer(int dx, int dy) {
        // Проверяем, не выходит ли игрок за пределы поля
        double newX = playerX + dx;
        double newY = playerY + dy;

        if (newX >= 0 && newX < maze.getWidth() && newY >= 0 && newY < maze.getHeight()) {
            playerX = newX;
            playerY = newY;

            // Вызываем обработчик движения
            if (moveCallback != null) {
                moveCallback.onPlayerMove(playerX, playerY);
            }

            draw();
        }
    }

    public void updateOtherPlayerPosition(double x, double y) {
        otherPlayerX = x;
        otherPlayerY = y;
        draw();
    }

    private void draw() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        drawMaze();
        drawPlayers();
    }

    private void drawMaze() {
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                int cellType = maze.getCellType(x, y);
                Image sprite = mazeSprites.getSpriteForType(cellType);
                if (sprite != null) {
                    // Отрисовываем спрайт с масштабированием до размера ячейки
                    gc.drawImage(sprite,
                            x * maze.getCellSize(),
                            y * maze.getCellSize(),
                            maze.getCellSize(),  // Ширина ячейки
                            maze.getCellSize()); // Высота ячейки
                }
            }
        }
    }

    private void drawPlayers() {
        // Сервер всегда красный, клиент всегда синий
        if (isServer) {
            // Свой игрок (сервер)
            gc.setFill(Color.RED);
            gc.fillOval(
                    playerX * maze.getCellSize() + 10,
                    playerY * maze.getCellSize() + 10,
                    maze.getCellSize() - 20,
                    maze.getCellSize() - 20
            );

            // Другой игрок (клиент)
            gc.setFill(Color.BLUE);
            gc.fillOval(
                    otherPlayerX * maze.getCellSize() + 10,
                    otherPlayerY * maze.getCellSize() + 10,
                    maze.getCellSize() - 20,
                    maze.getCellSize() - 20
            );
        } else {
            // Свой игрок (клиент)
            gc.setFill(Color.BLUE);
            gc.fillOval(
                    playerX * maze.getCellSize() + 10,
                    playerY * maze.getCellSize() + 10,
                    maze.getCellSize() - 20,
                    maze.getCellSize() - 20
            );

            // Другой игрок (сервер)
            gc.setFill(Color.RED);
            gc.fillOval(
                    otherPlayerX * maze.getCellSize() + 10,
                    otherPlayerY * maze.getCellSize() + 10,
                    maze.getCellSize() - 20,
                    maze.getCellSize() - 20
            );
        }
    }

    public Scene getScene() {
        return scene;
    }

    public void show() {
        stage.setScene(scene);
        stage.show();
    }
}