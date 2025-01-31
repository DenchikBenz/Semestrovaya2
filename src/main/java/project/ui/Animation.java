package project.ui;

import javafx.scene.image.Image;
import java.util.List;

public class Animation {
    private List<Image> frames;
    private int currentFrame;
    private long lastUpdateTime;
    private long frameDuration;

    public Animation(List<Image> frames, long frameDuration) {
        this.frames = frames;
        this.frameDuration = frameDuration;
        this.currentFrame = 0;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public Image getCurrentFrame() {
        if (frames.isEmpty()) {
            return null;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > frameDuration) {
            currentFrame = (currentFrame + 1) % frames.size();
            lastUpdateTime = currentTime;
        }
        return frames.get(currentFrame);
    }

    public void reset() {
        currentFrame = 0;
        lastUpdateTime = System.currentTimeMillis();
    }
}
