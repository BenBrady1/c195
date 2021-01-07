package Controllers;

import Models.Record.ValidationError;
import Models.Record;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public abstract class Form<T extends Record> extends Base implements Initializable {
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
    private String windowTitle;

    public Form(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (mode != Mode.Create) {
            setFields();
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
            validateRecord();
            callCallback(record);
            handleClose();
        } catch (ValidationError err) {
            displayError(err);
        }
    }

    abstract protected void validateRecord() throws ValidationError;

    /**
     * called when the cancel button is clicked or any time the form must be closed
     */
    private void handleClose() {
        stage.hide();
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
            handleClose(null);
        }
    }
}
