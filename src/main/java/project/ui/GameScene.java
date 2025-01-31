package project.ui;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.util.Duration;
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
    private StackPane root;
    private VBox messageOverlay;
    private Timeline fadeOutTimeline;
    private double playerX = 1;
    private double playerY = 1;
    private double otherPlayerX = 1;
    private double otherPlayerY = 1;
    private boolean isServer;

    public interface PlayerMoveCallback {
        void onPlayerMove(double x, double y);
    }

    public interface LeverCallback {
        void onLeverStateChange(int x, int y, boolean active);
    }

    public interface GameStateCallback {
        void onGameStateChanged(GameStateType type, boolean finished);
    }
    
    private PlayerMoveCallback moveCallback;
    private LeverCallback leverCallback;
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

    public void setOnPlayerMove(PlayerMoveCallback callback) {
        this.moveCallback = callback;
    }

    public void setLeverCallback(LeverCallback callback) {
        this.leverCallback = callback;
    }

    public void setGameStateCallback(GameStateCallback callback) {
        this.gameStateCallback = callback;
    }
    
    public void updateOtherLeverState(int x, int y, boolean active) {
        System.out.println("Updating other player's lever state: " + x + "," + y + " = " + active);
        boolean previousState = maze.getLeverState(x, y);
        maze.setLeverState(x, y, active);
        
        if (active) {
            showGameMessage("Второй игрок активировал рычаг!");
        } else if (previousState) {
            showGameMessage("Время истекло! Рычаги сброшены!");
        }
        
        draw();
    }

    public void updateGameState(GameStateType type, boolean otherFinished) {
        Platform.runLater(() -> {
            if (type == GameStateType.STARTED) {
            } else if (type == GameStateType.FINISHED) {
                this.otherPlayerFinished = otherFinished;
                checkGameEnd();
            }
        });
    }

    private void createGameScene() {
        root = new StackPane();
        
        BorderPane gameArea = new BorderPane();
        gameCanvas = new Canvas(maze.getWidth() * maze.getCellSize(), maze.getHeight() * maze.getCellSize());
        gc = gameCanvas.getGraphicsContext2D();
        gameArea.setCenter(gameCanvas);
        
        messageOverlay = new VBox(10);
        messageOverlay.setAlignment(Pos.CENTER); 
        messageOverlay.setMouseTransparent(true);
        messageOverlay.setVisible(false);
        messageOverlay.setStyle("-fx-padding: 20px;");
        
        root.getChildren().addAll(gameArea, messageOverlay);
        
        scene = new Scene(root);
        
        draw();
    }

    public void showGameMessage(String message) {
        Platform.runLater(() -> {
            Label messageLabel = new Label(message);
            messageLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.7);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 24px;" + 
                "-fx-padding: 15px 30px;" + 
                "-fx-background-radius: 10px;" + 
                "-fx-text-alignment: center;" 
            );
            
            messageOverlay.getChildren().setAll(messageLabel);
            messageOverlay.setVisible(true);
            messageOverlay.setOpacity(0); 
            
            if (fadeOutTimeline != null) {
                fadeOutTimeline.stop();
            }
            
            fadeOutTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(messageOverlay.opacityProperty(), 0)),
                new KeyFrame(Duration.seconds(0.5), new KeyValue(messageOverlay.opacityProperty(), 1)), 
                new KeyFrame(Duration.seconds(2.5), new KeyValue(messageOverlay.opacityProperty(), 1)), 
                new KeyFrame(Duration.seconds(3), new KeyValue(messageOverlay.opacityProperty(), 0)) 
            );
            
            fadeOutTimeline.setOnFinished(event -> messageOverlay.setVisible(false));
            fadeOutTimeline.play();
        });
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
                    handleLeverActivation((int)playerX, (int)playerY);
                    break;
            }
        });
    }
    

    private void movePlayer(int dx, int dy) {
        if (maze.isWall((int)(playerX + dx), (int)(playerY + dy))) return;

        if (dx != 0 || dy != 0) {
            currentPlayerAnimation = MazeSprites.PLAYER_RUN;
            isMoving = true;
        }

        playerX += dx;
        playerY += dy;

        if (maze.getCellType((int)playerX, (int)playerY) == Maze.FINISH && !playerFinished) {
            playerFinished = true;
            System.out.println("Player reached finish!");
            if (gameStateCallback != null) {
                gameStateCallback.onGameStateChanged(GameStateType.FINISHED, true);
            }
            showGameMessage("Вы достигли финиша!");
            checkGameEnd();
        }

        if (moveCallback != null) {
            moveCallback.onPlayerMove(playerX, playerY);
        }
    }

    private void handleLeverActivation(int x, int y) {
        if (maze.canActivateLever(x, y)) {
            boolean currentState = maze.getLeverState(x, y);
            maze.setLeverState(x, y, !currentState);
            
            if (!currentState) {
                showGameMessage("Рычаг активирован! У второго игрока есть 2 секунды!");
            }
            
            if (leverCallback != null) {
                leverCallback.onLeverStateChange(x, y, !currentState);
            }
        }
    }

    public void updateOtherPlayerPosition(double x, double y) {
        otherPlayerX = x;
        otherPlayerY = y;
        draw();
    }

    private void draw() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                int cellType = maze.getCellType(x, y);
                Image sprite;
                
                if (cellType == Maze.DOOR) {
                    sprite = maze.isDoorOpen() ? mazeSprites.getSprite(Maze.PATH) : mazeSprites.getSprite(Maze.DOOR);
                }
                else if (cellType == Maze.LEVER_1 || cellType == Maze.LEVER_2) {
                    Image floorSprite = mazeSprites.getSprite(Maze.PATH);
                    if (floorSprite != null) {
                        gc.drawImage(floorSprite, 
                            x * maze.getCellSize(), 
                            y * maze.getCellSize(), 
                            maze.getCellSize(), 
                            maze.getCellSize()
                        );
                    }
                    
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

    

    private void checkGameEnd() {
        if (playerFinished && otherPlayerFinished) {
            showGameMessage("Поздравляем! Оба игрока достигли финиша!");
        }
    }
}