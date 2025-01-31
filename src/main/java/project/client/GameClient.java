package project.client;

import project.common.NetworkMessage;
import project.common.LeverState;
import project.common.GameState;
import project.common.GameState.GameStateType;
import project.ui.GameScene;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.*;
import java.net.Socket;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final String HOST = "192.168.100.88";
    private final int PORT = 5004;
    private boolean isConnected;
    private Thread listenThread;
    private GameScene gameScene;

    public interface MessageHandler {
        void handleMessage(NetworkMessage message);
    }

    private MessageHandler messageHandler;

    public GameClient() {
        isConnected = false;
    }

    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }

    private void handleMessage(NetworkMessage message) {
        if (messageHandler != null) {
            messageHandler.handleMessage(message);
        }

        System.out.println("Client received message: " + message.getType());

        switch (message.getType()) {
            case CONNECT_ACCEPTED:
                if (gameScene != null) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Подключение");
                        alert.setHeaderText(null);
                        alert.setContentText("Второй игрок подключился. Игра началась!");
                        alert.showAndWait();
                    });
                }
                break;
                
            case LEVER_INTERACTION:
                if (gameScene != null) {
                    LeverState leverState = (LeverState) message.getData();
                    Platform.runLater(() -> {
                        gameScene.updateOtherLeverState(
                            leverState.getX(),
                            leverState.getY(),
                            leverState.isActive()
                        );
                    });
                } else {
                    System.out.println("GameScene is null!");
                }
                break;
            case GAME_START:
            case GAME_END:
                if (gameScene != null) {
                    GameState gameState = (GameState) message.getData();
                    Platform.runLater(() -> {
                        gameScene.updateGameState(
                            gameState.getType(),
                            gameState.isPlayerFinished()
                        );
                    });
                }
                break;
        }
    }

    public void setGameScene(GameScene scene) {
        this.gameScene = scene;
        
        scene.setLeverCallback((x, y, active) -> {
            LeverState leverState = new LeverState(x, y, active);
            NetworkMessage message = new NetworkMessage(
                NetworkMessage.MessageType.LEVER_INTERACTION,
                leverState
            );
            sendMessage(message);
        });
        
        scene.setGameStateCallback((type, finished) -> {
            GameState gameState = new GameState(type, finished);
            NetworkMessage message = new NetworkMessage(
                type == GameStateType.STARTED ? NetworkMessage.MessageType.GAME_START : NetworkMessage.MessageType.GAME_END,
                gameState
            );
            sendMessage(message);
        });
    }

    public boolean connect() {
        try {
            socket = new Socket(HOST, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            isConnected = true;
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.CONNECT, null));
            startListening();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void startListening() {
        listenThread = new Thread(() -> {
            while (isConnected) {
                try {
                    NetworkMessage message = (NetworkMessage) in.readObject();
                    handleMessage(message);
                } catch (IOException | ClassNotFoundException e) {
                    disconnect();
                    break;
                }
            }
        });
        listenThread.start();
    }

    public void sendMessage(NetworkMessage message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            isConnected = false;
            if (socket != null && !socket.isClosed()) {
                sendMessage(new NetworkMessage(NetworkMessage.MessageType.DISCONNECT, null));

                in.close();
                out.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}