package ru.servicecenter.client.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.servicecenter.client.dto.*;
import ru.servicecenter.client.service.CatalogApiService;
import ru.servicecenter.client.service.ClientApiService;
import ru.servicecenter.client.service.DeviceApiService;
import ru.servicecenter.client.service.RepairApiService;
import ru.servicecenter.client.service.UserApiService;
import ru.servicecenter.client.session.Session;
import ru.servicecenter.client.util.ClientComboHelper;
import ru.servicecenter.client.util.DeviceComboHelper;
import ru.servicecenter.client.util.FxTasks;
import ru.servicecenter.client.util.FormValidator;
import ru.servicecenter.client.util.TableStyles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RepairsController extends BaseViewController implements Refreshable {

    @FXML private Label roleHintLabel;
    @FXML private Label messageLabel;
    @FXML private TextArea historyArea;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button btnNew;
    @FXML private TableView<RepairDto> repairTable;
    @FXML private TableColumn<RepairDto, String> colNumber, colClient, colDeviceType, colDeviceBrand, colDeviceModel, colSerial, colProblem, colStatus, colCost;

    @FXML private Label formTitleLabel;
    @FXML private VBox repairSummarySection;
    @FXML private Label summaryTitle, summaryClient, summaryMaster, summaryType, summaryModel;
    @FXML private Label summaryProblem, summaryDiagnosis, summaryStatus, summaryCost;
    @FXML private Separator summarySeparator;
    @FXML private Label editFormLabel;
    @FXML private VBox clientSection;
    @FXML private VBox deviceViewSection;
    @FXML private VBox deviceEditSection;
    @FXML private Label viewDeviceType, viewDeviceBrand, viewDeviceModel, viewDeviceSerial;
    @FXML private ComboBox<ClientDto> clientCombo;
    @FXML private CheckBox newDeviceCheck;
    @FXML private ComboBox<DeviceDto> deviceCombo;
    @FXML private ComboBox<CatalogItemDto> deviceTypeCombo;
    @FXML private ComboBox<CatalogItemDto> deviceBrandCombo;
    @FXML private HBox adminAddTypeRow;
    @FXML private TextField adminNewTypeField;
    @FXML private HBox managerAddBrandRow;
    @FXML private TextField inlineBrandField;
    @FXML private HBox adminAddBrandRow;
    @FXML private TextField adminNewBrandField;
    @FXML private TextField deviceModelField, deviceSerialField;
    @FXML private TextArea problemField, diagnosisField, statusCommentField;
    @FXML private TextField estimatedCostField, finalCostField;
    @FXML private VBox estimatedCostSection;
    @FXML private VBox masterSection;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<UserDto> masterCombo;
    @FXML private Button btnSave, btnHistory;

    private final RepairApiService repairApi = new RepairApiService();
    private final ClientApiService clientApi = new ClientApiService();
    private final DeviceApiService deviceApi = new DeviceApiService();
    private final UserApiService userApi = new UserApiService();
    private final CatalogApiService catalogApi = new CatalogApiService();

    private List<ClientDto> clients = List.of();
    private List<UserDto> masters = List.of();
    private List<CatalogItemDto> deviceTypes = List.of();
    private List<CatalogItemDto> brands = List.of();
    private Long editingRepairId;
    private boolean suppressSelection;

    @FXML
    private void initialize() {
        bindMessageLabel(messageLabel);
        TableStyles.apply(repairTable);
        setupRepairTableColumns();
        setupCombos();
        applyRoleUi();
        setupListeners();
        statusFilter.valueProperty().addListener((obs, o, n) -> loadRepairs());
        searchField.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.length() == 0 || n.length() >= 2) {
                loadRepairs();
            }
        });
        if (Session.isMaster()) {
            editingRepairId = null;
            clearForm();
            formTitleLabel.setText("Выберите заявку в таблице");
            setFormMode(false);
        } else {
            onNew();
        }

        repairTable.getSelectionModel().selectedItemProperty().addListener((obs, o, repair) -> {
            if (!suppressSelection && repair != null) {
                openRepair(repair);
            }
        });

        newDeviceCheck.selectedProperty().addListener((obs, o, isNew) -> updateDeviceInputsMode(isNew));
        updateDeviceInputsMode(false);
    }

    @Override
    public void refresh() {
        if (!Session.isMaster()) {
            loadReferenceData();
        }
        if (needsCatalogData()) {
            loadCatalogs();
        }
        loadRepairs();
    }

    @Override
    protected Node[] busyNodes() {
        return new Node[]{repairTable, btnSave};
    }

    @FXML
    private void onRefresh() {
        if (needsCatalogData()) {
            loadCatalogs();
        }
        loadRepairs();
    }

    @FXML
    private void onNew() {
        editingRepairId = null;
        suppressSelection = true;
        repairTable.getSelectionModel().clearSelection();
        suppressSelection = false;
        clearForm();
        formTitleLabel.setText("Новая заявка");
        setFormMode(true);
    }

    @FXML
    private void onSave() {
        if (editingRepairId == null) {
            createRepair();
        } else {
            updateExistingRepair();
        }
    }

    @FXML
    private void onAddDeviceType() {
        if (!Session.isAdmin()) {
            showError("Добавлять типы устройств может только администратор");
            return;
        }
        String name = adminNewTypeField.getText().trim();
        if (name.isBlank()) {
            showError("Введите название типа устройства");
            return;
        }
        FxTasks.run(
                () -> catalogApi.createDeviceType(name),
                created -> {
                    adminNewTypeField.clear();
                    loadCatalogs();
                    selectInCombo(deviceTypeCombo, created);
                    showSuccess("Тип добавлен: " + created.getName());
                },
                e -> showErrorUnlessAccessDenied(e)
        );
    }

    @FXML
    private void onAddBrand() {
        if (!Session.isAdmin() && !Session.isManager()) {
            return;
        }
        String name = readNewBrandName();
        if (name.isBlank()) {
            showError("Введите название бренда");
            return;
        }
        FxTasks.run(
                () -> catalogApi.createBrand(name),
                created -> {
                    inlineBrandField.clear();
                    adminNewBrandField.clear();
                    loadCatalogs();
                    selectInCombo(deviceBrandCombo, created);
                    showSuccess("Бренд добавлен: " + created.getName());
                },
                e -> showErrorUnlessAccessDenied(e)
        );
    }

    private String readNewBrandName() {
        if (Session.isManager() && inlineBrandField.getText() != null && !inlineBrandField.getText().isBlank()) {
            return inlineBrandField.getText().trim();
        }
        if (Session.isAdmin() && adminNewBrandField.getText() != null && !adminNewBrandField.getText().isBlank()) {
            return adminNewBrandField.getText().trim();
        }
        return "";
    }

    private boolean needsCatalogData() {
        return Session.isAdmin() || Session.isManager();
    }

    @FXML
    private void onShowHistory() {
        if (editingRepairId == null) {
            showError("Выберите заявку в таблице");
            return;
        }
        FxTasks.run(
                () -> repairApi.getHistory(editingRepairId),
                history -> {
                    String text = history.stream()
                            .map(h -> String.format("%s → %s | %s | %s",
                                    h.getOldStatus() != null ? formatStatus(h.getOldStatus()) : "—",
                                    formatStatus(h.getNewStatus()),
                                    h.getChangedByName() != null ? h.getChangedByName() : "система",
                                    h.getComment() != null ? h.getComment() : ""))
                            .collect(Collectors.joining("\n"));
                    historyArea.setText(text.isEmpty() ? "История пуста" : text);
                    historyArea.setVisible(true);
                    historyArea.setManaged(true);
                    showInfo("История загружена в блок ниже");
                },
                e -> showErrorUnlessAccessDenied(e)
        );
    }

    private void createRepair() {
        if (!Session.isAdmin() && !Session.isManager()) {
            showError("Создавать заявки могут администратор и менеджер");
            return;
        }
        if (!validateCreateForm()) {
            return;
        }
        ClientDto client = clientCombo.getValue();
        UserDto master = masterCombo.getValue();

        runAsync(() -> {
            Long deviceId = resolveDeviceId(client);
            Map<String, Object> body = new HashMap<>();
            body.put("clientId", client.getId());
            body.put("deviceId", deviceId);
            body.put("problemDescription", problemField.getText().trim());
            if (master != null) {
                body.put("masterId", master.getId());
            }
            if (!estimatedCostField.getText().isBlank()) {
                body.put("estimatedCost", new BigDecimal(estimatedCostField.getText().trim().replace(',', '.')));
            }
            repairApi.create(body);
            return null;
        }, () -> {
            showSuccess(master != null ? "Заявка создана, мастер назначен" : "Заявка успешно создана");
            loadRepairs();
            onNew();
        });
    }

    private boolean validateCreateForm() {
        clearMessage();
        if (!FormValidator.check(Optional.ofNullable(
                clientCombo.getValue() == null ? "Выберите клиента из списка" : null), messageLabel)) {
            return false;
        }
        if (!FormValidator.check(FormValidator.problemDescription(problemField.getText()), messageLabel)) {
            return false;
        }
        if (!FormValidator.check(FormValidator.moneyOptional(estimatedCostField.getText(), "Ориентировочная стоимость"), messageLabel)) {
            return false;
        }
        if (newDeviceCheck.isSelected()) {
            if (!FormValidator.check(Optional.ofNullable(
                    deviceTypeCombo.getValue() == null ? "Выберите тип устройства" : null), messageLabel)) {
                return false;
            }
            if (!FormValidator.check(Optional.ofNullable(
                    deviceBrandCombo.getValue() == null ? "Выберите бренд или добавьте новый" : null), messageLabel)) {
                return false;
            }
            return FormValidator.check(FormValidator.deviceField(deviceModelField.getText(), "Модель"), messageLabel)
                    && FormValidator.check(FormValidator.serialOptional(deviceSerialField.getText()), messageLabel);
        }
        if (!FormValidator.check(Optional.ofNullable(
                deviceCombo.getValue() == null ? "Выберите технику или отметьте «Зарегистрировать новую технику»" : null), messageLabel)) {
            return false;
        }
        return true;
    }

    private Long resolveDeviceId(ClientDto client) throws Exception {
        if (newDeviceCheck.isSelected()) {
            DeviceDto dto = new DeviceDto();
            dto.setClientId(client.getId());
            dto.setDeviceType(deviceTypeCombo.getValue().getName());
            dto.setBrand(deviceBrandCombo.getValue().getName());
            dto.setModel(deviceModelField.getText().trim());
            dto.setSerialNumber(deviceSerialField.getText().trim());
            DeviceDto created = deviceApi.create(DeviceApiService.toBody(dto));
            return created.getId();
        }
        return deviceCombo.getValue().getId();
    }

    private void updateExistingRepair() {
        if (!validateUpdateForm()) {
            return;
        }
        String status = statusCombo.getValue();
        Long repairId = editingRepairId;
        UserDto master = masterCombo.getValue();
        boolean archiving = "COMPLETED".equals(status) || "CANCELED".equals(status);

        runAsync(() -> {
            BigDecimal finalCost = null;
            if (!finalCostField.getText().isBlank()) {
                finalCost = new BigDecimal(finalCostField.getText().trim().replace(',', '.'));
            }
            Map<String, Object> statusBody = RepairApiService.statusBody(
                    status,
                    statusCommentField.getText(),
                    diagnosisField.getText().isBlank() ? null : diagnosisField.getText().trim(),
                    finalCost
            );
            repairApi.updateStatus(repairId, statusBody);

            if (!Session.isMaster() && master != null) {
                repairApi.assignMaster(repairId, master.getId());
            }
            return null;
        }, () -> {
            statusCommentField.clear();
            if (archiving) {
                showSuccess("Заявка перенесена в «Историю ремонта»");
                editingRepairId = null;
                clearForm();
                formTitleLabel.setText("Выберите заявку в таблице");
                loadRepairs();
            } else {
                showSuccess("Изменения сохранены");
                loadRepairs();
            }
        });
    }

    private void openRepair(RepairDto repair) {
        editingRepairId = repair.getId();
        formTitleLabel.setText("Заявка " + repair.getRequestNumber());
        fillRepairSummary(repair);
        setFormMode(false);

        clients.stream().filter(c -> c.getId().equals(repair.getClientId())).findFirst()
                .ifPresent(clientCombo::setValue);

        problemField.setText(repair.getProblemDescription() != null ? repair.getProblemDescription() : "");
        diagnosisField.setText(repair.getDiagnosis() != null ? repair.getDiagnosis() : "");
        estimatedCostField.setText(repair.getEstimatedCost() != null ? repair.getEstimatedCost().toString() : "");
        finalCostField.setText(repair.getFinalCost() != null ? repair.getFinalCost().toString() : "");
        statusCombo.setValue(repair.getStatus());

        masters.stream().filter(m -> repair.getMasterId() != null && m.getId().equals(repair.getMasterId()))
                .findFirst().ifPresent(masterCombo::setValue);

        String type = nullToDash(repair.getDeviceType());
        String brand = nullToDash(repair.getDeviceBrand());
        String model = nullToDash(repair.getDeviceModel());
        viewDeviceType.setText(type);
        viewDeviceBrand.setText(brand);
        viewDeviceModel.setText(model);
        viewDeviceSerial.setText(nullToDash(repair.getDeviceSerial()));

        selectInCombo(deviceTypeCombo, findByName(deviceTypes, repair.getDeviceType()));
        selectInCombo(deviceBrandCombo, findByName(brands, repair.getDeviceBrand()));
        deviceModelField.setText("—".equals(model) ? "" : model);
        deviceSerialField.setText(repair.getDeviceSerial() != null ? repair.getDeviceSerial() : "");

        if (repair.getClientId() != null && !Session.isMaster()) {
            FxTasks.run(
                    () -> deviceApi.findAll(repair.getClientId()),
                    devices -> {
                        deviceCombo.setItems(FXCollections.observableArrayList(devices));
                        devices.stream().filter(d -> d.getId().equals(repair.getDeviceId())).findFirst()
                                .ifPresent(deviceCombo::setValue);
                    },
                    this::showErrorUnlessAccessDenied
            );
        }
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private boolean validateUpdateForm() {
        clearMessage();
        if (!FormValidator.check(FormValidator.status(statusCombo.getValue()), messageLabel)) {
            return false;
        }
        if (Session.isMaster() && !FormValidator.check(FormValidator.diagnosisOptional(diagnosisField.getText()), messageLabel)) {
            return false;
        }
        return FormValidator.check(FormValidator.moneyOptional(finalCostField.getText(), "Итоговая стоимость"), messageLabel);
    }

    private void clearForm() {
        clientCombo.setValue(null);
        newDeviceCheck.setSelected(false);
        deviceCombo.getItems().clear();
        deviceCombo.setValue(null);
        deviceTypeCombo.setValue(null);
        deviceBrandCombo.setValue(null);
        deviceModelField.clear();
        deviceSerialField.clear();
        adminNewTypeField.clear();
        inlineBrandField.clear();
        adminNewBrandField.clear();
        viewDeviceType.setText("—");
        viewDeviceBrand.setText("—");
        viewDeviceModel.setText("—");
        viewDeviceSerial.setText("—");
        problemField.clear();
        diagnosisField.clear();
        estimatedCostField.clear();
        finalCostField.clear();
        statusCombo.setValue("NEW");
        statusCommentField.clear();
        masterCombo.setValue(null);
        if (historyArea != null) {
            historyArea.clear();
            historyArea.setVisible(false);
            historyArea.setManaged(false);
        }
        hideRepairSummary();
        clearMessage();
    }

    private void fillRepairSummary(RepairDto repair) {
        summaryTitle.setText("Заявка " + repair.getRequestNumber());
        summaryClient.setText(nullToDash(repair.getClientName()));
        summaryMaster.setText(nullToDash(repair.getMasterName()));
        summaryType.setText(nullToDash(repair.getDeviceType()));
        String brandModel = formatBrandModel(repair);
        summaryModel.setText(brandModel);
        summaryProblem.setText(nullToDash(repair.getProblemDescription()));
        summaryDiagnosis.setText(nullToDash(repair.getDiagnosis()));
        summaryStatus.setText(formatStatus(repair.getStatus()));
        String cost = formatCost(repair);
        summaryCost.setText(cost.isBlank() ? "—" : cost);
    }

    private void hideRepairSummary() {
        repairSummarySection.setVisible(false);
        repairSummarySection.setManaged(false);
        summarySeparator.setVisible(false);
        summarySeparator.setManaged(false);
        editFormLabel.setVisible(false);
        editFormLabel.setManaged(false);
    }

    private static String formatBrandModel(RepairDto repair) {
        String brand = repair.getDeviceBrand();
        String model = repair.getDeviceModel();
        if (brand != null && !brand.isBlank() && model != null && !model.isBlank()) {
            return brand + " " + model;
        }
        if (brand != null && !brand.isBlank()) {
            return brand;
        }
        if (model != null && !model.isBlank()) {
            return model;
        }
        if (repair.getDeviceInfo() != null && !repair.getDeviceInfo().isBlank()) {
            return repair.getDeviceInfo();
        }
        return "—";
    }

    private void setFormMode(boolean createMode) {
        boolean canCreate = Session.isAdmin() || Session.isManager();
        boolean isMaster = Session.isMaster();
        boolean showSummary = !createMode && !isMaster && canCreate;

        formTitleLabel.setVisible(!showSummary);
        formTitleLabel.setManaged(!showSummary);
        repairSummarySection.setVisible(showSummary);
        repairSummarySection.setManaged(showSummary);
        summarySeparator.setVisible(showSummary);
        summarySeparator.setManaged(showSummary);
        editFormLabel.setVisible(showSummary);
        editFormLabel.setManaged(showSummary);

        if (createMode) {
            hideRepairSummary();
            formTitleLabel.setVisible(true);
            formTitleLabel.setManaged(true);
        }

        clientSection.setVisible(!isMaster);
        clientSection.setManaged(!isMaster);
        deviceEditSection.setVisible(!isMaster || createMode);
        deviceEditSection.setManaged(!isMaster || createMode);
        deviceViewSection.setVisible(isMaster && !createMode);
        deviceViewSection.setManaged(isMaster && !createMode);

        masterSection.setVisible(!isMaster);
        masterSection.setManaged(!isMaster);
        estimatedCostSection.setVisible(!isMaster);
        estimatedCostSection.setManaged(!isMaster);

        boolean showAdminType = Session.isAdmin() && createMode && canCreate;
        adminAddTypeRow.setVisible(showAdminType);
        adminAddTypeRow.setManaged(showAdminType);

        boolean showManagerBrand = Session.isManager() && createMode && canCreate;
        managerAddBrandRow.setVisible(showManagerBrand);
        managerAddBrandRow.setManaged(showManagerBrand);

        boolean showAdminBrand = Session.isAdmin() && createMode && canCreate;
        adminAddBrandRow.setVisible(showAdminBrand);
        adminAddBrandRow.setManaged(showAdminBrand);

        clientCombo.setDisable(!createMode || !canCreate);
        newDeviceCheck.setDisable(!createMode || !canCreate);
        deviceCombo.setDisable(!createMode || !canCreate);
        deviceTypeCombo.setDisable(!createMode || !canCreate);
        deviceBrandCombo.setDisable(!createMode || !canCreate);
        deviceModelField.setDisable(!createMode || !canCreate);
        deviceSerialField.setDisable(!createMode || !canCreate);
        masterCombo.setDisable(isMaster);

        boolean creating = createMode && canCreate;
        problemField.setDisable(!creating && !isMaster);
        if (isMaster && !createMode) {
            problemField.setDisable(true);
        }
        estimatedCostField.setDisable(!creating);
        diagnosisField.setDisable(!isMaster || createMode);
        finalCostField.setDisable(createMode);
        statusCombo.setDisable(createMode);
        statusCommentField.setDisable(false);
        btnSave.setDisable(createMode && !canCreate);
        btnSave.setText(createMode ? "Создать заявку" : "Сохранить изменения");

        if (!createMode) {
            updateDeviceInputsMode(false);
        }
    }

    private void updateDeviceInputsMode(boolean newDevice) {
        if (editingRepairId != null || Session.isMaster()) {
            return;
        }
        deviceCombo.setDisable(newDevice);
        deviceTypeCombo.setDisable(!newDevice);
        deviceBrandCombo.setDisable(!newDevice);
        deviceModelField.setDisable(!newDevice);
        deviceSerialField.setDisable(!newDevice);
        if (newDevice) {
            deviceCombo.setValue(null);
        }
    }

    private void setupListeners() {
        clientCombo.valueProperty().addListener((obs, o, client) -> {
            if (client == null || editingRepairId != null) {
                return;
            }
            FxTasks.run(
                    () -> deviceApi.findAll(client.getId()),
                    devices -> deviceCombo.setItems(FXCollections.observableArrayList(devices)),
                    this::showErrorUnlessAccessDenied
            );
        });

        deviceCombo.valueProperty().addListener((obs, o, device) -> {
            if (device != null && !newDeviceCheck.isSelected() && editingRepairId == null) {
                selectInCombo(deviceTypeCombo, findByName(deviceTypes, device.getDeviceType()));
                selectInCombo(deviceBrandCombo, findByName(brands, device.getBrand()));
                deviceModelField.setText(device.getModel());
                deviceSerialField.setText(device.getSerialNumber() != null ? device.getSerialNumber() : "");
            }
        });
    }

    private void setupCombos() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "ALL", "NEW", "IN_PROGRESS", "WAITING_PARTS"));
        statusFilter.setValue("ALL");
        statusCombo.setItems(FXCollections.observableArrayList(
                "NEW", "IN_PROGRESS", "WAITING_PARTS", "COMPLETED", "CANCELED"));
        ClientComboHelper.setup(clientCombo, "Выберите клиента");
        DeviceComboHelper.setup(deviceCombo);
        deviceTypeCombo.setPromptText("Выберите тип");
        deviceBrandCombo.setPromptText("Выберите бренд");
    }

    private void setupRepairTableColumns() {
        colNumber.setCellValueFactory(new PropertyValueFactory<>("requestNumber"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colDeviceType.setCellValueFactory(new PropertyValueFactory<>("deviceType"));
        colDeviceBrand.setCellValueFactory(new PropertyValueFactory<>("deviceBrand"));
        colDeviceModel.setCellValueFactory(new PropertyValueFactory<>("deviceModel"));
        colSerial.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDeviceSerial() != null ? c.getValue().getDeviceSerial() : "—"));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(formatStatus(c.getValue().getStatus())));
        colProblem.setCellValueFactory(new PropertyValueFactory<>("problemDescription"));
        colCost.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(formatCost(c.getValue())));
        applyLeftAlignRepair(colNumber, colClient, colDeviceType, colDeviceBrand, colDeviceModel, colSerial, colStatus, colProblem, colCost);
        configureRepairColumnsForRole();
    }

    private void configureRepairColumnsForRole() {
        boolean master = Session.isMaster();
        setColumnVisible(colNumber, true);
        setColumnVisible(colDeviceType, master);
        setColumnVisible(colProblem, master);
        setColumnVisible(colStatus, true);
        setColumnVisible(colClient, !master);
        setColumnVisible(colDeviceBrand, !master);
        setColumnVisible(colDeviceModel, !master);
        setColumnVisible(colSerial, !master);
        setColumnVisible(colCost, !master);
    }

    private void setColumnVisible(TableColumn<RepairDto, ?> column, boolean visible) {
        column.setVisible(visible);
    }

    private void applyRoleUi() {
        configureRepairColumnsForRole();

        if (Session.isAdmin()) {
            roleHintLabel.setText("Администратор: активные заявки. Завершённые — во вкладке «История ремонта».");
        } else if (Session.isManager()) {
            roleHintLabel.setText("Менеджер: активные заявки. Завершённые и отменённые — в «Истории ремонта».");
        } else if (Session.isMaster()) {
            roleHintLabel.setText("Мастер: активные назначенные заявки. После «Завершена»/«Отменена» — в «Истории ремонта».");
            btnNew.setVisible(false);
            btnNew.setManaged(false);
        }
    }

    private void loadReferenceData() {
        FxTasks.run(
                () -> new RefData(clientApi.findAll(null), userApi.findMasters()),
                data -> {
                    clients = data.clients();
                    masters = data.masters();
                    clientCombo.setItems(FXCollections.observableArrayList(clients));
                    masterCombo.setItems(FXCollections.observableArrayList(masters));
                    setupMasterComboDisplay();
                },
                this::showErrorUnlessAccessDenied
        );
    }

    private void loadCatalogs() {
        CatalogItemDto keepType = deviceTypeCombo.getValue();
        CatalogItemDto keepBrand = deviceBrandCombo.getValue();

        FxTasks.run(
                () -> new CatalogData(catalogApi.findDeviceTypes(), catalogApi.findBrands()),
                data -> {
                    deviceTypes = data.types();
                    brands = data.brands();
                    deviceTypeCombo.setItems(FXCollections.observableArrayList(deviceTypes));
                    deviceBrandCombo.setItems(FXCollections.observableArrayList(brands));
                    selectInCombo(deviceTypeCombo, keepType != null ? findByName(deviceTypes, keepType.getName()) : null);
                    selectInCombo(deviceBrandCombo, keepBrand != null ? findByName(brands, keepBrand.getName()) : null);
                },
                this::showErrorUnlessAccessDenied
        );
    }

    private void setupMasterComboDisplay() {
        masterCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(UserDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });
        masterCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(UserDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Не назначен" : item.getFullName());
            }
        });
    }

    private void loadRepairs() {
        if (Session.isMaster() && Session.getUserId() == null) {
            return;
        }

        String status = statusFilter.getValue();
        String search = searchField.getText();
        Long masterFilter = Session.isMaster() ? Session.getUserId() : null;
        Long keepId = editingRepairId;

        int gen = nextLoadGeneration();
        setBusy(true);
        FxTasks.run(
                () -> repairApi.findAll(false, status, masterFilter, search),
                repairs -> {
                    if (!isLatestLoad(gen)) {
                        return;
                    }
                    setBusy(false);
                    suppressSelection = true;
                    repairTable.setItems(FXCollections.observableArrayList(repairs));
                    if (keepId != null) {
                        repairs.stream().filter(r -> keepId.equals(r.getId())).findFirst()
                                .ifPresent(r -> repairTable.getSelectionModel().select(r));
                    }
                    suppressSelection = false;
                },
                e -> {
                    if (isLatestLoad(gen)) {
                        setBusy(false);
                        showErrorUnlessAccessDenied(e);
                    }
                }
        );
    }

    private static CatalogItemDto findByName(List<CatalogItemDto> items, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return items.stream()
                .filter(i -> i.getName() != null && i.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }

    private static void selectInCombo(ComboBox<CatalogItemDto> combo, CatalogItemDto item) {
        if (item == null) {
            combo.setValue(null);
        } else {
            combo.getItems().stream()
                    .filter(i -> i.getId() != null && i.getId().equals(item.getId()))
                    .findFirst()
                    .ifPresentOrElse(combo::setValue, () -> combo.setValue(item));
        }
    }

    @SafeVarargs
    private void applyLeftAlignRepair(TableColumn<RepairDto, ?>... cols) {
        for (var col : cols) {
            col.setCellFactory(TableStyles.leftAlignedCell());
        }
    }

    private static String formatStatus(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "NEW" -> "Новая";
            case "IN_PROGRESS" -> "В работе";
            case "WAITING_PARTS" -> "Ожидание запчастей";
            case "COMPLETED" -> "Завершена";
            case "CANCELED" -> "Отменена";
            default -> status;
        };
    }

    private static String formatCost(RepairDto r) {
        if (r == null) {
            return "";
        }
        if (r.getFinalCost() != null) {
            return r.getFinalCost() + " ₽";
        }
        if (r.getEstimatedCost() != null) {
            return "~" + r.getEstimatedCost() + " ₽";
        }
        return "";
    }

    private record RefData(List<ClientDto> clients, List<UserDto> masters) {
    }

    private record CatalogData(List<CatalogItemDto> types, List<CatalogItemDto> brands) {
    }
}
