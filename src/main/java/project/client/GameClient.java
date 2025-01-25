package project.client;

import project.common.NetworkMessage;
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
}