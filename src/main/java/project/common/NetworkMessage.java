package project.common;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private Object data;

    public enum MessageType {
        // Сообщения подключения/отключения
        CONNECT,              // запрос на подключение
        CONNECT_ACCEPTED,     // подтверждение подключения
        DISCONNECT,           // отключение игрока

        // Сообщения состояния игры
        GAME_START,          // начало игры (когда оба игрока готовы)
        GAME_END,            // завершение игры (достигнут финиш)
        GAME_STATE,          // обновление состояния игры

        // Игровые действия
        PLAYER_MOVE,         // движение игрока
        LEVER_INTERACTION    // взаимодействие с рычагом
    }

    // Конструктор
    public NetworkMessage(MessageType type, Object data) {
        this.type = type;
        this.data = data;
    }

    // Геттеры
    public MessageType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
