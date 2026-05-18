package ru.servicecenter.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.servicecenter.client.util.SceneManager;

public class ServiceCenterApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.init(stage);
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root, 480, 560);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        stage.setTitle("Сервисный центр — Авторизация");
        stage.setScene(scene);
        stage.setMinWidth(420);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
