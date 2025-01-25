package project.ui;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import project.Maze;

import java.util.HashMap;
import java.util.Map;

public class MazeSprites {
    private Map<Integer, Image> tileSprites = new HashMap<>();

    public MazeSprites() {
        try {
            var stream = getClass().getClassLoader().getResourceAsStream("sprites/Castle_stonefloor.png");
            if (stream != null) {
                System.out.println("Test sprite stream loaded successfully");
                Image testImage = new Image(stream);
                System.out.println("Test sprite loaded successfully");
            } else {
                System.err.println("Test sprite stream is null");
            }
        } catch (Exception e) {
            System.err.println("Error loading test sprite:");
            e.printStackTrace();
        }


        loadSprite(Maze.PATH, "sprites/Castle_stonefloor.png");
        loadSprite(Maze.WALL_H, "sprites/Castle_wallHorizontal.png");
        

        Image horizontalWall = tileSprites.get(Maze.WALL_H);
        if (horizontalWall != null) {
            tileSprites.put(Maze.WALL_V, rotateImage(horizontalWall));
        }
        
        loadSprite(Maze.CORNER_1, "sprites/Castle_corner_top_left.png");
        loadSprite(Maze.CORNER_2, "sprites/Castle_corner_top_right.png");
        loadSprite(Maze.CORNER_3, "sprites/Castle_corner_bottom_left.png");
        loadSprite(Maze.CORNER_4, "sprites/Castle_corner_bottom_right.png");
        loadSprite(Maze.DOOR, "sprites/Castle_door.png");
        loadSprite(Maze.LEVER_1, "sprites/resized_lever_left.png");
        loadSprite(Maze.LEVER_2, "sprites/resized_lever_right.png");

        Image floorSprite = tileSprites.get(Maze.PATH);
        if (floorSprite != null) {
            tileSprites.put(Maze.START_1, floorSprite);
            tileSprites.put(Maze.START_2, floorSprite);
            tileSprites.put(Maze.FINISH, floorSprite);
        }
        
        System.out.println("Loaded " + tileSprites.size() + " sprites");
    }

    private Image rotateImage(Image source) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        
        Canvas canvas = new Canvas(height, width);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Поворачиваем на 90 градусов
        gc.translate(height/2, width/2);
        gc.rotate(90);
        gc.translate(-width/2, -height/2);
        
        gc.drawImage(source, 0, 0);
        
        WritableImage rotatedImage = new WritableImage(height, width);
        canvas.snapshot(null, rotatedImage);
        
        return rotatedImage;
    }

    private void loadSprite(int type, String resourcePath) {
        try {
            System.out.println("Loading sprite: " + resourcePath);
            var stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            
            if (stream == null) {
                System.err.println("Resource stream is null for: " + resourcePath);
                return;
            }
            
            Image sprite = new Image(stream);
            if (sprite.isError()) {
                System.err.println("Error loading image: " + sprite.getException().getMessage());
                return;
            }
            
            tileSprites.put(type, sprite);
            System.out.println("Successfully loaded sprite: " + resourcePath);
        } catch (Exception e) {
            System.err.println("Failed to load sprite: " + resourcePath);
            e.printStackTrace();
        }
    }

    public Image getSpriteForType(int type) {
        Image sprite = tileSprites.get(type);
        if (sprite == null) {
            System.err.println("No sprite found for type: " + type);
        }
        return sprite;
    }
}
