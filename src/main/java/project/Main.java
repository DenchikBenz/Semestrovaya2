package project;

import project.client.GameClient;
import project.common.GameState;
import project.common.NetworkMessage;
import project.common.LeverState;
import project.server.GameServer;
import project.ui.MainMenu;
import project.ui.GameScene;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import project.ui.MainMenuController;

public class Main extends Application {
    private GameServer server;
    private GameClient client;
    private Stage primaryStage;
    private GameScene gameScene;
    private boolean isServerStarted = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Maze Game");

        MainMenu mainMenu = new MainMenu(primaryStage);
        mainMenu.show(new MainMenuController.MenuCallback() {
            @Override
            public void onHostSelected() {
                if (!isServerStarted) {
                    startServer();
                }
            }

            @Override
            public void onJoinSelected() {
                startClient();
            }
        });
    }


    private void startServer() {
        gameScene = new GameScene(primaryStage, true);

        server = new GameServer();
        server.setGameScene(gameScene);

        gameScene.setOnPlayerMove((x, y) -> {
            if (server != null) {
                server.updateServerPosition(x, y);
                server.sendMessage(new NetworkMessage(
                        NetworkMessage.MessageType.PLAYER_MOVE,
                        new double[]{x, y}
                ));
            }
        });

        gameScene.setLeverCallback((x, y, state) -> {
            if (server != null) {
                server.sendMessage(new NetworkMessage(
                    NetworkMessage.MessageType.LEVER_INTERACTION,
                    new LeverState(x, y, state)
                ));
            }
        });

        gameScene.show();

        new Thread(() -> {
            server.start();
            isServerStarted = true;
        }).start();
    }

    private void startClient() {
        client = new GameClient();
        if (client.connect()) {
            gameScene = new GameScene(primaryStage, false);

            gameScene.setOnPlayerMove((x, y) -> {
                if (client != null) {
                    client.sendMessage(new NetworkMessage(
                            NetworkMessage.MessageType.PLAYER_MOVE,
                            new double[]{x, y}
                    ));
                }
            });

            gameScene.setLeverCallback((x, y, state) -> {
                if (client != null) {
                    client.sendMessage(new NetworkMessage(
                        NetworkMessage.MessageType.LEVER_INTERACTION,
                        new LeverState(x, y, state)
                    ));
                }
            });

            gameScene.setGameStateCallback((type, finished) -> {
                if (client != null) {
                    client.sendMessage(new NetworkMessage(
                        NetworkMessage.MessageType.GAME_END,
                        new GameState(type, finished)
                    ));
                }
            });

            client.setMessageHandler(message -> {
                switch (message.getType()) {
                    case PLAYER_MOVE:
                        double[] position = (double[]) message.getData();
                        Platform.runLater(() -> {
                            gameScene.updateOtherPlayerPosition(position[0], position[1]);
                        });
                        break;

                    case CONNECT_ACCEPTED:
                        Platform.runLater(() -> {
                            gameScene.showGameMessage("Второй игрок подключился. Игра началась!");
                        });
                        break;

                    case GAME_END:
                        GameState gameState = (GameState) message.getData();
                        Platform.runLater(() -> {
                            gameScene.updateGameState(gameState.getType(), gameState.isPlayerFinished());
                        });
                        break;

                    case LEVER_INTERACTION:
                        LeverState leverState = (LeverState) message.getData();
                        Platform.runLater(() -> {
                            gameScene.updateOtherLeverState(leverState.getX(), leverState.getY(), leverState.isActive());
                        });
                        break;

                    default:
                        System.out.println("Error" + message.getType());
                }
            });

            gameScene.show();
        } else {
            System.out.println("Failed connection");
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.disconnect();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}