package ru.servicecenter.client.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.servicecenter.client.dto.ClientDto;
import ru.servicecenter.client.service.ClientApiService;
import ru.servicecenter.client.session.Session;
import ru.servicecenter.client.util.FormValidator;
import ru.servicecenter.client.util.FxTasks;
import ru.servicecenter.client.util.TableStyles;

import java.util.List;
import java.util.Optional;

public class ClientsController extends BaseViewController implements Refreshable {

    @FXML private Label messageLabel;
    @FXML private Label pageSubtitleLabel;
    @FXML private TextField searchField;
    @FXML private Button btnSave;
    @FXML private Button btnNew;
    @FXML private Button btnDelete;
    @FXML private TableView<ClientDto> table;
    @FXML private TableColumn<ClientDto, Long> colId;
    @FXML private TableColumn<ClientDto, String> colName, colPhone, colEmail, colAddress;
    @FXML private TextField fullNameField, phoneField, emailField, addressField;
    @FXML private TextArea notesField;

    private final ClientApiService api = new ClientApiService();
    private Long editingId;
    private boolean suppressSelection;

    @FXML
    private void initialize() {
        bindMessageLabel(messageLabel);
        TableStyles.apply(table);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        applyLeftAlign(colName, colPhone, colEmail, colAddress);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (!suppressSelection && selected != null && Session.isAdmin()) {
                fillForm(selected);
            }
        });
        applyRoleUi();
    }

    private void applyRoleUi() {
        boolean admin = Session.isAdmin();
        boolean manager = Session.isManager();

        btnDelete.setVisible(admin);
        btnDelete.setManaged(admin);

        if (admin) {
            pageSubtitleLabel.setText("Регистрация, редактирование и удаление клиентов");
            btnSave.setText("Сохранить");
        } else if (manager) {
            pageSubtitleLabel.setText("Добавление новых клиентов. Редактирование и удаление доступны только администратору.");
            btnSave.setText("Добавить клиента");
        }
    }

    private void setFormEditable(boolean editable) {
        fullNameField.setDisable(!editable);
        phoneField.setDisable(!editable);
        emailField.setDisable(!editable);
        addressField.setDisable(!editable);
        notesField.setDisable(!editable);
        btnSave.setDisable(!editable);
    }

    @Override
    public void refresh() {
        loadData(searchField.getText(), editingId);
    }

    @Override
    protected Node[] busyNodes() {
        return new Node[]{table};
    }

    @FXML
    private void onSearch() {
        loadData(searchField.getText(), null);
    }

    @FXML
    private void onAdd() {
        editingId = null;
        onClear();
        setFormEditable(true);
    }

    @FXML
    private void onEdit() {
        ClientDto selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            fillForm(selected);
        }
    }

    @FXML
    private void onDelete() {
        if (!Session.isAdmin()) {
            showError("Удалять клиентов может только администратор");
            return;
        }
        ClientDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите клиента в таблице");
            return;
        }
        runAsync(() -> {
            api.delete(selected.getId());
            return null;
        }, () -> {
            editingId = null;
            onClear();
            showSuccess("Клиент удалён");
            loadData(searchField.getText(), null);
        });
    }

    @FXML
    private void onSave() {
        if (Session.isManager() && editingId != null) {
            showError("Менеджер может только добавлять новых клиентов. Нажмите «Новый».");
            return;
        }
        clearMessage();
        ClientDto dto = new ClientDto();
        dto.setFullName(fullNameField.getText().trim());
        dto.setPhone(phoneField.getText().trim());
        dto.setEmail(emailField.getText().trim());
        dto.setAddress(addressField.getText().trim());
        dto.setNotes(notesField.getText());

        if (!FormValidator.check(FormValidator.fullName(dto.getFullName()), messageLabel)
                || !FormValidator.check(FormValidator.phone(dto.getPhone()), messageLabel)
                || !FormValidator.check(FormValidator.emailOptional(dto.getEmail()), messageLabel)) {
            return;
        }
        if (dto.getAddress() != null && dto.getAddress().length() > 500) {
            showError("Адрес: не более 500 символов");
            return;
        }

        Long id = editingId;
        boolean isNew = id == null;
        runAsync(() -> {
            if (isNew) {
                return api.create(ClientApiService.toBody(dto)).getId();
            }
            api.update(id, ClientApiService.toBody(dto));
            return id;
        }, savedId -> {
            showSuccess(isNew ? "Клиент добавлен" : "Данные клиента сохранены");
            loadData(searchField.getText(), savedId);
        });
    }

    @FXML
    private void onClear() {
        editingId = null;
        suppressSelection = true;
        table.getSelectionModel().clearSelection();
        suppressSelection = false;
        fullNameField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
        notesField.clear();
        setFormEditable(Session.isAdmin() || Session.isManager());
        clearMessage();
    }

    private void fillForm(ClientDto client) {
        editingId = client.getId();
        fullNameField.setText(client.getFullName());
        phoneField.setText(client.getPhone());
        emailField.setText(Optional.ofNullable(client.getEmail()).orElse(""));
        addressField.setText(Optional.ofNullable(client.getAddress()).orElse(""));
        notesField.setText(Optional.ofNullable(client.getNotes()).orElse(""));
        setFormEditable(Session.isAdmin());
        clearMessage();
    }

    private void loadData(String search, Long selectId) {
        int generation = nextLoadGeneration();
        setBusy(true);
        FxTasks.run(
                () -> api.findAll(search),
                (List<ClientDto> list) -> {
                    if (!isLatestLoad(generation)) return;
                    setBusy(false);
                    suppressSelection = true;
                    table.setItems(FXCollections.observableArrayList(list));
                    if (selectId != null) {
                        list.stream().filter(c -> selectId.equals(c.getId())).findFirst()
                                .ifPresent(c -> table.getSelectionModel().select(c));
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

    @SafeVarargs
    private void applyLeftAlign(TableColumn<ClientDto, ?>... columns) {
        for (TableColumn<ClientDto, ?> col : columns) {
            col.setCellFactory(TableStyles.leftAlignedCell());
        }
    }
}
