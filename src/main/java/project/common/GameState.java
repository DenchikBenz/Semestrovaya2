package project.common;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private GameStateType type;
    private boolean playerFinished;
    
    public enum GameStateType {
        STARTED,    // Игра началась (оба игрока подключены)
        FINISHED    // Игрок достиг финиша
    }
    
    public GameState(GameStateType type, boolean playerFinished) {
        this.type = type;
        this.playerFinished = playerFinished;
    }
    
    public GameStateType getType() {
        return type;
    }
    
    public boolean isPlayerFinished() {
        return playerFinished;
    }
}
