package ru.servicecenter.client.util;

import javafx.application.Platform;
import javafx.scene.control.Label;
import ru.servicecenter.client.api.ApiException;

/**
 * Сообщения внутри экрана без модальных окон.
 */
public final class UiMessages {

    private UiMessages() {
    }

    public static void showError(Label label, String message) {
        show(label, message, "inline-message-error");
    }

    public static void showInfo(Label label, String message) {
        show(label, message, "inline-message-info");
    }

    public static void showSuccess(Label label, String message) {
        show(label, message, "inline-message-success");
    }

    public static void clear(Label label) {
        if (label == null) {
            return;
        }
        Platform.runLater(() -> {
            label.setText("");
            label.getStyleClass().removeAll(
                    "inline-message-error", "inline-message-info", "inline-message-success");
            label.setVisible(false);
            label.setManaged(false);
        });
    }

    public static String errorText(Throwable error) {
        if (error instanceof ApiException apiException) {
            return apiException.getMessage();
        }
        if (error.getMessage() != null && !error.getMessage().isBlank()) {
            return error.getMessage();
        }
        return "Произошла ошибка. Попробуйте ещё раз.";
    }

    private static void show(Label label, String message, String styleClass) {
        if (label == null || message == null || message.isBlank()) {
            return;
        }
        Platform.runLater(() -> {
            label.getStyleClass().removeAll(
                    "inline-message-error", "inline-message-info", "inline-message-success");
            label.getStyleClass().add(styleClass);
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        });
    }
}
