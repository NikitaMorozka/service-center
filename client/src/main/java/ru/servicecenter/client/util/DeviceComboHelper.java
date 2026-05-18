package ru.servicecenter.client.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ru.servicecenter.client.dto.DeviceDto;

public final class DeviceComboHelper {

    private DeviceComboHelper() {
    }

    public static void setup(ComboBox<DeviceDto> combo) {
        combo.setCellFactory(deviceCellFactory());
        combo.setButtonCell(deviceButtonCell());
    }

    public static Callback<ListView<DeviceDto>, ListCell<DeviceDto>> deviceCellFactory() {
        return listView -> new ListCell<>() {
            @Override
            protected void updateItem(DeviceDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : format(item));
            }
        };
    }

    public static ListCell<DeviceDto> deviceButtonCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(DeviceDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Выберите технику" : format(item));
            }
        };
    }

    private static String format(DeviceDto device) {
        return device.getBrand() + " " + device.getModel()
                + (device.getSerialNumber() != null && !device.getSerialNumber().isBlank()
                ? " (" + device.getSerialNumber() + ")" : "");
    }
}
