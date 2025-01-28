package project.ui;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import project.Maze;
import project.ui.Animation;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class MazeSprites {
    private Map<Integer, Image> tileSprites = new HashMap<>();
    private Map<String, Animation> animations = new HashMap<>();
    private Map<Integer, Image> leverActiveSprites = new HashMap<>();
    
    // Константы для анимаций
    public static final String PLAYER_RUN = "player_run";
    public static final String PLAYER_IDLE = "player_idle";

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

        loadBasicSprites();
        loadAnimations();
    }

    private void loadBasicSprites() {
        loadSprite(Maze.PATH, "sprites/Castle_stonefloor.png");
        loadSprite(Maze.WALL_H, "sprites/Castle_wallHorizontal.png");
        
        // Загружаем неактивные рычаги (направлены влево)
        loadSprite(Maze.LEVER_1, "sprites/resized_lever_left.png");
        loadSprite(Maze.LEVER_2, "sprites/resized_lever_left.png");
        
        // Загружаем активные рычаги (направлены вправо) в отдельную карту
        leverActiveSprites.put(Maze.LEVER_1, loadImage("sprites/resized_lever_right.png"));
        leverActiveSprites.put(Maze.LEVER_2, loadImage("sprites/resized_lever_right.png"));
        
        Image horizontalWall = tileSprites.get(Maze.WALL_H);
        if (horizontalWall != null) {
            tileSprites.put(Maze.WALL_V, rotateImage(horizontalWall));
        }
        
        loadSprite(Maze.CORNER_1, "sprites/Castle_corner_top_left.png");
        loadSprite(Maze.CORNER_2, "sprites/Castle_corner_top_right.png");
        loadSprite(Maze.CORNER_3, "sprites/Castle_corner_bottom_left.png");
        loadSprite(Maze.CORNER_4, "sprites/Castle_corner_bottom_right.png");
        loadSprite(Maze.DOOR, "sprites/Castle_door.png");
        loadSprite(Maze.START_1, "sprites/Castle_stonefloor.png");
        loadSprite(Maze.START_2, "sprites/Castle_stonefloor.png");
        loadSprite(Maze.FINISH, "sprites/Castle_stonefloor.png");
        
        System.out.println("Loaded " + tileSprites.size() + " sprites");
    }

    private void loadAnimations() {
        // Загружаем кадры анимации бега
        List<Image> runFrames = new ArrayList<>();
        for (int i = 0; i <= 3; i++) {
            loadAnimationFrame(runFrames, "sprites/knight_f_run_anim_f" + i + ".png");
        }
        animations.put(PLAYER_RUN, new Animation(runFrames, 90));
        
        // Загружаем кадры анимации покоя
        List<Image> idleFrames = new ArrayList<>();
        for (int i = 0; i <= 3; i++) {
            loadAnimationFrame(idleFrames, "sprites/knight_f_idle_anim_f" + i + ".png");
        }
        animations.put(PLAYER_IDLE, new Animation(idleFrames, 160));
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

    private Image loadImage(String path) {
        try {
            System.out.println("Loading image: " + path);
            var stream = getClass().getClassLoader().getResourceAsStream(path);
            
            if (stream == null) {
                System.err.println("Resource stream is null for: " + path);
                return null;
            }
            
            Image image = new Image(stream);
            if (image.isError()) {
                System.err.println("Error loading image: " + image.getException().getMessage());
                return null;
            }
            
            System.out.println("Successfully loaded image: " + path);
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load image: " + path);
            e.printStackTrace();
            return null;
        }
    }

    private void loadAnimationFrame(List<Image> frames, String path) {
        try {
            System.out.println("Loading animation frame: " + path);
            var stream = getClass().getClassLoader().getResourceAsStream(path);
            
            if (stream == null) {
                System.err.println("Resource stream is null for: " + path);
                return;
            }
            
            Image frame = new Image(stream);
            if (frame.isError()) {
                System.err.println("Error loading image: " + frame.getException().getMessage());
                return;
            }
            
            frames.add(frame);
            System.out.println("Successfully loaded animation frame: " + path);
        } catch (Exception e) {
            System.err.println("Failed to load animation frame: " + path);
            e.printStackTrace();
        }
    }

    public Image getSpriteForType(int type) {
        return tileSprites.get(type);
    }

    public Image getSprite(int type) {
        return tileSprites.get(type);
    }

    public Animation getAnimation(String animationKey) {
        return animations.get(animationKey);
    }

    public Image getLeverSprite(int leverType, boolean isActive) {
        if (isActive) {
            return leverActiveSprites.get(leverType);
        } else {
            return tileSprites.get(leverType);
        }
    }
}
