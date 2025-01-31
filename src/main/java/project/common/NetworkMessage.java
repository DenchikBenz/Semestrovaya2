package project.common;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private Object data;

    public enum MessageType {
        CONNECT,
        CONNECT_ACCEPTED,
        DISCONNECT,


        GAME_START,
        GAME_END,
        GAME_STATE,

        PLAYER_MOVE,
        LEVER_INTERACTION
    }

    public NetworkMessage(MessageType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
