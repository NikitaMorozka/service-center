package ru.servicecenter.client.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ru.servicecenter.client.dto.ClientDto;

public final class ClientComboHelper {

    private ClientComboHelper() {
    }

    public static void setup(ComboBox<ClientDto> combo, String emptyLabel) {
        combo.setCellFactory(clientCellFactory());
        combo.setButtonCell(clientButtonCell(emptyLabel));
    }

    public static Callback<ListView<ClientDto>, ListCell<ClientDto>> clientCellFactory() {
        return listView -> new ListCell<>() {
            @Override
            protected void updateItem(ClientDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        };
    }

    public static ListCell<ClientDto> clientButtonCell(String emptyLabel) {
        return new ListCell<>() {
            @Override
            protected void updateItem(ClientDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(emptyLabel);
                } else {
                    setText(item.getFullName());
                }
            }
        };
    }
}
