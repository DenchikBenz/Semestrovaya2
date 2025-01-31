package project.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainMenu {
    private Stage stage;
    private MainMenuController controller;

    public MainMenu(Stage stage) {
        this.stage = stage;
    }

    public void show(MainMenuController.MenuCallback callback) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/ui/MainMenu.fxml"));
            Parent root = loader.load();

            controller = loader.getController();
            controller.setCallback(callback);

            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
