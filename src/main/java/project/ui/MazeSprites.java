package project.ui;

import javafx.scene.image.Image;
import project.Maze;

import java.util.HashMap;
import java.util.Map;

public class MazeSprites {
    private Map<Integer, Image> tileSprites = new HashMap<>();

    public MazeSprites() {
        loadSprite(Maze.PATH, "Castle_stonefloor.png");
        loadSprite(Maze.WALL_H, "Castle_wallHorizontal.png");
        loadSprite(Maze.WALL_V, "Castle_vertical.png");
        loadSprite(Maze.CORNER_1, "Castle_corner_top_left.png");
        loadSprite(Maze.CORNER_2, "Castle_corner_top_right.png");
        loadSprite(Maze.CORNER_3, "Castle_corner_bottom_left.png");
        loadSprite(Maze.CORNER_4, "Castle_corner_bottom_right.png");
        loadSprite(Maze.DOOR, "Castle_door.png");
        loadSprite(Maze.DOOR, "door.png");
        loadSprite(Maze.LEVER_1, "resized_lever_left.png");
        loadSprite(Maze.LEVER_2, "resized_lever_right.png"); // дверь
    }

    private void loadSprite(int type, String filename) {
        try {
           Image sprite = new Image(getClass().getClassLoader().getResourceAsStream("/sprites/" + filename));
           tileSprites.put(type, sprite);
        }catch (Exception e){
            System.err.println("Failed to load sprite: " + filename);
        }
    }
}
