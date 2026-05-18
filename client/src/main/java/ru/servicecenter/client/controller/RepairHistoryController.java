package ru.servicecenter.client.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.servicecenter.client.dto.RepairDto;
import ru.servicecenter.client.dto.UserDto;
import ru.servicecenter.client.service.RepairApiService;
import ru.servicecenter.client.service.UserApiService;
import ru.servicecenter.client.session.Session;
import ru.servicecenter.client.util.FxTasks;
import ru.servicecenter.client.util.TableStyles;

import java.util.Comparator;
import java.util.List;
public class RepairHistoryController extends BaseViewController implements Refreshable {

    @FXML private Label roleHintLabel;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<UserDto> masterFilter;
    @FXML private Button btnDelete;
    @FXML private TableView<RepairDto> archiveTable;
    @FXML private TableColumn<RepairDto, String> colNumber, colClient, colMaster, colDeviceType, colProblem, colStatus, colCost;
    @FXML private Label detailTitle, detailClient, detailMaster, detailType, detailModel, detailProblem, detailDiagnosis, detailStatus, detailCost;

    private final RepairApiService repairApi = new RepairApiService();
    private final UserApiService userApi = new UserApiService();
    private List<UserDto> masters = List.of();

    @FXML
    private void initialize() {
        bindMessageLabel(messageLabel);
        TableStyles.apply(archiveTable);
        setupColumns();
        applyRoleUi();

        statusFilter.setItems(FXCollections.observableArrayList("ALL", "COMPLETED", "CANCELED"));
        statusFilter.setValue("ALL");
        statusFilter.valueProperty().addListener((o, a, b) -> loadArchive());
        searchField.textProperty().addListener((o, a, b) -> {
            if (b == null || b.length() == 0 || b.length() >= 2) {
                loadArchive();
            }
        });
        masterFilter.valueProperty().addListener((o, a, b) -> loadArchive());

        archiveTable.getSelectionModel().selectedItemProperty().addListener((o, a, repair) -> {
            if (repair != null) {
                showDetail(repair);
            }
        });

    }

    @Override
    public void refresh() {
        if (Session.isMaster()) {
            loadArchive();
        } else {
            loadMastersIfNeeded();
        }
    }

    @Override
    protected Node[] busyNodes() {
        return new Node[]{archiveTable, btnDelete};
    }

    @FXML
    private void onRefresh() {
        loadArchive();
    }

