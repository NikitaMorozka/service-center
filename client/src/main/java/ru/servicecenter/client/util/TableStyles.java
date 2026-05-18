package ru.servicecenter.client.util;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public final class TableStyles {

    private TableStyles() {
    }

    public static <S> void apply(TableView<S> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        table.setFixedCellSize(40);
        table.getStyleClass().add("data-table");
        table.setPlaceholder(new javafx.scene.control.Label("Нет данных для отображения"));
    }

    /** Справочники с длинными названиями (тип, бренд) — перенос текста и авто-высота строки. */
    public static <S> void applyCatalog(TableView<S> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setFixedCellSize(-1);
        table.getStyleClass().addAll("data-table", "catalog-table");
        table.setPlaceholder(new javafx.scene.control.Label("Список пуст"));
    }

    public static <S, T> void textColumn(TableColumn<S, T> column, String property, String title, double percentWidth) {
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setStyle("-fx-alignment: CENTER-LEFT;");
        column.setReorderable(false);
        column.setSortable(true);
        column.setPrefWidth(percentWidth);
        column.setCellFactory(leftAlignedCell());
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> leftAlignedCell() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.valueOf(item));
                }
                setAlignment(Pos.CENTER_LEFT);
            }
        };
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> wrapTextCell() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(true);
                }
                setAlignment(Pos.TOP_LEFT);
            }
        };
    }
}
