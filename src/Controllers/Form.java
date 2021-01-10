package Controllers;

import Models.Record;
import Models.Record.ValidationError;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class Form<T extends Record> extends Base implements Initializable {
    @FXML
    protected TextField idField;
    @FXML
    private ButtonBar buttonBar;

    public enum Mode {
        Create,
        Read,
        Update
    }

    protected T record;
    protected Mode mode;
    protected boolean readOnly = true;
    protected Consumer<T> callback;
    private Stage stage;
    private final String windowTitle;

    public Form(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (record.getId() != 0) idField.setText(Long.toString(record.getId()));
        idField.setDisable(true);
        if (mode != Mode.Create) {
            if (mode == Mode.Read) {
                buttonBar.setVisible(false);
            }
            setFields();
            setTextFields();
        }
    }

    public void open(T record, Mode mode, Consumer<T> callback) {
        this.record = record;
        this.mode = mode;
        readOnly = mode == Mode.Read;
        this.callback = callback;
        openForm();
    }

    private void callCallback(T record) {
        if (callback != null) {
            callback.accept(record);
        }
        callback = null;
    }

    @FXML
    private void handleSave() {
        try {
            applyStringFormFieldsToRecord();
            applyOtherFieldsToRecord();
            record.validate();
            callCallback(record);
            handleClose();
        } catch (ValidationError err) {
            displayError(err);
        }
    }

    abstract protected void applyOtherFieldsToRecord();

    /**
     * called when the cancel button is clicked or any time the form must be closed
     */
    private void handleClose() {
        if (stage != null) stage.hide();
        stage = null;
    }

    @FXML
    private void handleClose(ActionEvent event) {
        callCallback(null);
        handleClose();
    }

    protected abstract void setFields();

    protected abstract String getResourceURL();

    private String getWindowTitle() {
        return windowTitle;
    }

    protected abstract double getWidth();

    protected abstract double getHeight();

    /**
     * Opens a new window with the correct form for the controller
     */
    private void openForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(getResourceURL()), bundle);
            loader.setController(this);
            Scene scene = new Scene(loader.load(), getWidth(), getHeight());
            stage = new Stage();
            stage.setOnHidden(ev -> handleClose(null));
            stage.setScene(scene);
            stage.setTitle(getWindowTitle());
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
            handleClose(null);
        }
    }

    private void setTextFields() {
        iterateStringFields((textField, recordField) -> {
            try {
                final String data = (String) recordField.get(record);
                textField.setText(data);
                textField.setDisable(readOnly);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private void applyStringFormFieldsToRecord() {
        iterateStringFields((textField, recordField) -> {
            try {
                recordField.set(record, textField.getText().trim());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private List<Field> getStringFields() {
        final List<Field> output = new ArrayList<>();
        for (Field declaredField : record.getClass().getDeclaredFields()) {
            if (declaredField.getType() == String.class) {
                output.add(declaredField);
            }
        }
        return output;
    }

    private void iterateStringFields(BiConsumer<TextField, Field> callback) {
        for (final Field declaredField : getStringFields()) {
            try {
                declaredField.setAccessible(true);
                final String fieldName = declaredField.getName();
                final Field textFieldField = getClass().getDeclaredField(String.format("%sField", fieldName));
                textFieldField.setAccessible(true);
                final TextField input = (TextField) textFieldField.get(this);
                callback.accept(input, declaredField);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    protected long getRecordId(Record record) {
        return Optional.ofNullable(record).map(Record::getId).orElse(0L);
    }
}
