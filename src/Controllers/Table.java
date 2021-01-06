package Controllers;

import Models.Record;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public abstract class Table<T extends Record> extends Base implements Initializable {
    @FXML
    protected TableView<T> tableView;

    protected abstract void addColumns();

    protected abstract void populateData();

    protected Form<T> formController;
    protected Optional<Integer> userId;
    protected FormFactory formFactory;

    public Table(Optional<Integer> userId, FormFactory formFactory) {
        this.userId = userId;
        this.formFactory = formFactory;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final TableColumn<T, Long> idColumn = new TableColumn("ID");
        idColumn.setCellValueFactory(param -> new SimpleLongProperty(param.getValue().getId()).asObject());
        tableView.getColumns().add(idColumn);
        addColumns();
        populateData();
        tableView.refresh();
    }

    private void openForm(FormFactory.Type type, T record, Form.Mode mode, Consumer<T> callback) {
        formController = formFactory.getInstance(type);
        formController.open(record, mode, callback);
    }

    private void finalizeAction() {
        tableView.refresh();
        formController = null;
    }

    protected abstract void addToDatabase(T record);

    protected abstract T getNewRecord();

    @FXML
    private void addRecord() {
        if (formController == null) {
            System.out.println("add record called");
            openForm(FormFactory.Type.Create, getNewRecord(), Form.Mode.Create, (newRecord) -> {
                if (newRecord != null) {
                    addToDatabase(newRecord);
                    if (newRecord.getId() != 0) {
                        tableView.getItems().add(newRecord);
                    }
                }
                finalizeAction();
            });
        }
    }

    private T getSelectedRecord() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void viewRecord() {
        T selected = getSelectedRecord();
        System.out.println(selected);
        if (selected != null && formController == null) {
            System.out.println("view record called");
            openForm(FormFactory.Type.Read, selected, Form.Mode.Read, (record) -> finalizeAction());
        }
    }

    protected abstract void updateInDatabase(T record);

    @FXML
    private void editRecord() {
        T selected = getSelectedRecord();
        if (selected != null && formController == null) {
            System.out.println("edit record called");
            openForm(FormFactory.Type.Update, selected, Form.Mode.Update, (updatedRecord) -> {
                if (updatedRecord != null) updateInDatabase(updatedRecord);
                finalizeAction();
            });
        }
    }

    protected abstract void deleteFromDatabase(T record);

    @FXML
    private void deleteRecord() {
        System.out.println("delete record called");
        T recordToDelete = getSelectedRecord();
        if (recordToDelete != null) {
            deleteFromDatabase(recordToDelete);
            if (recordToDelete.getId() == 0) {
                tableView.getItems().remove(recordToDelete);
                tableView.refresh();
            }
        }
    }
}
