package project.common;

import java.io.Serializable;

public class LeverState implements Serializable {
    private static final long serialVersionUID = 1L;

    private int x;
    private int y;
    private boolean active;

    public LeverState(int x, int y, boolean active) {
        this.x = x;
        this.y = y;
        this.active = active;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isActive() { return active; }
}
