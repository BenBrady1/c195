package Controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class Filter extends Base implements Initializable {
    public class FilterFields {
        final public int year;
        final public String field;
        final public int fieldValue;

        public FilterFields(int year, String field, int fieldValue) {
            this.year = year;
            this.field = field;
            this.fieldValue = fieldValue;
        }
    }

    private class ComboBoxValue {
        final private String display;
        final public int value;

        public ComboBoxValue(String display, int value) {
            this.display = display;
            this.value = value;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    @FXML
    private ComboBox<Integer> yearComboBox;
    @FXML
    private ComboBox<ComboBoxValue> comboBox;
    @FXML
    private ToggleGroup toggleGroup = new ToggleGroup();
    @FXML
    private RadioButton monthButton;
    @FXML
    private RadioButton weekButton;
    @FXML
    private Label comboBoxLabel;

    private Stage stage;
    private Consumer<FilterFields> callback;
    private String fieldName;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setYears();
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> updateItems(newValue));
        yearComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener(((observable, oldValue , newValue) -> updateItems(toggleGroup.getSelectedToggle())));
        toggleGroup.getToggles().addAll(monthButton, weekButton);
        toggleGroup.selectToggle(monthButton);
    }

    private void updateItems(Toggle newValue) {
        String bundleProp;
        if (newValue == monthButton) {
            bundleProp = "month";
            setMonths();
        } else {
            bundleProp = "week";
            setWeeks();
        }
        fieldName = bundleProp.toUpperCase();
        comboBoxLabel.setText(bundle.getString(String.format("form.%s", bundleProp)));
        comboBox.getSelectionModel().selectFirst();
    }

    private void setYears() {
        executeQuery("SElECT DISTINCT YEAR(`Start`) FROM appointments ORDER BY YEAR(`Start`)", (ex, rs) -> {
            if (ex != null) return;
            final ObservableList<Integer> years = yearComboBox.getItems();
            try {
                while (rs.next()) {
                    years.add(rs.getInt(1));
                }
            } catch (SQLException exception) {
                printSQLException(exception);
            }
        });
        yearComboBox.getSelectionModel().selectFirst();
    }

    private void setWeeks() {
        final ObservableList<ComboBoxValue> items = comboBox.getItems();
        items.clear();
        final List<Object> arguments = List.of(yearComboBox.getValue());
        executeQuery("SELECT DISTINCT WEEK(`Start`) " +
                "FROM appointments " +
                "WHERE YEAR(`Start`) = ? " +
                "ORDER BY WEEK(`Start`)", arguments, (ex, rs) -> {
            if (ex != null) return;
            try {
                while (rs.next()) {
                    final int week = rs.getInt(1);
                    items.add(new ComboBoxValue(Integer.toString(week + 1), week));
                }
            } catch (SQLException exception) {
                printSQLException(exception);
            }
        });
    }

    private void setMonths() {
        final ObservableList<ComboBoxValue> items = comboBox.getItems();
        items.clear();
        final List<Object> arguments = List.of(yearComboBox.getValue());
        executeQuery("SELECT DISTINCT MONTH(`Start`) " +
                "FROM appointments " +
                "WHERE YEAR(`Start`) = ? " +
                "ORDER BY MONTH(`Start`)", arguments, (ex, rs) -> {
            if (ex != null) return;
            try {
                while (rs.next()) {
                    final int month = rs.getInt(1);
                    items.add(new ComboBoxValue(bundle.getString(String.format("month.%d", month)), month));
                }
            } catch (SQLException exception) {
                printSQLException(exception);
            }
        });
    }

    private void callCallback(FilterFields values) {
        if (callback != null) callback.accept(values);
        callback = null;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        final int year = yearComboBox.getValue();
        final int fieldValue = comboBox.getValue().value;
        final FilterFields fields = new FilterFields(year, fieldName, fieldValue);
        callCallback(fields);
        handleClose(null);
    }

    @FXML
    private void handleClear(ActionEvent event) {
        callCallback(null);
        handleClose(null);
        toggleGroup.getToggles().clear();
    }

    @FXML
    private void handleClose(ActionEvent event) {
        if (stage != null) stage.close();
        stage = null;
    }

    public void openFilterWindow(Consumer<FilterFields> callback) {
        this.callback = callback;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Filter.fxml"), bundle);
            loader.setController(this);
            Scene scene = new Scene(loader.load(), 400, 400);
            stage = new Stage();
            stage.setOnHidden(ev -> handleClose(null));
            stage.setScene(scene);
            stage.setTitle(bundle.getString("filter.windowTitle"));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
            handleClose(null);
        }
    }
}
