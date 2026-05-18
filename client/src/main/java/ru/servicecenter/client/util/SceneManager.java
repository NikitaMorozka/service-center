package ru.servicecenter.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public final class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    /** Первый экран (логин) с фиксированным размером. */
    public static void show(String fxmlPath, String title, double width, double height) throws IOException {
        Parent root = loadRoot(fxmlPath);
        Scene scene = new Scene(root, width, height);
        applyStylesheet(scene);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(width * 0.7);
        primaryStage.setMinHeight(height * 0.7);
        primaryStage.setMaximized(false);
    }

    /**
     * Смена экрана с сохранением режима окна: полноэкранный остаётся полноэкранным,
     * оконный — оконным (размер и позиция сохраняются).
     */
    public static void showPreservingWindow(String fxmlPath, String title, double defaultWidth, double defaultHeight)
            throws IOException {
        boolean maximized = primaryStage.isMaximized();
        double prevWidth = primaryStage.getWidth();
        double prevHeight = primaryStage.getHeight();
        double prevX = primaryStage.getX();
        double prevY = primaryStage.getY();

        Parent root = loadRoot(fxmlPath);
        Scene scene;
        if (maximized) {
            scene = new Scene(root);
        } else {
            double w = prevWidth >= primaryStage.getMinWidth() ? prevWidth : defaultWidth;
            double h = prevHeight >= primaryStage.getMinHeight() ? prevHeight : defaultHeight;
            scene = new Scene(root, w, h);
        }
        applyStylesheet(scene);
        primaryStage.setTitle(title);
        primaryStage.setMinWidth(defaultWidth * 0.7);
        primaryStage.setMinHeight(defaultHeight * 0.7);
        primaryStage.setScene(scene);

        if (maximized) {
            primaryStage.setMaximized(true);
        } else {
            primaryStage.setMaximized(false);
            primaryStage.setWidth(scene.getWidth());
            primaryStage.setHeight(scene.getHeight());
            if (!Double.isNaN(prevX) && prevX >= 0) {
                primaryStage.setX(prevX);
            }
            if (!Double.isNaN(prevY) && prevY >= 0) {
                primaryStage.setY(prevY);
            }
        }
    }

    private static Parent loadRoot(String fxmlPath) throws IOException {
        return FXMLLoader.load(Objects.requireNonNull(SceneManager.class.getResource(fxmlPath)));
    }

    private static void applyStylesheet(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(
                SceneManager.class.getResource("/css/app.css")).toExternalForm());
    }
}
