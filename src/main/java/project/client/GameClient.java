package project.client;

import project.common.NetworkMessage;
import project.common.LeverState;
import project.common.GameState;
import project.common.GameState.GameStateType;
import project.ui.GameScene;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final String HOST = "localhost";
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

    // Добавляем метод установки обработчика
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }

    // Обновляем метод handleMessage для использования обработчика
    private void handleMessage(NetworkMessage message) {
        if (messageHandler != null) {
            messageHandler.handleMessage(message);
        }

        // Обрабатываем сообщение локально
        switch (message.getType()) {
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
        
        // Устанавливаем обработчик событий рычага
        scene.setLeverCallback((x, y, active) -> {
            LeverState leverState = new LeverState(x, y, active);
            NetworkMessage message = new NetworkMessage(
                NetworkMessage.MessageType.LEVER_INTERACTION,
                leverState
            );
            sendMessage(message);
        });
        
        // Добавляем обработчик состояний игры
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
            System.out.println("Trying to connect to server on port " + PORT);
            socket = new Socket(HOST, PORT);
            System.out.println("Socket connected");

            out = new ObjectOutputStream(socket.getOutputStream());

            in = new ObjectInputStream(socket.getInputStream());

            isConnected = true;

            System.out.println("Sending CONNECT message");
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.CONNECT, null));

            System.out.println("Starting listening thread");
            startListening();

            System.out.println("Connected to server successfully");
            return true;

        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
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
                    System.err.println("Error receiving message");
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
            System.err.println("Error sending message");
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
            System.err.println("Error disconnecting");
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void start() {
        connect();
        if (isConnected) {
            startListening();
            // Отправляем сообщение о готовности
            GameState gameState = new GameState(GameStateType.STARTED, false);
            NetworkMessage message = new NetworkMessage(NetworkMessage.MessageType.GAME_START, gameState);
            sendMessage(message);
        }
    }
}