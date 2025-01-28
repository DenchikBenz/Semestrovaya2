package project.server;

import project.common.LeverState;
import project.common.NetworkMessage;
import project.ui.GameScene;
import javafx.application.Platform;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final int PORT = 5004;
    private boolean isRunning;
    private GameScene gameScene;

    private double[] serverPosition = {1, 1};
    private double[] clientPosition = {1, 1};
    private boolean gameStarted = false;

    public GameServer() {
        isRunning = false;
    }

    public void setGameScene(GameScene scene) {
        this.gameScene = scene;
    }

    public void start() {
        try {
            System.out.println("Starting server on port " + PORT);
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("Server started successfully");

            System.out.println("Waiting for client...");
            waitForClient();

        } catch (IOException e) {
            System.err.println("Server start failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void waitForClient() {
        try {
            System.out.println("Waiting for client...");
            clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            // Отправляем подтверждение подключения
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.CONNECT_ACCEPTED, null));

            // Отправляем начальное состояние игры
            sendGameState();

            // Начинаем игру
            gameStarted = true;
            sendMessage(new NetworkMessage(NetworkMessage.MessageType.GAME_START, null));

            handleMessages();

        } catch (IOException e) {
            System.err.println("Accept failed on port " + PORT);
            e.printStackTrace();
        }
    }

    private void handleMessages() {
        while (isRunning) {
            try {
                NetworkMessage message = (NetworkMessage) in.readObject();
                handleMessage(message);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error handling message");
                stop();
                break;
            }
        }
    }

    private void handleMessage(NetworkMessage message) {
        System.out.println("Server received message: " + message.getType());
        switch (message.getType()) {
            case CONNECT:
                System.out.println("Client connected");
                sendGameState();
                break;

            case DISCONNECT:
                System.out.println("Client disconnected");
                stop();
                break;

            case PLAYER_MOVE:
                handlePlayerMove(message);
                break;

            case LEVER_INTERACTION:
                LeverState leverState = (LeverState) message.getData();
                if (gameScene != null) {
                    Platform.runLater(() -> {
                        gameScene.updateOtherLeverState(
                                leverState.getX(),
                                leverState.getY(),
                                leverState.isActive()
                        );
                    });
                }
                sendMessage(message);
                break;

            default:
                System.out.println("Received unknown message type: " + message.getType());
        }
    }

    private void handlePlayerMove(NetworkMessage message) {
        if (!gameStarted) return;

        double[] newPosition = (double[]) message.getData();
        System.out.println("Server: Handling player move: " + newPosition[0] + ", " + newPosition[1]);

        // Обновляем позицию клиента
        clientPosition[0] = newPosition[0];
        clientPosition[1] = newPosition[1];

        // Обновляем UI для отображения клиента
        if (gameScene != null) {
            Platform.runLater(() -> {
                gameScene.updateOtherPlayerPosition(clientPosition[0], clientPosition[1]);
                System.out.println("Server: Updated client position in UI");
            });
        }

        // Отправляем обновленное состояние игры
        sendGameState();
    }

    public void updateServerPosition(double x, double y) {
        System.out.println("Server: Updating server position to " + x + ", " + y);
        serverPosition[0] = x;
        serverPosition[1] = y;
        // При изменении позиции сервера отправляем обновление клиенту
        sendGameState();
    }

    private void sendGameState() {
        GameState state = new GameState(serverPosition, clientPosition);
        sendMessage(new NetworkMessage(
                NetworkMessage.MessageType.GAME_STATE,
                state
        ));
    }

    public void sendMessage(NetworkMessage message) {
        try {
            if (out != null) {
                System.out.println("Server: Sending message type: " + message.getType());
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message");
            e.printStackTrace();
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing server");
            e.printStackTrace();
        }
    }

    public static class GameState implements Serializable {
        private static final long serialVersionUID = 1L;
        public final double[] serverPosition;
        public final double[] clientPosition;

        public GameState(double[] serverPosition, double[] clientPosition) {
            this.serverPosition = serverPosition.clone();
            this.clientPosition = clientPosition.clone();
        }
    }
}