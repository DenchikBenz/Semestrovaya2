package project.ui;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GameScene {
    private Stage stage;
    private Scene scene;
    private Canvas gameCanvas;
    private GraphicsContext gc;

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
        createGameScene();
    }

    // Добавляем метод установки обработчика движения
    public void setOnPlayerMove(PlayerMoveCallback callback) {
        this.moveCallback = callback;
    }

    private void createGameScene() {
        BorderPane root = new BorderPane();

        gameCanvas = new Canvas(MAZE_WIDTH * CELL_SIZE, MAZE_HEIGHT * CELL_SIZE);
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

        if (newX >= 0 && newX < MAZE_WIDTH && newY >= 0 && newY < MAZE_HEIGHT) {
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
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);

        // Рисуем вертикальные линии сетки
        for (int x = 0; x <= MAZE_WIDTH; x++) {
            gc.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, MAZE_HEIGHT * CELL_SIZE);
        }

        // Рисуем горизонтальные линии сетки
        for (int y = 0; y <= MAZE_HEIGHT; y++) {
            gc.strokeLine(0, y * CELL_SIZE, MAZE_WIDTH * CELL_SIZE, y * CELL_SIZE);
        }
    }

    private void drawPlayers() {
        // Сервер всегда красный, клиент всегда синий
        if (isServer) {
            // Свой игрок (сервер)
            gc.setFill(Color.RED);
            gc.fillOval(
                    playerX * CELL_SIZE + 5,
                    playerY * CELL_SIZE + 5,
                    CELL_SIZE - 10,
                    CELL_SIZE - 10
            );

            // Другой игрок (клиент)
            gc.setFill(Color.BLUE);
            gc.fillOval(
                    otherPlayerX * CELL_SIZE + 5,
                    otherPlayerY * CELL_SIZE + 5,
                    CELL_SIZE - 10,
                    CELL_SIZE - 10
            );
        } else {
            // Свой игрок (клиент)
            gc.setFill(Color.BLUE);
            gc.fillOval(
                    playerX * CELL_SIZE + 5,
                    playerY * CELL_SIZE + 5,
                    CELL_SIZE - 10,
                    CELL_SIZE - 10
            );

            // Другой игрок (сервер)
            gc.setFill(Color.RED);
            gc.fillOval(
                    otherPlayerX * CELL_SIZE + 5,
                    otherPlayerY * CELL_SIZE + 5,
                    CELL_SIZE - 10,
                    CELL_SIZE - 10
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