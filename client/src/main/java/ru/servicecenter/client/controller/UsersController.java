package ru.servicecenter.client.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.servicecenter.client.dto.UserDto;
import ru.servicecenter.client.dto.UserRequest;
import ru.servicecenter.client.service.UserApiService;
import ru.servicecenter.client.util.FormValidator;
import ru.servicecenter.client.util.FxTasks;
import ru.servicecenter.client.util.TableStyles;

import java.util.List;

public class UsersController extends BaseViewController implements Refreshable {

    @FXML private Label messageLabel;
    @FXML private TableView<UserDto> table;
    @FXML private TableColumn<UserDto, Long> colId;
    @FXML private TableColumn<UserDto, String> colLogin, colName, colEmail, colRole;
    @FXML private TableColumn<UserDto, Boolean> colActive;
    @FXML private TextField usernameField, fullNameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private CheckBox activeCheck;

    private final UserApiService api = new UserApiService();
    private Long editingId;
    private boolean suppressSelection;

    @FXML
    private void initialize() {
        bindMessageLabel(messageLabel);
        TableStyles.apply(table);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("username"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colActive.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Да" : "Нет"));
                setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            }
        });

        roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "MANAGER", "MASTER"));

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (!suppressSelection && selected != null) {
                fillForm(selected);
            }
        });
    }

    @Override
    public void refresh() {
        loadUsers(editingId);
    }

    @Override
    protected Node[] busyNodes() {
        return new Node[]{table};
    }

    @FXML
    private void onRefresh() {
        loadUsers(editingId);
    }

    @FXML
    private void onAdd() {
        editingId = null;
        onClear();
    }

    @FXML
    private void onEdit() {
        UserDto selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fillForm(selected);
        }
    }

    @FXML
    private void onDelete() {
        UserDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите пользователя в таблице");
            return;
        }
        runAsync(() -> {
            api.delete(selected.getId());
            return null;
        }, () -> {
            editingId = null;
            onClear();
            showSuccess("Пользователь удалён");
            loadUsers(null);
        });
    }

    @FXML
    private void onSave() {
        clearMessage();
        if (!FormValidator.check(FormValidator.username(usernameField.getText()), messageLabel)
                || !FormValidator.check(FormValidator.fullName(fullNameField.getText()), messageLabel)
                || !FormValidator.check(FormValidator.emailOptional(emailField.getText()), messageLabel)
                || !FormValidator.check(FormValidator.role(roleCombo.getValue()), messageLabel)
                || !FormValidator.check(FormValidator.password(passwordField.getText(), editingId == null), messageLabel)) {
            return;
        }

        UserRequest request = UserRequest.builder()
                .username(usernameField.getText().trim())
                .password(passwordField.getText().isBlank() ? null : passwordField.getText())
                .fullName(fullNameField.getText().trim())
                .email(emailField.getText().trim())
                .role(roleCombo.getValue())
                .active(activeCheck.isSelected())
                .build();

        Long id = editingId;
        boolean isNew = id == null;
        runAsync(() -> {
            if (isNew) {
                return api.create(request).getId();
            }
            api.update(id, request);
            return id;
        }, savedId -> {
            showSuccess(isNew ? "Пользователь создан" : "Данные пользователя сохранены");
            loadUsers(savedId);
        });
    }

    @FXML
    private void onClear() {
        editingId = null;
        suppressSelection = true;
        table.getSelectionModel().clearSelection();
        suppressSelection = false;
        usernameField.clear();
        passwordField.clear();
        fullNameField.clear();
        emailField.clear();
        roleCombo.setValue(null);
        activeCheck.setSelected(true);
        clearMessage();
    }

    private void fillForm(UserDto user) {
        editingId = user.getId();
        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        roleCombo.setValue(user.getRole());
        activeCheck.setSelected(user.isActive());
        passwordField.clear();
        clearMessage();
    }

    private void loadUsers(Long selectId) {
        int generation = nextLoadGeneration();
        setBusy(true);
        FxTasks.run(
                () -> api.findAll(),
                (List<UserDto> users) -> {
                    if (!isLatestLoad(generation)) return;
                    setBusy(false);
                    suppressSelection = true;
                    table.setItems(FXCollections.observableArrayList(users));
                    if (selectId != null) {
                        users.stream().filter(u -> selectId.equals(u.getId())).findFirst()
                                .ifPresent(u -> table.getSelectionModel().select(u));
                    }
                    suppressSelection = false;
                },
                error -> {
                    if (isLatestLoad(generation)) {
                        setBusy(false);
                        showError(errorText(error));
                    }
                }
        );
    }
}
