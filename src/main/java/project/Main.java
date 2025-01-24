package project;

import project.client.GameClient;
import project.common.NetworkMessage;
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

        // Загружаем MainMenu через FXML
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
        // Создаем сцену с флагом isServer = true
        gameScene = new GameScene(primaryStage, true);

        // Создаем сервер и устанавливаем GameScene
        server = new GameServer();
        server.setGameScene(gameScene);

        // Добавляем обработчик движения для сервера
        gameScene.setOnPlayerMove((x, y) -> {
            if (server != null) {
                System.out.println("Server: Player moved to " + x + ", " + y);
                // Обновляем позицию на сервере
                server.updateServerPosition(x, y);
                // Отправляем новую позицию клиенту
                server.sendMessage(new NetworkMessage(
                        NetworkMessage.MessageType.PLAYER_MOVE,
                        new double[]{x, y}
                ));
            }
        });

        // Показываем игровую сцену
        gameScene.show();

        // Запускаем сервер в отдельном потоке
        new Thread(() -> {
            server.start();
            isServerStarted = true;
            System.out.println("Server started successfully");
        }).start();
    }

    private void startClient() {
        client = new GameClient();
        if (client.connect()) {
            // Создаем сцену с флагом isServer = false
            gameScene = new GameScene(primaryStage, false);

            // Добавляем обработчик движения для клиента
            gameScene.setOnPlayerMove((x, y) -> {
                if (client != null) {
                    System.out.println("Client: Sending player move: " + x + ", " + y);
                    client.sendMessage(new NetworkMessage(
                            NetworkMessage.MessageType.PLAYER_MOVE,
                            new double[]{x, y}
                    ));
                }
            });

            // Добавляем обработчик входящих сообщений
            client.setMessageHandler(message -> {
                System.out.println("Client: Received message type: " + message.getType());
                switch (message.getType()) {
                    case PLAYER_MOVE:
                        double[] position = (double[]) message.getData();
                        System.out.println("Client: Received move: " + position[0] + ", " + position[1]);
                        Platform.runLater(() -> {
                            gameScene.updateOtherPlayerPosition(position[0], position[1]);
                        });
                        break;

                    case GAME_STATE:
                        GameServer.GameState state = (GameServer.GameState) message.getData();
                        System.out.println("Client: Received game state");
                        Platform.runLater(() -> {
                            // Обновляем позицию сервера
                            gameScene.updateOtherPlayerPosition(state.serverPosition[0], state.serverPosition[1]);
                        });
                        break;

                    case CONNECT_ACCEPTED:
                        System.out.println("Client: Connection accepted by server");
                        break;

                    default:
                        System.out.println("Client: Received unknown message type: " + message.getType());
                }
            });

            // Показываем сцену сразу
            gameScene.show();
            System.out.println("Client: Game scene shown");
        } else {
            System.out.println("Failed to connect to server");
        }
    }

    @Override
    public void stop() {
        System.out.println("Application stopping...");
        if (server != null) {
            server.stop();
            System.out.println("Server stopped");
        }
        if (client != null) {
            client.disconnect();
            System.out.println("Client disconnected");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}