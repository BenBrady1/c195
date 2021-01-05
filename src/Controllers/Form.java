package Controllers;

import Models.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public abstract class Form<T extends Item> extends Base implements Initializable {
    public enum Mode {
        Create,
        Read,
        Update
    }

    protected T item;
    protected Mode mode;
    protected boolean readOnly = true;
    protected Consumer<T> callback;
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (mode != Mode.Create) {
            setFields();
        }
    }

    public void open(T item, Mode mode, Consumer<T> callback) {
        this.item = item;
        this.mode = mode;
        readOnly = mode == Mode.Read;
        this.callback = callback;
        openForm();
    }

    private void callCallback(T item) {
        if (callback != null) {
            callback.accept(item);
        }
        callback = null;
    }

    @FXML
    private void handleSave() {
        callCallback(item);
        handleClose();
    }

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
    protected abstract String getWindowTitle();
    protected abstract String getResourceURL();

    /**
     * Opens a new window with the correct form for the controller
     */
    private void openForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(getResourceURL()), bundle);
            loader.setController(this);
            Scene scene = new Scene(loader.load(), 800, 600);
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
