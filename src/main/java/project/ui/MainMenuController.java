package project.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MainMenuController {
    @FXML
    private Button hostButton;
    @FXML
    private Button joinButton;

    private MenuCallback callback;

    // Интерфейс для обработки действий
    public interface MenuCallback {
        void onHostSelected();
        void onJoinSelected();
    }

    public void setCallback(MenuCallback callback) {
        this.callback = callback;
    }

    @FXML
    public void initialize() {
        // Обработка кнопок
        hostButton.setOnAction(event -> {
            if (callback != null) {
                callback.onHostSelected();
            }
        });

        joinButton.setOnAction(event -> {
            if (callback != null) {
                callback.onJoinSelected();
            }
        });
    }
}

