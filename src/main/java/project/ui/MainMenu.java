package project.ui;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenu {
    private Stage stage;
    private Scene scene;
    private VBox root;

    // Callback интерфейсы для обработки выбора режима
    public interface GameModeCallback {
        void onHostSelected();
        void onJoinSelected();
    }

    public MainMenu(Stage stage, GameModeCallback callback) {
        this.stage = stage;
        createUI(callback);
    }

    private void createUI(GameModeCallback callback) {
        root = new VBox(20); // 20 - отступ между элементами
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50;"); // Тёмный фон

        // Заголовок
        Label titleLabel = new Label("Maze Game");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: white;");

        // Кнопка Host Game
        Button hostButton = new Button("Host Game");
        styleButton(hostButton);
        hostButton.setOnAction(e -> callback.onHostSelected());

        // Кнопка Join Game
        Button joinButton = new Button("Join Game");
        styleButton(joinButton);
        joinButton.setOnAction(e -> callback.onJoinSelected());

        // Добавляем элементы в VBox
        root.getChildren().addAll(titleLabel, hostButton, joinButton);

        // Создаём сцену
        scene = new Scene(root, 800, 600);
    }

    private void styleButton(Button button) {
        button.setStyle(
                "-fx-background-color: #3498db;" + // Синий фон
                        "-fx-text-fill: white;" + // Белый текст
                        "-fx-font-size: 18px;" + // Размер шрифта
                        "-fx-min-width: 200px;" + // Минимальная ширина
                        "-fx-min-height: 40px;" + // Минимальная высота
                        "-fx-cursor: hand;" // Курсор-рука при наведении
        );

        // Эффект при наведении
        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle() + "-fx-background-color: #2980b9;")
        );

        // Возврат к обычному стилю
        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle() + "-fx-background-color: #3498db;")
        );
    }

    public Scene getScene() {
        return scene;
    }

    public void show() {
        stage.setScene(scene);
        stage.show();
    }
}
