package project.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.application.Platform;
import project.Maze;
import project.common.GameState;
import project.common.GameState.GameStateType;

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

    // Новый интерфейс для событий рычага
    public interface LeverCallback {
        void onLeverStateChange(int x, int y, boolean active);
    }

    // Добавляем новый интерфейс для уведомлений о состоянии игры
    public interface GameStateCallback {
        void onGameStateChanged(GameStateType type, boolean finished);
    }
    
    // Добавляем поле для хранения обработчика
    private PlayerMoveCallback moveCallback;
    private LeverCallback leverCallback;  // Добавляем колбэк для рычага
    private GameStateCallback gameStateCallback;
    private boolean playerFinished = false;
    private boolean otherPlayerFinished = false;

    private String currentPlayerAnimation = MazeSprites.PLAYER_IDLE;
    private boolean isMoving = false;

    private AnimationTimer gameLoop;

    public GameScene(Stage stage, boolean isServer) {
        this.stage = stage;
        this.isServer = isServer;
        this.maze = new Maze();
        this.mazeSprites = new MazeSprites();
        createGameScene();
        setupKeyHandling();
        setupGameLoop();
    }

    // Добавляем метод установки обработчика движения
    public void setOnPlayerMove(PlayerMoveCallback callback) {
        this.moveCallback = callback;
    }

    // Метод установки колбэка для рычага
    public void setLeverCallback(LeverCallback callback) {
        this.leverCallback = callback;
    }

    public void setGameStateCallback(GameStateCallback callback) {
        this.gameStateCallback = callback;
    }
    
    // Метод обновления состояния рычага другого игрока
    public void updateOtherLeverState(int x, int y, boolean active) {
        maze.setLeverState(x, y, active);
        draw();
    }

    public void updateGameState(GameStateType type, boolean otherFinished) {
        Platform.runLater(() -> {
            if (type == GameStateType.STARTED) {
                showAlert("Игра началась", "Второй игрок подключился. Игра началась!");
            } else if (type == GameStateType.FINISHED) {
                this.otherPlayerFinished = otherFinished;
                checkGameEnd();
            }
        });
    }

    private void createGameScene() {
        BorderPane root = new BorderPane();

        gameCanvas = new Canvas(maze.getWidth() * maze.getCellSize(), maze.getHeight() * maze.getCellSize());
        gc = gameCanvas.getGraphicsContext2D();

        root.setCenter(gameCanvas);

        scene = new Scene(root);

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
                case E:
                    // Проверяем, стоит ли игрок на рычаге
                    if (maze.canActivateLever((int)playerX, (int)playerY)) {
                        // Переключаем состояние рычага
                        boolean currentState = maze.getLeverState((int)playerX, (int)playerY);
                        maze.setLeverState((int)playerX, (int)playerY, !currentState);
                        
                        // Уведомляем другого игрока только через leverCallback
                        if (leverCallback != null) {
                            leverCallback.onLeverStateChange((int)playerX, (int)playerY, !currentState);
                        }
                        draw();
                    }
                    break;
            }
        });
    }
    

    private void movePlayer(int dx, int dy) {
        if (maze.isWall((int)(playerX + dx), (int)(playerY + dy))) return;

        // Обновляем анимацию при движении
        if (dx != 0 || dy != 0) {
            currentPlayerAnimation = MazeSprites.PLAYER_RUN;
            isMoving = true;
        }

        playerX += dx;
        playerY += dy;

        if (moveCallback != null) {
            moveCallback.onPlayerMove(playerX, playerY);
        }
    }

    public void updateOtherPlayerPosition(double x, double y) {
        otherPlayerX = x;
        otherPlayerY = y;
        draw();
    }

    private void draw() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        // Отрисовка лабиринта
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                int cellType = maze.getCellType(x, y);
                Image sprite;
                
                // Для двери проверяем состояние
                if (cellType == Maze.DOOR) {
                    sprite = maze.isDoorOpen() ? mazeSprites.getSprite(Maze.PATH) : mazeSprites.getSprite(Maze.DOOR);
                }
                // Для рычагов проверяем состояние
                else if (cellType == Maze.LEVER_1 || cellType == Maze.LEVER_2) {
                    // Сначала рисуем пол под рычагом
                    Image floorSprite = mazeSprites.getSprite(Maze.PATH);
                    if (floorSprite != null) {
                        gc.drawImage(floorSprite, 
                            x * maze.getCellSize(), 
                            y * maze.getCellSize(), 
                            maze.getCellSize(), 
                            maze.getCellSize()
                        );
                    }
                    
                    // Теперь рисуем рычаг в нужном состоянии
                    boolean isActive = maze.getLeverState(x, y);
                    sprite = mazeSprites.getLeverSprite(cellType, isActive);
                }
                else {
                    sprite = mazeSprites.getSprite(cellType);
                }
                
                if (sprite != null) {
                    gc.drawImage(sprite, 
                        x * maze.getCellSize(), 
                        y * maze.getCellSize(), 
                        maze.getCellSize(), 
                        maze.getCellSize()
                    );
                }
            }
        }

        // Отрисовка игроков
        project.ui.Animation playerAnim = mazeSprites.getAnimation(currentPlayerAnimation);
        if (playerAnim != null) {
            Image playerSprite = playerAnim.getCurrentFrame();
            if (playerSprite != null) {
                gc.drawImage(playerSprite, 
                    playerX * maze.getCellSize(), 
                    playerY * maze.getCellSize(), 
                    maze.getCellSize(), 
                    maze.getCellSize()
                );
                gc.drawImage(playerSprite, 
                    otherPlayerX * maze.getCellSize(), 
                    otherPlayerY * maze.getCellSize(), 
                    maze.getCellSize(), 
                    maze.getCellSize()
                );
            }
        }

        // Сбрасываем флаг движения и анимацию
        if (isMoving) {
            isMoving = false;
            currentPlayerAnimation = MazeSprites.PLAYER_IDLE;
        }
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw();
            }
        };
        gameLoop.start();
    }

    public Scene getScene() {
        return scene;
    }

    public void show() {
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void checkGameEnd() {
        if (playerFinished && otherPlayerFinished) {
            if (gameStateCallback != null) {
                gameStateCallback.onGameStateChanged(GameStateType.FINISHED, true);
            }
        }
    }
}