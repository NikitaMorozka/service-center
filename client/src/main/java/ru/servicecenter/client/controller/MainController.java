package ru.servicecenter.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import ru.servicecenter.client.session.Session;
import ru.servicecenter.client.util.SceneManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainController {

    @FXML private Label userLabel;
    @FXML private StackPane contentPane;
    @FXML private Button navRepairs;
    @FXML private Button navHistory;
    @FXML private Button navClients;
    @FXML private Button navUsers;

    private final Map<String, CachedView> viewCache = new HashMap<>();
    private String currentViewPath;

    @FXML
    private void initialize() {
        var user = Session.getUser();
        userLabel.setText(user.getFullName() + " · " + user.getRole());
        navUsers.setVisible(Session.isAdmin());
        navUsers.setManaged(Session.isAdmin());
        boolean canAccessClients = Session.isAdmin() || Session.isManager();
        navClients.setVisible(canAccessClients);
        navClients.setManaged(canAccessClients);
        showRepairs();
    }

    @FXML
    private void showRepairs() {
        loadView("/fxml/repairs.fxml", navRepairs);
    }

    @FXML
    private void showHistory() {
        loadView("/fxml/repair-history.fxml", navHistory);
    }

    @FXML
    private void showClients() {
        if (Session.isAdmin() || Session.isManager()) {
            loadView("/fxml/clients.fxml", navClients);
        }
    }

    @FXML
    private void showUsers() {
        if (Session.isAdmin()) {
            loadView("/fxml/users.fxml", navUsers);
        }
    }

    @FXML
    private void onLogout() throws IOException {
        viewCache.clear();
        currentViewPath = null;
        Session.clear();
        SceneManager.showPreservingWindow("/fxml/login.fxml", "Сервисный центр — Авторизация", 480, 560);
    }

    private void loadView(String fxmlPath, Button active) {
        try {
            CachedView cached = viewCache.computeIfAbsent(fxmlPath, fxmlPath1 -> {
                try {
                    return loadFxml(fxmlPath1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            boolean sameView = Objects.equals(fxmlPath, currentViewPath);
            currentViewPath = fxmlPath;

            contentPane.getChildren().setAll(cached.root);
            setActiveNav(active);

            // Обновляем данные только при первом открытии или смене раздела
            if (!sameView && cached.controller instanceof Refreshable refreshable) {
                refreshable.refresh();
            }
        } catch (Exception e) {
            System.err.println("Не удалось открыть экран: " + e.getMessage());
        }
    }

    private CachedView loadFxml(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        return new CachedView(root, loader.getController());
    }

    private void setActiveNav(Button active) {
        for (Button btn : new Button[]{navRepairs, navHistory, navClients, navUsers}) {
            btn.getStyleClass().remove("nav-button-active");
            if (!btn.getStyleClass().contains("nav-button")) {
                btn.getStyleClass().add("nav-button");
            }
        }
        active.getStyleClass().remove("nav-button");
        active.getStyleClass().add("nav-button-active");
    }

    private record CachedView(Parent root, Object controller) {
    }
}