    @FXML
    private void onDelete() {
        if (!Session.isAdmin()) {
            showError("Удалять архивные заявки может только администратор");
            return;
        }
        RepairDto selected = archiveTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите заявку в таблице");
            return;
        }
        runAsync(() -> {
            repairApi.delete(selected.getId());
            return null;
        }, () -> {
            showSuccess("Заявка удалена из архива");
            loadArchive();
        });
    }

    private void applyRoleUi() {
        boolean master = Session.isMaster();
        boolean admin = Session.isAdmin();

        btnDelete.setVisible(admin);
        btnDelete.setManaged(admin);

        masterFilter.setVisible(!master);
        masterFilter.setManaged(!master);

        setColumnVisible(colNumber, true);
        setColumnVisible(colDeviceType, true);
        setColumnVisible(colProblem, true);
        setColumnVisible(colStatus, true);
        setColumnVisible(colClient, !master);
        setColumnVisible(colMaster, !master);
        setColumnVisible(colCost, !master);

        if (master) {
            roleHintLabel.setText("Ваши завершённые и отменённые заявки.");
        } else if (Session.isManager()) {
            roleHintLabel.setText("Архив всех мастеров. Фильтр по мастеру и статусу.");
        } else {
            roleHintLabel.setText("Архив заявок. Администратор может удалять записи.");
        }
    }

    private void loadMastersIfNeeded() {
        if (Session.isMaster()) {
            return;
        }
        FxTasks.run(
                () -> userApi.findMasters(),
                list -> {
                    masters = list;
                    UserDto all = new UserDto();
                    all.setId(null);
                    all.setFullName("Все мастера");
                    masterFilter.getItems().setAll(all);
                    masterFilter.getItems().addAll(list);
                    masterFilter.setValue(all);
                    masterFilter.setCellFactory(lv -> new ListCell<>() {
                        @Override
                        protected void updateItem(UserDto item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? null : item.getFullName());
                        }
                    });
                    masterFilter.setButtonCell(new ListCell<>() {
                        @Override
                        protected void updateItem(UserDto item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? "Все мастера" : item.getFullName());
                        }
                    });
                    loadArchive();
                },
                this::showErrorUnlessAccessDenied
        );
    }

    private void loadArchive() {
        if (Session.isMaster() && Session.getUserId() == null) {
            return;
        }

        String status = statusFilter.getValue();
        String search = searchField.getText();
        Long masterId = Session.isMaster() ? Session.getUserId() : null;
        if (!Session.isMaster()) {
            UserDto selectedMaster = masterFilter.getValue();
            if (selectedMaster != null && selectedMaster.getId() != null) {
                masterId = selectedMaster.getId();
            }
        }

        Long finalMasterId = masterId;
        int gen = nextLoadGeneration();
        setBusy(true);
        FxTasks.run(
                () -> repairApi.findAll(true, status, finalMasterId, search),
                repairs -> {
                    if (!isLatestLoad(gen)) {
                        return;
                    }
                    setBusy(false);
                    repairs.sort(Comparator.comparing(RepairDto::getRequestNumber).reversed());
                    archiveTable.setItems(FXCollections.observableArrayList(repairs));
                    if (repairs.isEmpty()) {
                        clearDetail();
                    }
                },
                e -> {
                    if (isLatestLoad(gen)) {
                        setBusy(false);
                        showErrorUnlessAccessDenied(e);
                    }
                }
        );
    }

    private void showDetail(RepairDto r) {
        detailTitle.setText("Заявка " + r.getRequestNumber());
        detailClient.setText(nullToDash(r.getClientName()));
        detailMaster.setText(nullToDash(r.getMasterName()));
        detailType.setText(nullToDash(r.getDeviceType()));
        String model = (r.getDeviceBrand() != null ? r.getDeviceBrand() : "")
                + (r.getDeviceModel() != null ? " " + r.getDeviceModel() : "");
        detailModel.setText(model.isBlank() ? "—" : model.trim());
        detailProblem.setText(nullToDash(r.getProblemDescription()));
        detailDiagnosis.setText(nullToDash(r.getDiagnosis()));
        detailStatus.setText(formatStatus(r.getStatus()));
        detailCost.setText(formatCost(r));
    }

    private void clearDetail() {
        detailTitle.setText("Архив пуст");
        detailClient.setText("—");
        detailMaster.setText("—");
        detailType.setText("—");
        detailModel.setText("—");
        detailProblem.setText("—");
        detailDiagnosis.setText("—");
        detailStatus.setText("—");
        detailCost.setText("—");
    }

    private void setupColumns() {
        colNumber.setCellValueFactory(new PropertyValueFactory<>("requestNumber"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colMaster.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getMasterName() != null ? c.getValue().getMasterName() : "—"));
        colDeviceType.setCellValueFactory(new PropertyValueFactory<>("deviceType"));
        colProblem.setCellValueFactory(new PropertyValueFactory<>("problemDescription"));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(formatStatus(c.getValue().getStatus())));
        colCost.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(formatCost(c.getValue())));
        for (var col : new TableColumn[]{colNumber, colClient, colMaster, colDeviceType, colProblem, colStatus, colCost}) {
            col.setCellFactory(TableStyles.leftAlignedCell());
            col.setComparator((a, b) -> String.valueOf(a).compareToIgnoreCase(String.valueOf(b)));
        }
    }

    private void setColumnVisible(TableColumn<RepairDto, ?> col, boolean visible) {
        col.setVisible(visible);
    }

    private static String nullToDash(String v) {
        return v == null || v.isBlank() ? "—" : v;
    }

    private static String formatStatus(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "COMPLETED" -> "Завершена";
            case "CANCELED" -> "Отменена";
            default -> status;
        };
    }

    private static String formatCost(RepairDto r) {
        if (r.getFinalCost() != null) {
            return r.getFinalCost() + " ₽";
        }
        if (r.getEstimatedCost() != null) {
            return "~" + r.getEstimatedCost() + " ₽";
        }
        return "—";
    }
}
