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
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class Filter extends Base implements Initializable {
    public class FilterFields {
        final public String year;
        final public String field;
        final public int fieldValue;

        public FilterFields(String year, String field, int fieldValue) {
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
    private TextField yearField;
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
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            String bundleProp;
            if (newValue == monthButton) {
                bundleProp = "month";
                setMonths();
            } else {
                bundleProp = "week";
                setWeeks();
            }
            fieldName = bundleProp.toUpperCase();
            comboBoxLabel.setText(getBundleString(String.format("form.%s", bundleProp)));
            comboBox.getSelectionModel().selectFirst();
        });
        toggleGroup.getToggles().addAll(monthButton, weekButton);
        toggleGroup.selectToggle(monthButton);
    }

    private void setWeeks() {
        final ObservableList<ComboBoxValue> items = comboBox.getItems();
        items.clear();
        for (int i = 1; i <= 52; i++) {
            items.add(new ComboBoxValue(Integer.toString(i), i - 1));
        }
    }

    private void setMonths() {
        final ObservableList<ComboBoxValue> items = comboBox.getItems();
        items.clear();
        for (int i = 1; i <= 12; i++) {
            items.add(new ComboBoxValue(getBundleString(String.format("month.%d", i)), i));
        }
    }

    private void callCallback(FilterFields values) {
        if (callback != null) callback.accept(values);
        callback = null;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        final String year = yearField.getText().trim();
        if (year.matches("^\\d{4}$")) {
            final int fieldValue = comboBox.getValue().value;
            final FilterFields fields = new FilterFields(year, fieldName, fieldValue);
            callCallback(fields);
            handleClose(null);
        } else {
            final String message = getBundleString("error.is")
                    .replace("%{field}", getBundleString("form.year"))
                    .replace("%{issue}", getBundleString("issue.invalid"));
            displayError(message);
        }
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
            stage.setTitle(getBundleString("filter.windowTitle"));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
            handleClose(null);
        }
    }
}
