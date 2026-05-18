package ru.servicecenter.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.servicecenter.client.dto.AuthResponse;
import ru.servicecenter.client.service.AuthApiService;
import ru.servicecenter.client.session.Session;
import ru.servicecenter.client.util.FxTasks;
import ru.servicecenter.client.util.SceneManager;
import ru.servicecenter.client.util.UiMessages;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthApiService authApi = new AuthApiService();

    private void showLoginError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    private void onLogin() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showLoginError("Введите логин и пароль");
            return;
        }

        loginButton.setDisable(true);
        FxTasks.run(
                () -> authApi.login(username, password),
                (AuthResponse response) -> {
                    loginButton.setDisable(false);
                    Session.setUser(response);
                    try {
                        SceneManager.showPreservingWindow("/fxml/main.fxml", "Сервисный центр", 1360, 820);
                    } catch (Exception ex) {
                        errorLabel.setText("Ошибка загрузки: " + ex.getMessage());
                    }
                },
                error -> {
                    loginButton.setDisable(false);
                    showLoginError(UiMessages.errorText(error));
                }
        );
    }
}
